package com.example.model;

/**
 * Data Transfer Object for GameMatch with player usernames
 * Extended version of GameMatch with additional display information
 */
public class GameMatchDTO extends GameMatch {
    private String player1Username;
    private String player2Username;
    private String winnerUsername;
    
    public GameMatchDTO() {
        super();
    }
    
    public GameMatchDTO(GameMatch match) {
        this.setMatchId(match.getMatchId());
        this.setPlayer1Id(match.getPlayer1Id());
        this.setPlayer2Id(match.getPlayer2Id());
        this.setPlayer1Score(match.getPlayer1Score());
        this.setPlayer2Score(match.getPlayer2Score());
        this.setWinnerId(match.getWinnerId());
        this.setCurrentPlayerId(match.getCurrentPlayerId());
        this.setPlayer1ThrowsLeft(match.getPlayer1ThrowsLeft());
        this.setPlayer2ThrowsLeft(match.getPlayer2ThrowsLeft());
        this.setBoardRotation(match.getBoardRotation());
        this.setStatus(match.getStatus());
        this.setStartTime(match.getStartTime());
        this.setEndTime(match.getEndTime());
    }
    
    // Getters and Setters for usernames
    public String getPlayer1Username() {
        return player1Username;
    }
    
    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }
    
    public String getPlayer2Username() {
        return player2Username;
    }
    
    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }
    
    public String getWinnerUsername() {
        return winnerUsername;
    }
    
    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }
    
    /**
     * Get opponent username for a specific player
     * @param currentUserId The current user's ID
     * @return The opponent's username
     */
    public String getOpponentUsername(int currentUserId) {
        if (currentUserId == getPlayer1Id()) {
            return player2Username;
        } else if (currentUserId == getPlayer2Id()) {
            return player1Username;
        }
        return "Unknown";
    }
    
    /**
     * Get user's score
     * @param currentUserId The current user's ID
     * @return The user's score
     */
    public int getUserScore(int currentUserId) {
        if (currentUserId == getPlayer1Id()) {
            return getPlayer1Score();
        } else if (currentUserId == getPlayer2Id()) {
            return getPlayer2Score();
        }
        return 0;
    }
    
    /**
     * Get opponent's score
     * @param currentUserId The current user's ID
     * @return The opponent's score
     */
    public int getOpponentScore(int currentUserId) {
        if (currentUserId == getPlayer1Id()) {
            return getPlayer2Score();
        } else if (currentUserId == getPlayer2Id()) {
            return getPlayer1Score();
        }
        return 0;
    }
    
    /**
     * Get match result for a specific player
     * @param currentUserId The current user's ID
     * @return "WIN", "LOSE", or "DRAW"
     */
    public String getMatchResult(int currentUserId) {
        if (getWinnerId() == 0) {
            return "DRAW";
        } else if (getWinnerId() == currentUserId) {
            return "WIN";
        } else {
            return "LOSE";
        }
    }
    
    /**
     * Format match score as "userScore - opponentScore"
     * @param currentUserId The current user's ID
     * @return Formatted score string
     */
    public String getFormattedScore(int currentUserId) {
        return getUserScore(currentUserId) + " - " + getOpponentScore(currentUserId);
    }
}
