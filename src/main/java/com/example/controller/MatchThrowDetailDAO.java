package com.example.controller;

import com.example.model.MatchThrowDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchThrowDetailDAO {
    private Connection connection;

    public MatchThrowDetailDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean saveThrowDetail(MatchThrowDetail detail) {
        String sql = "INSERT INTO match_throw_details " +
                "(match_id, player_id, throw_number, theta_deg, phi_deg, power_percent, " +
                "x_hit, y_hit, score, hit_board, throw_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, detail.getMatchId());
            stmt.setInt(2, detail.getPlayerId());
            stmt.setInt(3, detail.getThrowNumber());
            stmt.setDouble(4, detail.getThetaDeg());
            stmt.setDouble(5, detail.getPhiDeg());
            stmt.setDouble(6, detail.getPowerPercent());
            stmt.setDouble(7, detail.getXHit());
            stmt.setDouble(8, detail.getYHit());
            stmt.setInt(9, detail.getScore());
            stmt.setBoolean(10, detail.isHitBoard());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    detail.setThrowId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<MatchThrowDetail> getThrowDetailsByMatch(int matchId) {
        List<MatchThrowDetail> details = new ArrayList<>();
        String sql = "SELECT mtd.*, u.username as player_username " +
                "FROM match_throw_details mtd " +
                "JOIN users u ON mtd.player_id = u.user_id " +
                "WHERE mtd.match_id = ? " +
                "ORDER BY mtd.throw_number ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                details.add(extractThrowDetailFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public List<MatchThrowDetail> getThrowDetailsByPlayerInMatch(int matchId, int playerId) {
        List<MatchThrowDetail> details = new ArrayList<>();
        String sql = "SELECT mtd.*, u.username as player_username " +
                "FROM match_throw_details mtd " +
                "JOIN users u ON mtd.player_id = u.user_id " +
                "WHERE mtd.match_id = ? AND mtd.player_id = ? " +
                "ORDER BY mtd.throw_number ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.setInt(2, playerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                details.add(extractThrowDetailFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public List<MatchThrowDetail> getThrowDetailsByPlayer(int playerId, int limit) {
        List<MatchThrowDetail> details = new ArrayList<>();
        String sql = "SELECT mtd.*, u.username as player_username " +
                "FROM match_throw_details mtd " +
                "JOIN users u ON mtd.player_id = u.user_id " +
                "WHERE mtd.player_id = ? " +
                "ORDER BY mtd.throw_time DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                details.add(extractThrowDetailFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public int getNextThrowNumber(int matchId, int playerId) {
        String sql = "SELECT COALESCE(MAX(throw_number), 0) + 1 as next_number " +
                "FROM match_throw_details " +
                "WHERE match_id = ? AND player_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.setInt(2, playerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("next_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public boolean deleteThrowDetailsByMatch(int matchId) {
        String sql = "DELETE FROM match_throw_details WHERE match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int[] getPlayerMatchStats(int matchId, int playerId) {
        String sql = "SELECT COUNT(*) as total_throws, " +
                "SUM(score) as total_score, " +
                "AVG(score) as avg_score, " +
                "MAX(score) as max_score, " +
                "SUM(CASE WHEN hit_board = true THEN 1 ELSE 0 END) as hits " +
                "FROM match_throw_details " +
                "WHERE match_id = ? AND player_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.setInt(2, playerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new int[] {
                        rs.getInt("total_throws"),
                        rs.getInt("total_score"),
                        (int) rs.getDouble("avg_score"),
                        rs.getInt("max_score"),
                        rs.getInt("hits")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[] { 0, 0, 0, 0, 0 };
    }

    private MatchThrowDetail extractThrowDetailFromResultSet(ResultSet rs) throws SQLException {
        MatchThrowDetail detail = new MatchThrowDetail();
        detail.setThrowId(rs.getInt("throw_id"));
        detail.setMatchId(rs.getInt("match_id"));
        detail.setPlayerId(rs.getInt("player_id"));
        detail.setThrowNumber(rs.getInt("throw_number"));
        detail.setThetaDeg(rs.getDouble("theta_deg"));
        detail.setPhiDeg(rs.getDouble("phi_deg"));
        detail.setPowerPercent(rs.getDouble("power_percent"));
        detail.setXHit(rs.getDouble("x_hit"));
        detail.setYHit(rs.getDouble("y_hit"));
        detail.setScore(rs.getInt("score"));
        detail.setHitBoard(rs.getBoolean("hit_board"));
        detail.setThrowTime(rs.getTimestamp("throw_time"));

        // Set username if available
        try {
            detail.setPlayerUsername(rs.getString("player_username"));
        } catch (SQLException e) {
            // Column might not exist in all queries
        }

        return detail;
    }
}
