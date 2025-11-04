package com.example.controller;

import com.example.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;

    // Constructor khởi tạo kết nối database
    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // Đăng ký người dùng mới
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

    // Đăng nhập người dùng
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                // Cập nhật trạng thái online
                updateOnlineStatus(user.getUserId(), true);
                user.setOnline(true);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Đăng xuất người dùng
    public boolean logoutUser(int userId) {
        return updateOnlineStatus(userId, false);
    }

    // Cập nhật trạng thái online
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

    // Cập nhật trạng thái bận
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

    // Lấy danh sách tất cả người dùng online
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

    // Lấy người dùng theo ID
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

    // Cập nhật thống kê người dùng sau trận đấu
    public boolean updateUserStats(int userId, int scoreToAdd, String result) {
        String sql = "UPDATE users SET total_score = total_score + ?, " +
                "total_wins = total_wins + ?, " +
                "total_losses = total_losses + ?, " +
                "total_draws = total_draws + ? " +
                "WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, scoreToAdd);

            // Cập nhật thắng, thua, hòa dựa trên kết quả
            if ("WIN".equals(result)) {
                stmt.setInt(2, 1); // thắng
                stmt.setInt(3, 0); // thua
                stmt.setInt(4, 0); // hòa
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

    // Lấy bảng xếp hạng
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

    // Kiểm tra xem tên đăng nhập đã tồn tại chưa
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

    // Đặt lại tất cả người dùng về offline (được gọi khi server khởi động)
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

    // Phương thức trợ giúp để trích xuất User từ ResultSet
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
