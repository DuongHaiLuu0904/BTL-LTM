package com.example.controller;

import com.example.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    // Register new user
    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, total_score, total_wins, total_losses, total_draws, is_online, is_busy) VALUES (?, ?, 0, 0, 0, 0, FALSE, FALSE)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Login user
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                // Update online status
                updateOnlineStatus(user.getUserId(), true);
                user.setOnline(true);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Logout user
    public boolean logoutUser(int userId) {
        return updateOnlineStatus(userId, false);
    }
    
    // Update online status
    public boolean updateOnlineStatus(int userId, boolean isOnline) {
        String sql = "UPDATE users SET is_online = ? WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, isOnline);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update busy status
    public boolean updateBusyStatus(int userId, boolean isBusy) {
        String sql = "UPDATE users SET is_busy = ? WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, isBusy);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all online users
    public List<User> getOnlineUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_online = TRUE ORDER BY username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    // Get user by ID
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Update user statistics after game
    public boolean updateUserStats(int userId, int scoreToAdd, String result) {
        String sql = "UPDATE users SET total_score = total_score + ?, " +
                    "total_wins = total_wins + ?, " +
                    "total_losses = total_losses + ?, " +
                    "total_draws = total_draws + ? " +
                    "WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, scoreToAdd);
            
            // Update wins, losses, draws based on result
            if ("WIN".equals(result)) {
                stmt.setInt(2, 1);  // wins
                stmt.setInt(3, 0);  // losses
                stmt.setInt(4, 0);  // draws
            } else if ("LOSS".equals(result)) {
                stmt.setInt(2, 0);
                stmt.setInt(3, 1);
                stmt.setInt(4, 0);
            } else if ("DRAW".equals(result)) {
                stmt.setInt(2, 0);
                stmt.setInt(3, 0);
                stmt.setInt(4, 1);
            } else {
                stmt.setInt(2, 0);
                stmt.setInt(3, 0);
                stmt.setInt(4, 0);
            }
            
            stmt.setInt(5, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get leaderboard
    public List<User> getLeaderboard() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY total_score DESC, total_wins DESC, username ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    // Check if username exists
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Reset all users to offline (called when server starts)
    public boolean resetAllUsersToOffline() {
        String sql = "UPDATE users SET is_online = FALSE, is_busy = FALSE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            System.out.println("Reset " + updated + " users to offline status");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to extract User from ResultSet
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setTotalScore(rs.getInt("total_score"));
        user.setTotalWins(rs.getInt("total_wins"));
        user.setTotalLosses(rs.getInt("total_losses"));
        user.setTotalDraws(rs.getInt("total_draws"));
        user.setOnline(rs.getBoolean("is_online"));
        user.setBusy(rs.getBoolean("is_busy"));
        return user;
    }
}
