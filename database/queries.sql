-- Additional database queries for management

-- ===========================
-- USER MANAGEMENT
-- ===========================

-- View all users with full stats
SELECT 
    user_id,
    username,
    total_score,
    total_wins,
    total_losses,
    total_draws,
    CONCAT(ROUND(total_wins * 100.0 / NULLIF(total_wins + total_losses + total_draws, 0), 2), '%') as win_rate,
    is_online,
    is_busy,
    created_at
FROM users
ORDER BY total_score DESC, total_wins DESC;

-- Find top 10 players
SELECT 
    RANK() OVER (ORDER BY total_score DESC, total_wins DESC) as ranking,
    username,
    total_score,
    total_wins,
    total_losses,
    total_draws
FROM users
LIMIT 10;

-- Reset a user's stats
UPDATE users 
SET total_score = 0, total_wins = 0, total_losses = 0, total_draws = 0
WHERE username = 'player1';

-- Delete inactive users (not logged in for 30 days)
DELETE FROM users 
WHERE is_online = FALSE 
AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- ===========================
-- MATCH MANAGEMENT
-- ===========================

-- View recent matches
SELECT 
    m.match_id,
    u1.username as player1,
    u2.username as player2,
    m.player1_score,
    m.player2_score,
    CASE 
        WHEN m.winner_id = m.player1_id THEN u1.username
        WHEN m.winner_id = m.player2_id THEN u2.username
        ELSE 'Draw'
    END as winner,
    m.status,
    m.start_time,
    m.end_time,
    TIMESTAMPDIFF(SECOND, m.start_time, m.end_time) as duration_seconds
FROM game_matches m
JOIN users u1 ON m.player1_id = u1.user_id
JOIN users u2 ON m.player2_id = u2.user_id
WHERE m.status = 'FINISHED'
ORDER BY m.end_time DESC
LIMIT 20;

-- Match statistics
SELECT 
    COUNT(*) as total_matches,
    COUNT(CASE WHEN winner_id IS NOT NULL THEN 1 END) as decided_matches,
    COUNT(CASE WHEN winner_id IS NULL AND status = 'FINISHED' THEN 1 END) as draws,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled,
    AVG(TIMESTAMPDIFF(SECOND, start_time, end_time)) as avg_duration_seconds
FROM game_matches
WHERE status IN ('FINISHED', 'CANCELLED');

-- Player match history
SELECT 
    m.match_id,
    CASE 
        WHEN m.player1_id = 1 THEN u2.username 
        ELSE u1.username 
    END as opponent,
    CASE 
        WHEN m.player1_id = 1 THEN m.player1_score 
        ELSE m.player2_score 
    END as my_score,
    CASE 
        WHEN m.player1_id = 1 THEN m.player2_score 
        ELSE m.player1_score 
    END as opponent_score,
    CASE 
        WHEN m.winner_id = 1 THEN 'WIN'
        WHEN m.winner_id IS NULL THEN 'DRAW'
        ELSE 'LOSS'
    END as result,
    m.end_time
FROM game_matches m
JOIN users u1 ON m.player1_id = u1.user_id
JOIN users u2 ON m.player2_id = u2.user_id
WHERE (m.player1_id = 1 OR m.player2_id = 1)
AND m.status = 'FINISHED'
ORDER BY m.end_time DESC;

-- Delete old matches (older than 90 days)
DELETE FROM game_matches 
WHERE status = 'FINISHED' 
AND end_time < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- ===========================
-- STATISTICS & ANALYTICS
-- ===========================

-- Overall system statistics
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM users WHERE is_online = TRUE) as online_users,
    (SELECT COUNT(*) FROM game_matches WHERE status = 'PLAYING') as active_matches,
    (SELECT COUNT(*) FROM game_matches WHERE status = 'FINISHED') as total_finished_matches,
    (SELECT AVG(total_score) FROM users) as avg_user_score;

-- Daily match statistics
SELECT 
    DATE(start_time) as date,
    COUNT(*) as matches_played,
    AVG(player1_score + player2_score) as avg_total_score
FROM game_matches
WHERE status = 'FINISHED'
AND start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(start_time)
ORDER BY date DESC;

-- Most active players (last 7 days)
SELECT 
    u.username,
    COUNT(*) as matches_played,
    SUM(CASE 
        WHEN m.winner_id = u.user_id THEN 1 
        ELSE 0 
    END) as wins,
    u.total_score
FROM users u
JOIN game_matches m ON (u.user_id = m.player1_id OR u.user_id = m.player2_id)
WHERE m.status = 'FINISHED'
AND m.start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY u.user_id, u.username, u.total_score
ORDER BY matches_played DESC
LIMIT 10;

-- Head-to-head statistics
SELECT 
    u1.username as player1,
    u2.username as player2,
    COUNT(*) as total_matches,
    SUM(CASE WHEN m.winner_id = m.player1_id THEN 1 ELSE 0 END) as player1_wins,
    SUM(CASE WHEN m.winner_id = m.player2_id THEN 1 ELSE 0 END) as player2_wins,
    SUM(CASE WHEN m.winner_id IS NULL THEN 1 ELSE 0 END) as draws
FROM game_matches m
JOIN users u1 ON m.player1_id = u1.user_id
JOIN users u2 ON m.player2_id = u2.user_id
WHERE m.status = 'FINISHED'
GROUP BY m.player1_id, m.player2_id, u1.username, u2.username
HAVING total_matches > 0
ORDER BY total_matches DESC;

-- ===========================
-- MAINTENANCE
-- ===========================

-- Reset all online/busy status (useful after server crash)
UPDATE users SET is_online = FALSE, is_busy = FALSE;

-- Cancel all stuck matches
UPDATE game_matches 
SET status = 'CANCELLED' 
WHERE status = 'PLAYING' 
AND start_time < DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- Optimize tables
OPTIMIZE TABLE users;
OPTIMIZE TABLE game_matches;

-- Backup command (run in terminal)
-- mysqldump -u root -p dart_game > backup_$(date +%Y%m%d).sql

-- Check database size
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'dart_game'
ORDER BY (data_length + index_length) DESC;
