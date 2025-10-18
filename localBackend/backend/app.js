const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const app = express();
const PORT = process.env.PORT || 3000;

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
app.use(cors());

// Get all websites (for desktop)
app.get('/getWebsitesDesktop', async (req, res) => {
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

// Increment view/like/dislike counts
app.post('/incrementViewDesktop', async (req, res) => {
  try {
    const { id, url, category, action } = req.query;

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
      case 'likeDesktop':
        fieldToUpdate = 'likes = likes + 1, likesDesktop = likesDesktop + 1';
        break;
      case 'dislike':
      case 'dislikeDesktop':
        fieldToUpdate = 'dislikes = dislikes + 1, dislikesDesktop = dislikesDesktop + 1';
        break;
      default:
        fieldToUpdate = 'views = views + 1';
    }

    if (id) {
      query = `UPDATE websites SET ${fieldToUpdate} WHERE id = ?`;
      params = [id];
    } else {
      query = `UPDATE websites SET ${fieldToUpdate} WHERE url = ?`;
      params = [url];
    }

    const [result] = await connection.execute(query, params);

    if (result.affectedRows === 0) {
      // If website doesn't exist, create it first
      const insertQuery = 'INSERT INTO websites (name, url, category, views, likes, dislikes) VALUES (?, ?, ?, 1, 0, 0)';
      const domain = url.replace(/https?:\/\/(www\.)?/, '').split('/')[0];
      await connection.execute(insertQuery, [domain, url, category || 'curated']);
    }

    // Get updated stats
    const [updatedRows] = await connection.execute(
      'SELECT views, likes, dislikes, likesDesktop, dislikesDesktop FROM websites WHERE id = ? OR url = ?',
      [id || null, url || null]
    );

    await connection.end();

    res.json(updatedRows[0] || { views: 1, likes: 0, dislikes: 0, likesDesktop: 0, dislikesDesktop: 0 });
  } catch (error) {
    console.error('Increment view error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Add new website
app.post('/addwebsite', async (req, res) => {
  try {
    const { name, url, description, category, views, likes, dislikes, likesDesktop, dislikesDesktop } = req.body;

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
      'INSERT INTO websites (name, url, description, category, views, likes, dislikes, likesDesktop, dislikesDesktop) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
      [name, url, description, category || 'user-submitted', views || 0, likes || 0, dislikes || 0, likesDesktop || 0, dislikesDesktop || 0]
    );

    await connection.end();

    res.json({
      id: result.insertId,
      message: 'Website added successfully',
      name,
      url,
      description,
      category: category || 'user-submitted'
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
        '/getWebsitesDesktop': 'GET - Get all websites',
        '/incrementViewDesktop': 'POST - Update website stats',
        '/addwebsite': 'POST - Add new website',
        '/website/:id': 'GET - Get website by ID',
        '/health': 'GET - Health check'
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`API endpoints available at http://localhost:${PORT}`);
});