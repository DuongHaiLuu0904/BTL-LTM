-- Create database
CREATE DATABASE IF NOT EXISTS gameLTM;
USE gameLTM;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    total_score INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_losses INT DEFAULT 0,
    total_draws INT DEFAULT 0,
    is_online BOOLEAN DEFAULT FALSE,
    is_busy BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_online (is_online),
    INDEX idx_score (total_score DESC, total_wins DESC)
);

-- Game matches table
CREATE TABLE IF NOT EXISTS game_matches (
    match_id INT PRIMARY KEY AUTO_INCREMENT,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    player1_score INT DEFAULT 0,
    player2_score INT DEFAULT 0,
    winner_id INT,
    current_player_id INT,
    player1_throws_left INT DEFAULT 5,
    player2_throws_left INT DEFAULT 5,
    board_rotation INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PLAYING',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    FOREIGN KEY (player1_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (player2_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_player1 (player1_id),
    INDEX idx_player2 (player2_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time DESC)
);

-- Match throw details table (Chi tiết từng lần ném trong trận đấu)
CREATE TABLE IF NOT EXISTS match_throw_details (
    throw_id INT PRIMARY KEY AUTO_INCREMENT,
    match_id INT NOT NULL,
    player_id INT NOT NULL,
    throw_number INT NOT NULL,
    theta_deg DOUBLE DEFAULT 0,
    phi_deg DOUBLE DEFAULT 0,
    power_percent DOUBLE DEFAULT 0,
    x_hit DOUBLE DEFAULT 0,
    y_hit DOUBLE DEFAULT 0,
    score INT DEFAULT 0,
    hit_board BOOLEAN DEFAULT FALSE,
    throw_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES game_matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_match (match_id),
    INDEX idx_player (player_id),
    INDEX idx_throw_time (throw_time DESC)
);

-- Insert some test data
INSERT INTO users (username, password, total_score, total_wins, total_losses, total_draws) 
VALUES 
    ('player1', 'password123', 15, 5, 2, 0),
    ('player2', 'password123', 12, 4, 3, 0),
    ('player3', 'password123', 9, 3, 2, 0),
    ('admin', 'admin123', 20, 7, 1, 0)
ON DUPLICATE KEY UPDATE username=username;
