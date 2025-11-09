const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const app = express();
const PORT = process.env.PORT || 3000;

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
    // Combine IP + website identifier (id or url)
    const ip = req.ip || req.headers['x-forwarded-for'] || 'unknown';
    const websiteId = req.query.id || req.query.url || 'unknown';
    return `${ip}_${websiteId}`;
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
// For production, replace '*' with your frontend URL to restrict access:
// origin: 'https://your-frontend-domain.com'
app.use(cors({
  origin: '*', // allow all origins (for testing)
  // origin: [
  //   'https://discoverall.space',
  //   'https://backend.discoverall.space'
  // ],
  methods: ['GET', 'POST'],
  allowedHeaders: ['Content-Type']
}));

// Filtering criteria
function needToIgnore(likes, dislikes) {
  const total = likes + dislikes;
  // If less votes, consider it okay
  if (total <= 3) {
    return false;
  }
  const undesirable_score = dislikes / Math.max(total, 1);

  return undesirable_score > 0.8;
}

// Get all websites
app.get('/getWebsites', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT * FROM websites ORDER BY name');
    await connection.end();

    res.json(rows);
  } catch (error) {
    console.error('Get websites error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get all websites (for desktop)
app.get('/getWebsitesDesktop', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT * FROM websites ORDER BY name');
    await connection.end();

    // Filter out undesirable websites
    const filteredRows = rows.filter(website =>
      !needToIgnore(website.likesDesktop, website.dislikesDesktop)
    );

    res.json(filteredRows);
  } catch (error) {
    console.error('Get websites error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get mobile websites
app.get('/getWebsitesMobile', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute('SELECT * FROM websites ORDER BY name');
    await connection.end();

    // Filter out undesirable websites
    const filteredRows = rows.filter(website =>
      !needToIgnore(website.likes, website.dislikes)
    );

    res.json(filteredRows);
  } catch (error) {
    console.error('Get websites error:', error);
    res.status(500).json({ error: error.message });
  }
});

app.post('/incrementView', voteLimiter, async (req, res) => {
  try {
    const { id, url, action } = req.query;

    if (!id && !url) {
      return res.status(400).json({ error: 'ID or URL parameter is required' });
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
      case 'like':
        fieldToUpdate = 'likes = likes + 1';
        break;
      case 'dislike':
        fieldToUpdate = 'dislikes = dislikes + 1';
      case 'likeDesktop':
        fieldToUpdate = 'likesDesktop = likesDesktop + 1';
        break;
      case 'dislikeDesktop':
        fieldToUpdate = 'dislikesDesktop = dislikesDesktop + 1';
        break;
      default:
        return res.status(400).json({ success: false, error: 'Invalid action type' });
    }

    if (id) {
      query = `UPDATE websites SET ${fieldToUpdate} WHERE id = ?`;
      params = [id];
    } else {
      query = `UPDATE websites SET ${fieldToUpdate} WHERE url = ?`;
      params = [url];
    }

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
    const { id, url, action } = req.query;

    if (!id && !url) {
      return res.status(400).json({ error: 'ID or URL parameter is required' });
    }

    const connection = await mysql.createConnection(dbConfig);
    let query;
    let params;

    // Determine which field to update based on action
    let fieldToUpdate;
    switch (action) {
      case 'dislike':
        fieldToUpdate = 'dislikes = dislikes + 1000';
        break;
      case 'dislikeDesktop':
        fieldToUpdate = 'dislikesDesktop = dislikesDesktop + 1000';
        break;
      default:
        return res.status(400).json({ success: false, error: 'Invalid action type' });
    }

    if (id) {
      query = `UPDATE websites SET ${fieldToUpdate} WHERE id = ?`;
      params = [id];
    } else {
      query = `UPDATE websites SET ${fieldToUpdate} WHERE url = ?`;
      params = [url];
    }

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
    const { name, url, description, tags, views, likes, dislikes, likesDesktop, dislikesDesktop } = req.body;

    if (!name || !url || !description) {
      return res.status(400).json({ error: 'Name, URL, and description are required' });
    }

    const connection = await mysql.createConnection(dbConfig);

    // Check if website already exists
    const [existing] = await connection.execute(
      'SELECT id FROM websites WHERE url = ?',
      [url]
    );

    if (existing.length > 0) {
      await connection.end();
      return res.status(409).json({ error: 'Website already exists' });
    }

    // Insert new website
    const [result] = await connection.execute(
      'INSERT INTO websites (name, url, description, tags, views, likes, dislikes, likesDesktop, dislikesDesktop) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
      [name, url, description, tags, views || 0, likes || 0, dislikes || 0, likesDesktop || 0, dislikesDesktop || 0]
    );

    await connection.end();

    res.json({
      id: result.insertId,
      message: 'Website added successfully',
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

// Get website by ID
app.get('/website/:id', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    const [rows] = await connection.execute(
      'SELECT * FROM websites WHERE id = ?',
      [req.params.id]
    );

    await connection.end();

    if (rows.length === 0) {
      return res.status(404).json({ error: 'Website not found' });
    }

    res.json(rows[0]);
  } catch (error) {
    console.error('Get website error:', error);
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
        '/getWebsites': 'GET - Get all links',
        '/getWebsitesDesktop': 'GET - Get desktop links',
        '/getWebsitesMobile': 'GET - Get mobile links',
        '/incrementView': 'POST - Update link stats',
        '/addwebsite': 'POST - Add new website',
        '/removeLink': 'POST - Remove link',
        '/website/:id': 'GET - Get website by ID',
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
