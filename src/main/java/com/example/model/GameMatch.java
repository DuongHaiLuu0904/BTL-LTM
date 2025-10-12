package com.example.model;

import java.io.Serializable;
import java.util.Date;

public class GameMatch implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int matchId;
    private int player1Id;
    private int player2Id;
    private int player1Score;
    private int player2Score;
    private int winnerId;
    private Date startTime;
    private Date endTime;
    private String status; // WAITING, PLAYING, FINISHED, CANCELLED
    
    // Game state
    private int currentPlayerId;
    private int player1ThrowsLeft;
    private int player2ThrowsLeft;
    private int boardRotation; // Rotation angle in degrees
    
    public GameMatch() {
        this.player1ThrowsLeft = 5;
        this.player2ThrowsLeft = 5;
        this.boardRotation = 0;
        this.status = "WAITING";
    }
    
    public GameMatch(int player1Id, int player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1Score = 0;
        this.player2Score = 0;
        this.currentPlayerId = player1Id;
        this.player1ThrowsLeft = 5;
        this.player2ThrowsLeft = 5;
        this.boardRotation = 0;
        this.status = "PLAYING";
        this.startTime = new Date();
    }
    
    // Getters and Setters
    public int getMatchId() {
        return matchId;
    }
    
    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }
    
    public int getPlayer1Id() {
        return player1Id;
    }
    
    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }
    
    public int getPlayer2Id() {
        return player2Id;
    }
    
    public void setPlayer2Id(int player2Id) {
        this.player2Id = player2Id;
    }
    
    public int getPlayer1Score() {
        return player1Score;
    }
    
    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }
    
    public int getPlayer2Score() {
        return player2Score;
    }
    
    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }
    
    public int getWinnerId() {
        return winnerId;
    }
    
    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    public void setCurrentPlayerId(int currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }
    
    public int getPlayer1ThrowsLeft() {
        return player1ThrowsLeft;
    }
    
    public void setPlayer1ThrowsLeft(int player1ThrowsLeft) {
        this.player1ThrowsLeft = player1ThrowsLeft;
    }
    
    public int getPlayer2ThrowsLeft() {
        return player2ThrowsLeft;
    }
    
    public void setPlayer2ThrowsLeft(int player2ThrowsLeft) {
        this.player2ThrowsLeft = player2ThrowsLeft;
    }
    
    public int getBoardRotation() {
        return boardRotation;
    }
    
    public void setBoardRotation(int boardRotation) {
        this.boardRotation = boardRotation;
    }
    
    public void switchPlayer() {
        if (currentPlayerId == player1Id) {
            currentPlayerId = player2Id;
        } else {
            currentPlayerId = player1Id;
        }
    }
    
    public void decrementThrows(int playerId) {
        if (playerId == player1Id) {
            player1ThrowsLeft--;
        } else if (playerId == player2Id) {
            player2ThrowsLeft--;
        }
    }
    
    public boolean isGameOver() {
        return player1ThrowsLeft <= 0 && player2ThrowsLeft <= 0;
    }
    
    public void addScore(int playerId, int score) {
        if (playerId == player1Id) {
            player1Score += score;
        } else if (playerId == player2Id) {
            player2Score += score;
        }
    }
}
