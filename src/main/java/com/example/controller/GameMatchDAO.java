package com.example.controller;

import com.example.model.GameMatch;
import com.example.model.GameMatchDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameMatchDAO {
    private Connection connection;

    // Constructor khởi tạo kết nối database
    public GameMatchDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // Tạo trận đấu mới
    public int createMatch(int player1Id, int player2Id) {
        String sql = "INSERT INTO game_matches (player1_id, player2_id, player1_score, player2_score, " +
                "current_player_id, player1_throws_left, player2_throws_left, board_rotation, " +
                "status, start_time) VALUES (?, ?, 0, 0, ?, 5, 5, 0, 'PLAYING', ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, player1Id);
            stmt.setInt(2, player2Id);
            stmt.setInt(3, player1Id);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Lấy trận đấu theo ID
    public GameMatch getMatchById(int matchId) {
        String sql = "SELECT * FROM game_matches WHERE match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMatchFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy trận đấu đang hoạt động cho người chơi
    public GameMatch getActiveMatchForPlayer(int playerId) {
        String sql = "SELECT * FROM game_matches WHERE (player1_id = ? OR player2_id = ?) " +
                "AND status = 'PLAYING'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, playerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMatchFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Cập nhật trạng thái trận đấu
    public boolean updateMatchState(GameMatch match) {
        String sql = "UPDATE game_matches SET player1_score = ?, player2_score = ?, " +
                "current_player_id = ?, player1_throws_left = ?, player2_throws_left = ?, " +
                "board_rotation = ? WHERE match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, match.getPlayer1Score());
            stmt.setInt(2, match.getPlayer2Score());
            stmt.setInt(3, match.getCurrentPlayerId());
            stmt.setInt(4, match.getPlayer1ThrowsLeft());
            stmt.setInt(5, match.getPlayer2ThrowsLeft());
            stmt.setInt(6, match.getBoardRotation());
            stmt.setInt(7, match.getMatchId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // End match
    public boolean endMatch(int matchId, int winnerId, String status) {
        String sql = "UPDATE game_matches SET winner_id = ?, status = ?, end_time = ? " +
                "WHERE match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (winnerId > 0) {
                stmt.setInt(1, winnerId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, status);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(4, matchId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get match history for player
    public List<GameMatch> getMatchHistoryForPlayer(int playerId, int limit) {
        List<GameMatch> matches = new ArrayList<>();
        String sql = "SELECT * FROM game_matches WHERE (player1_id = ? OR player2_id = ?) " +
                "AND status = 'FINISHED' ORDER BY end_time DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, playerId);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(extractMatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches;
    }

    // Hủy trận đấu
    public boolean cancelMatch(int matchId) {
        return endMatch(matchId, 0, "CANCELLED");
    }

    public List<GameMatchDTO> getMatchHistoryWithNames(int playerId, int limit) {
        List<GameMatchDTO> matches = new ArrayList<>();
        String sql = "SELECT gm.*, " +
                "u1.username as player1_username, " +
                "u2.username as player2_username, " +
                "u3.username as winner_username " +
                "FROM game_matches gm " +
                "JOIN users u1 ON gm.player1_id = u1.user_id " +
                "JOIN users u2 ON gm.player2_id = u2.user_id " +
                "LEFT JOIN users u3 ON gm.winner_id = u3.user_id " +
                "WHERE (gm.player1_id = ? OR gm.player2_id = ?) " +
                "AND gm.status = 'FINISHED' " +
                "ORDER BY gm.end_time DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, playerId);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                matches.add(extractMatchDTOFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches;
    }

    public GameMatchDTO getMatchWithNames(int matchId) {
        String sql = "SELECT gm.*, " +
                "u1.username as player1_username, " +
                "u2.username as player2_username, " +
                "u3.username as winner_username " +
                "FROM game_matches gm " +
                "JOIN users u1 ON gm.player1_id = u1.user_id " +
                "JOIN users u2 ON gm.player2_id = u2.user_id " +
                "LEFT JOIN users u3 ON gm.winner_id = u3.user_id " +
                "WHERE gm.match_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMatchDTOFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Phương thức trợ giúp để trích xuất GameMatch từ ResultSet
    private GameMatch extractMatchFromResultSet(ResultSet rs) throws SQLException {
        GameMatch match = new GameMatch();
        match.setMatchId(rs.getInt("match_id"));
        match.setPlayer1Id(rs.getInt("player1_id"));
        match.setPlayer2Id(rs.getInt("player2_id"));
        match.setPlayer1Score(rs.getInt("player1_score"));
        match.setPlayer2Score(rs.getInt("player2_score"));

        int winnerId = rs.getInt("winner_id");
        if (!rs.wasNull()) {
            match.setWinnerId(winnerId);
        }

        match.setCurrentPlayerId(rs.getInt("current_player_id"));
        match.setPlayer1ThrowsLeft(rs.getInt("player1_throws_left"));
        match.setPlayer2ThrowsLeft(rs.getInt("player2_throws_left"));
        match.setBoardRotation(rs.getInt("board_rotation"));
        match.setStatus(rs.getString("status"));
        match.setStartTime(rs.getTimestamp("start_time"));

        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            match.setEndTime(endTime);
        }

        return match;
    }

    private GameMatchDTO extractMatchDTOFromResultSet(ResultSet rs) throws SQLException {
        GameMatchDTO matchDTO = new GameMatchDTO();
        matchDTO.setMatchId(rs.getInt("match_id"));
        matchDTO.setPlayer1Id(rs.getInt("player1_id"));
        matchDTO.setPlayer2Id(rs.getInt("player2_id"));
        matchDTO.setPlayer1Score(rs.getInt("player1_score"));
        matchDTO.setPlayer2Score(rs.getInt("player2_score"));

        int winnerId = rs.getInt("winner_id");
        if (!rs.wasNull()) {
            matchDTO.setWinnerId(winnerId);
        }

        matchDTO.setCurrentPlayerId(rs.getInt("current_player_id"));
        matchDTO.setPlayer1ThrowsLeft(rs.getInt("player1_throws_left"));
        matchDTO.setPlayer2ThrowsLeft(rs.getInt("player2_throws_left"));
        matchDTO.setBoardRotation(rs.getInt("board_rotation"));
        matchDTO.setStatus(rs.getString("status"));
        matchDTO.setStartTime(rs.getTimestamp("start_time"));

        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            matchDTO.setEndTime(endTime);
        }

        // Set usernames
        matchDTO.setPlayer1Username(rs.getString("player1_username"));
        matchDTO.setPlayer2Username(rs.getString("player2_username"));

        String winnerUsername = rs.getString("winner_username");
        if (winnerUsername != null) {
            matchDTO.setWinnerUsername(winnerUsername);
        }

        return matchDTO;
    }
}
