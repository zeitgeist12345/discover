CREATE TABLE IF NOT EXISTS websites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description TEXT,
    category VARCHAR(100) DEFAULT 'curated',
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    dislikes INT DEFAULT 0,
    likesDesktop INT DEFAULT 0,
    dislikesDesktop INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_url (url(255))
);
INSERT IGNORE INTO websites (
        name,
        url,
        description,
        category,
        views,
        likes,
        dislikes,
        likesDesktop,
        dislikesDesktop
    )
VALUES (
        'GitHub',
        'https://github.com',
        'The world''s leading software development platform',
        'tools',
        150,
        45,
        2,
        22,
        1
    ),
    (
        'Wikipedia',
        'https://wikipedia.org',
        'The free encyclopedia that anyone can edit',
        'educational',
        200,
        60,
        5,
        30,
        2
    );