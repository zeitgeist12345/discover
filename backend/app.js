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

// Per-IP-per-link voting limit
const voteLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute (per link)
  max: 5, // allow 5 vote per 1 minute per IP per link
  message: {
    status: 429,
    error: 'You are voting too quickly for this link. Please wait a few seconds.'
  },
  keyGenerator: (req, res, next) => {
    // Combine IP + link identifier (url)
    const ip = req.ip || req.headers['x-forwarded-for'] || 'unknown';
    const linkUrl = req.query.url || 'unknown';
    return `${ip}_${linkUrl}`;
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
  origin: ['https://discoverall.space', 'http://localhost:44631', 'https://abirusabil123.github.io']
}));

async function logErrorToDB(errorData) {
  try {
    const { source = 'unknown', level = 'error', message, user_agent = null } = errorData;

    if (!message) {
      throw new Error('Message is required for error logging');
    }

    const connection = await mysql.createConnection(dbConfig);
    const [result] = await connection.execute(
      'INSERT INTO errors (source, level, message, user_agent) VALUES (?, ?, ?, ?)',
      [source, level, message, user_agent]
    );
    await connection.end();

    return { success: true, id: result.insertId };
  } catch (dbError) {
    console.error('Failed to log error to database:', dbError);
    return { success: false, error: dbError.message };
  }
}

// Error logging middleware
async function logError(err, req, res, next) {
  const statusCode = err.status || res.statusCode || 500;
  const isClientError = statusCode >= 400 && statusCode < 500;

  try {
    await logErrorToDB({
      source: 'backend',
      level: isClientError ? 'warning' : 'error',
      message: `${statusCode}: ${err.message || 'Client error'} (${req.method} ${req.path})`,
      user_agent: req.headers['user-agent']
    });
  } catch (dbError) {
    console.error('Failed to log error:', dbError);
  }

  // Only call next for server errors (let client errors send response)
  if (!isClientError) {
    next(err);
  }
}

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

// POST endpoint to log errors
app.post('/log-error', async (req, res, next) => {
  try {
    const { source, level, message, user_agent } = req.body;

    if (!message) {
      return res.status(400).json({ error: 'Message is required' });
    }
    const result = await logErrorToDB({
      source,
      level,
      message,
      user_agent: user_agent || req.headers['user-agent']
    });

    if (result.success) {
      res.json({ success: true, id: result.id });
    } else {
      res.status(500).json({ error: 'Failed to log error' });
    }

  } catch (error) {
    console.error('Error logging error:', error);
    res.status(500).json({ error: 'Failed to log error' });
  }
});

// GET endpoint to retrieve errors
app.get('/errors', async (req, res, next) => {
  try {
    const { limit = 100, resolved } = req.query;
    const connection = await mysql.createConnection(dbConfig);

    let query = 'SELECT * FROM errors';
    const params = [];

    if (resolved !== undefined) {
      query += ' WHERE resolved = ?';
      params.push(resolved === 'true' ? 1 : 0); // Fix: Convert boolean to number
    }

    query += ' ORDER BY timestamp DESC LIMIT ?';
    params.push(limit);

    const [rows] = await connection.execute(query, params);
    await connection.end();

    res.json(rows);

  } catch (error) {
    console.error('Get errors error:', error);
    res.status(500).json({ error: 'Failed to fetch errors' });
  }
});

// Mark error as resolved
app.put('/errors/:id/resolve', async (req, res, next) => {
  try {
    const { id } = req.params;
    const connection = await mysql.createConnection(dbConfig);

    const [result] = await connection.execute(
      'UPDATE errors SET resolved = TRUE, resolved_at = CURRENT_TIMESTAMP WHERE id = ?',
      [id]
    );

    await connection.end();

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Error not found' });
    }

    res.json({ success: true });

  } catch (error) {
    console.error('Resolve error error:', error);
    res.status(500).json({ error: 'Failed to resolve error' });
  }
});

// GET visitors analytics
app.get('/visitors-analytics', async (req, res, next) => {
  try {
    const connection = await mysql.createConnection(dbConfig);

    // Get visitors by country
    const [byCountry] = await connection.execute(
      'SELECT country, COUNT(*) as count FROM visitors GROUP BY country ORDER BY count DESC'
    );

    // Get visitors by YYYYMM
    const [byMonth] = await connection.execute(
      'SELECT DATE_FORMAT(timestamp, "%Y%m") as month, COUNT(*) as count FROM visitors GROUP BY month ORDER BY month DESC'
    );

    await connection.end();

    res.json({
      byCountry,
      byMonth
    });

  } catch (error) {
    console.error('Get visitors analytics error:', error);
    res.status(500).json({ error: 'Failed to fetch analytics' });
  }
});

async function logVisitorToDB(visitorData) {
  try {
    const { country = 'unknown', user_agent = 'unknown', origin = 'direct', platform = 'unknown', path = 'unknown', product = 'unknown' } = visitorData;

    const connection = await mysql.createConnection(dbConfig);
    const [result] = await connection.execute(
      'INSERT INTO visitors (country, user_agent, origin, platform, path, product) VALUES (?, ?, ?, ?, ?, ?)', // 6 placeholders
      [
        country.substring(0, 10),
        user_agent.substring(0, 1000),
        origin.substring(0, 500),
        platform.substring(0, 50),
        path.substring(0, 255),
        product.substring(0, 255)
      ]
    );
    await connection.end();
    return { success: true, id: result.insertId };
  } catch (dbError) {
    console.error('Failed to log visitor:', dbError);
    return { success: false, error: dbError.message };
  }
}

// POST API for JavaScript frontend to log visitors
app.post('/api/log-visitor', async (req, res) => {
  // Get country from headers (Cloudflare provides this)
  const country = req.headers['cf-ipcountry'] || req.headers['x-country'] || 'unknown';

  try {
    const {
      user_agent,
      origin,
      platform,
      path,
      product
    } = req.body; // <-- FIX: Use req.body, not req.query

    // Validate required fields
    if (!path && !product) {
      return res.status(400).json({
        success: false,
        error: 'At least path or product is required'
      });
    }
    // Log visitor to database
    const result = await logVisitorToDB({
      country: country, // From headers
      user_agent: user_agent || req.headers['user-agent'] || 'unknown',
      origin: origin || req.headers.origin || 'direct',
      platform: platform || 'web',
      path: path || '/',
      product: product || 'frontend-app'
    });

    if (result.success) {
      res.json({
        success: true,
        id: result.id,
        message: 'Visitor logged successfully'
      });
    } else {
      res.status(500).json({
        success: false,
        error: 'Failed to log visitor'
      });
    }
  } catch (error) {
    console.error('Error in log-visitor endpoint:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
});

// Get all links
app.get('/getLinks', async (req, res, next) => {
  const { platform, reviewStatusEnable, tagsAllowlist, tagsBlocklist } = req.query;
  const country = req.headers['cf-ipcountry'] || req.headers['x-country'] || 'Unknown';
  const userAgent = req.headers['user-agent'] || 'Unknown';
  const origin = req.headers.origin || 'direct';

  await logVisitorToDB({
    country: country,
    user_agent: userAgent,
    origin: origin,
    platform: platform,
    path: '/getLinks',
    product: 'discover-backend'
  });

  try {

    const tagsAllowlistArray = (tagsAllowlist || "")
      .split(",")
      .map(t => t.trim())
      .filter(Boolean);

    const tagsBlocklistArray = (tagsBlocklist || "")
      .split(",")
      .map(t => t.trim())
      .filter(Boolean);

    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT * FROM links ORDER BY name');
    await connection.end();

    const parsed = rows.map(w => {
      try {
        w.tags = Array.isArray(w.tags)
          ? w.tags
          : JSON.parse(w.tags || '[]');
      } catch (err) {
        console.warn("Failed to parse tags for link url:", w.url);
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
    console.error('Get links error:', error);
    next(error);
  }
});

app.post('/incrementView', voteLimiter, async (req, res, next) => {
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

    query = `UPDATE links SET ${fieldToUpdate} WHERE url = ?`;
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
      message: `Successfully incremented ${action}`
    });
  } catch (error) {
    console.error('Increment view error:', error);
    next(error);
  }
});

app.post('/removeLink', voteLimiter, async (req, res, next) => {
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

    query = `UPDATE links SET ${fieldToUpdate} WHERE url = ?`;
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
    next(error);
  }
});

// Add new link
app.post('/addlink', apiLimiter, async (req, res, next) => {
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

    // Check if link already exists
    const [existing] = await connection.execute(
      'SELECT url FROM links WHERE url = ?',
      [url]
    );

    if (existing.length > 0) {
      await connection.end();
      return res.status(409).json({ error: 'Link already exists' });
    }

    // Insert new link
    const [result] = await connection.execute(
      'INSERT INTO links (name, url, description, tags, views, likesMobile, dislikesMobile, likesDesktop, dislikesDesktop, reviewStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
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
    console.error('Add link error:', error);
    next(error);
  }
});

// Health check endpoint
app.get('/health', async (req, res, next) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    await connection.execute('SELECT 1');
    await connection.end();

    res.json({ status: 'OK', message: 'Database connection successful' });
  } catch (error) {
    next(error);
  }
});

// Default route
app.get('/', async (req, res, next) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT COUNT(*) as count FROM links');
    await connection.end();

    res.json({
      message: 'Discover Backend API',
      totalLinks: rows[0].count,
      endpoints: {
        '/getLinks': 'GET - Get links',
        '/incrementView': 'POST - Update link stats',
        '/addlink': 'POST - Add new link',
        '/removeLink': 'POST - Remove link',
        '/health': 'GET - Health check'
      }
    });
  } catch (error) {
    next(error);
  }
});

app.use(logError);
app.use((err, req, res, next) => {
  res.status(500).json({ error: 'Internal Server Error' });
});

app.listen(3000, '0.0.0.0', () => {
  console.log('Server running on port 3000');
});
