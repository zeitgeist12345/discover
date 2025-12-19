const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const app = express();
const PORT = process.env.PORT || 3000;

// Configure Express to trust proxies (important for Docker + Cloudflare)
app.set('trust proxy', 1);

// IP based rate limiting
const apiLimiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 minute
  max: 100, // limit each IP to 100 requests per windowMs
  message: {
    status: 429,
    error: 'Too many requests, please try again later.'
  },
  standardHeaders: true, // Return rate limit info in `RateLimit-*` headers
  legacyHeaders: false // Disable the `X-RateLimit-*` headers
});

// Per-IP-per-website voting limit
const voteLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute (per website)
  max: 5, // allow 5 vote per 1 minute per IP per website
  message: {
    status: 429,
    error: 'You are voting too quickly for this website. Please wait a few seconds.'
  },
  keyGenerator: (req, res) => {
    // Combine IP + website identifier (url)
    const ip = req.ip || req.headers['x-forwarded-for'] || 'unknown';
    const websiteUrl = req.query.url || 'unknown';
    return `${ip}_${websiteUrl}`;
  },
  standardHeaders: true,
  legacyHeaders: false
});

// Database connection
const dbConfig = {
  host: process.env.DB_HOST || 'db',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || 'password',
  database: process.env.DB_NAME || 'mydatabase',
  multipleStatements: true // Allow multiple SQL statements
};

// Middleware
app.use(express.json());
app.use(apiLimiter);

// Cors
app.use(cors({
  origin: ['https://discoverall.space', 'http://localhost:44631']
}));

// Filtering criteria
function needToIgnore(likesMobile, dislikesMobile) {
  const total = likesMobile + dislikesMobile;
  // If less votes, consider it okay
  if (total <= 3) {
    return false;
  }
  const undesirable_score = dislikesMobile / Math.max(total, 1);

  return undesirable_score > 0.8;
}

// Get all websites
app.get('/getWebsites', async (req, res) => {

  console.log('Request origin:', req.headers.origin);
  console.log('Request query:', req.query);
  try {
    const { platform, reviewStatusEnable, tagsAllowlist, tagsBlocklist } = req.query;

    tagsAllowlistArray = (tagsAllowlist || "")
      .split(",")
      .map(t => t.trim())
      .filter(Boolean);

    tagsBlocklistArray = (tagsBlocklist || "")
      .split(",")
      .map(t => t.trim())
      .filter(Boolean);

    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT * FROM websites ORDER BY name');
    await connection.end();

    const parsed = rows.map(w => {
      try {
        w.tags = Array.isArray(w.tags)
          ? w.tags
          : JSON.parse(w.tags || '[]');
      } catch (err) {
        console.warn("Failed to parse tags for website url:", w.url);
        w.tags = [];
      }
      return w;
    });

    let filtered = parsed.filter(w => {
      const hasAllow = tagsAllowlistArray.length === 0 || w.tags.some(t => tagsAllowlistArray.includes(t));
      const hasBlock = tagsBlocklistArray.length > 0 && w.tags.some(t => tagsBlocklistArray.includes(t));

      if (!hasAllow) return false;
      if (hasBlock) return false;

      if (reviewStatusEnable != "1" && w.reviewStatus === 0) return false;

      if (platform === "desktop" && needToIgnore(w.likesDesktop, w.dislikesDesktop)) return false;
      if (platform === "mobile" && needToIgnore(w.likesMobile, w.dislikesMobile)) return false;

      return true;
    });

    res.json(filtered);
  } catch (error) {
    console.error('Get websites error:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

app.post('/incrementView', voteLimiter, async (req, res) => {
  try {
    const { url, action } = req.query;

    if (!url) {
      return res.status(400).json({ error: 'URL parameter is required' });
    }

    const connection = await mysql.createConnection(dbConfig);
    let query;
    let params;

    // Determine which field to update based on action
    let fieldToUpdate;
    switch (action) {
      case 'view':
        fieldToUpdate = 'views = views + 1';
        break;
      case 'likes':
        fieldToUpdate = 'likesMobile = likesMobile + 1';
        break;
      case 'unlikes':
        fieldToUpdate = 'likesMobile = likesMobile - 1';
        break;
      case 'dislikes':
        fieldToUpdate = 'dislikesMobile = dislikesMobile + 1';
        break;
      case 'undislikes':
        fieldToUpdate = 'dislikesMobile = dislikesMobile - 1';
        break;
      case 'likesDesktop':
        fieldToUpdate = 'likesDesktop = likesDesktop + 1';
        break;
      case 'unlikesDesktop':
        fieldToUpdate = 'likesDesktop = likesDesktop - 1';
        break;
      case 'dislikesDesktop':
        fieldToUpdate = 'dislikesDesktop = dislikesDesktop + 1';
        break;
      case 'undislikesDesktop':
        fieldToUpdate = 'dislikesDesktop = dislikesDesktop - 1';
        break;
      default:
        return res.status(400).json({ success: false, error: 'Invalid action type' });
    }

    query = `UPDATE websites SET ${fieldToUpdate} WHERE url = ?`;
    params = [url];

    const [result] = await connection.execute(query, params);

    // Check if update affected any rows
    if (result.affectedRows === 0) {
      await connection.end();
      return res.status(404).json({ success: false, error: 'Website not found' });
    }

    await connection.end();


    res.json({
      success: true,
      message: `Successfully incremented ${action}`
    });
  } catch (error) {
    console.error('Increment view error:', error);
    res.status(500).json({ error: error.message });
  }
});

app.post('/removeLink', voteLimiter, async (req, res) => {
  try {
    const { url, action } = req.query;

    if (!url) {
      return res.status(400).json({ error: 'URL parameter is required' });
    }

    const connection = await mysql.createConnection(dbConfig);
    let query;
    let params;

    // Determine which field to update based on action
    let fieldToUpdate;
    switch (action) {
      case 'dislikesMobile':
        fieldToUpdate = 'dislikesMobile = dislikesMobile + 1000';
        break;
      case 'dislikesDesktop':
        fieldToUpdate = 'dislikesDesktop = dislikesDesktop + 1000';
        break;
      default:
        return res.status(400).json({ success: false, error: 'Invalid action type' });
    }

    query = `UPDATE websites SET ${fieldToUpdate} WHERE url = ?`;
    params = [url];

    const [result] = await connection.execute(query, params);

    // Check if update affected any rows
    if (result.affectedRows === 0) {
      await connection.end();
      return res.status(404).json({ success: false, error: 'Link not found' });
    }

    await connection.end();

    res.json({
      success: true,
      message: `Successfully removed ${action}`
    });
  } catch (error) {
    console.error('Remove error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Add new website
app.post('/addwebsite', async (req, res) => {
  try {
    let { name, url, description, tags, views, likesMobile, dislikesMobile, likesDesktop, dislikesDesktop } = req.body;

    // Remove all types of newlines (including \r)
    name = name?.replace(/[\r\n]+/g, '') || name;
    url = url?.replace(/[\r\n]+/g, '') || url;
    description = description?.replace(/[\r\n]+/g, '') || description;
    tags = tags
      .map(tag => tag.trim())
      .map(tag => tag.replace(/[\r\n]+/g, '')) // Remove special characters
      .map(tag => tag.toLowerCase()) // Convert to lowercase
      .filter((tag, index, array) => array.indexOf(tag) === index); // Remove duplicates

    if (!name || !url || !description) {
      return res.status(400).json({ error: 'Name, URL, and description are required' });
    }

    /////////////////////////////////////////////////

    url = url.trim();
    // Remove any extra whitespace
    url = url.replace(/\s+/g, '');
    // Check if URL has a protocol, add https:// if not
    if (!url.match(/^https?:\/\//i)) {
      url = 'https://' + url;
    }
    try {
      // Use URL constructor to validate
      const urlObj = new URL(url);

      // Ensure it's a valid web URL (http or https)
      if (!['http:', 'https:'].includes(urlObj.protocol)) {
        throw new Error('Invalid protocol');
      }

      url = urlObj.href;
    } catch (error) {
      throw new Error('Invalid URL format');
    }

    /////////////////////////////////////////////////

    const connection = await mysql.createConnection(dbConfig);

    // Check if website already exists
    const [existing] = await connection.execute(
      'SELECT url FROM websites WHERE url = ?',
      [url]
    );

    if (existing.length > 0) {
      await connection.end();
      return res.status(409).json({ error: 'Website already exists' });
    }

    // Insert new website
    const [result] = await connection.execute(
      'INSERT INTO websites (name, url, description, tags, views, likesMobile, dislikesMobile, likesDesktop, dislikesDesktop, reviewStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
      [name, url, description, tags, views || 0, likesMobile || 0, dislikesMobile || 0, likesDesktop || 0, dislikesDesktop || 0, 0]
    );

    await connection.end();

    res.json({
      message: 'Link submitted for spam review successfully! The link will be live globally after review approval 🎉',
      name,
      url,
      description,
      tags
    });
  } catch (error) {
    console.error('Add website error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Health check endpoint
app.get('/health', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    await connection.execute('SELECT 1');
    await connection.end();

    res.json({ status: 'OK', message: 'Database connection successful' });
  } catch (error) {
    res.status(500).json({ status: 'ERROR', error: error.message });
  }
});

// Default route
app.get('/', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT COUNT(*) as count FROM websites');
    await connection.end();

    res.json({
      message: 'Discover Backend API',
      totalWebsites: rows[0].count,
      endpoints: {
        '/getWebsites': 'GET - Get links',
        '/incrementView': 'POST - Update link stats',
        '/addwebsite': 'POST - Add new website',
        '/removeLink': 'POST - Remove link',
        '/health': 'GET - Health check'
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.listen(3000, '0.0.0.0', () => {
  console.log('Server running on port 3000');
});
