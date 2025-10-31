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
    private int dartboardHeight; // Height of the dartboard panel in pixels
    private int dartboardWidth; // Width of the dartboard panel in pixels
    // Constructor mặc định khởi tạo trạng thái chờ
    public GameMatch() {
        this.player1ThrowsLeft = 5;
        this.player2ThrowsLeft = 5;
        this.boardRotation = 0;
        this.status = "WAITING";
    }
    
    // Constructor khởi tạo trận đấu với 2 người chơi
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
    // Lấy ID trận đấu
    public int getMatchId() {
        return matchId;
    }
    
    // Đặt ID trận đấu
    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }
    
    // Lấy ID người chơi 1
    public int getPlayer1Id() {
        return player1Id;
    }
    
    // Đặt ID người chơi 1
    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }
    
    // Lấy ID người chơi 2
    public int getPlayer2Id() {
        return player2Id;
    }
    
    // Đặt ID người chơi 2
    public void setPlayer2Id(int player2Id) {
        this.player2Id = player2Id;
    }
    
    // Lấy điểm người chơi 1
    public int getPlayer1Score() {
        return player1Score;
    }
    
    // Đặt điểm người chơi 1
    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }
    
    // Lấy điểm người chơi 2
    public int getPlayer2Score() {
        return player2Score;
    }
    
    // Đặt điểm người chơi 2
    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }
    
    // Lấy ID người thắng
    public int getWinnerId() {
        return winnerId;
    }
    
    // Đặt ID người thắng
    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }
    
    // Lấy thời gian bắt đầu
    public Date getStartTime() {
        return startTime;
    }
    
    // Đặt thời gian bắt đầu
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    // Lấy thời gian kết thúc
    public Date getEndTime() {
        return endTime;
    }
    
    // Đặt thời gian kết thúc
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    // Lấy trạng thái trận đấu
    public String getStatus() {
        return status;
    }
    
    // Đặt trạng thái trận đấu
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Lấy ID người chơi hiện tại
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    // Đặt ID người chơi hiện tại
    public void setCurrentPlayerId(int currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }
    
    // Lấy số lần ném còn lại của người chơi 1
    public int getPlayer1ThrowsLeft() {
        return player1ThrowsLeft;
    }
    
    // Đặt số lần ném còn lại của người chơi 1
    public void setPlayer1ThrowsLeft(int player1ThrowsLeft) {
        this.player1ThrowsLeft = player1ThrowsLeft;
    }
    
    // Lấy số lần ném còn lại của người chơi 2
    public int getPlayer2ThrowsLeft() {
        return player2ThrowsLeft;
    }
    
    // Đặt số lần ném còn lại của người chơi 2
    public void setPlayer2ThrowsLeft(int player2ThrowsLeft) {
        this.player2ThrowsLeft = player2ThrowsLeft;
    }
    
    // Lấy góc xoay của bảng
    public int getBoardRotation() {
        return boardRotation;
    }
    
    // Đặt góc xoay của bảng
    public void setBoardRotation(int boardRotation) {
        this.boardRotation = boardRotation;
    }
    
    // Chuyển lượt chơi sang người chơi khác
    public void switchPlayer() {
        if (currentPlayerId == player1Id) {
            setCurrentPlayerId( player2Id);
        } else {
            setCurrentPlayerId(player1Id);
        }
    }
    
    // Giảm số lần ném còn lại của người chơi
    public void decrementThrows(int playerId) {
        if (playerId == player1Id) {
            player1ThrowsLeft--;
        } else if (playerId == player2Id) {
            player2ThrowsLeft--;
        }
    }
    
    // Kiểm tra xem trò chơi đã kết thúc chưa
    public boolean isGameOver() {
        return player1ThrowsLeft <= 0 && player2ThrowsLeft <= 0;
    }
    
    // Cộng điểm cho người chơi
    public void addScore(int playerId, int score) {
        if (playerId == player1Id) {
            player1Score += score;
        } else if (playerId == player2Id) {
            player2Score += score;
        }
    }
    
//    public void throwDart(int playerId, int score) {
//        addScore(playerId, score);
//        decrementThrows(playerId);
////        switchPlayer();
//    }
    
    public boolean hasThrowsLeft(int playerId) {
        return playerId == player1Id ? player1ThrowsLeft > 0 : player2ThrowsLeft > 0;
    }
    
    public boolean isPlayerInMatch(int playerId) {
        return playerId == player1Id || playerId == player2Id;
    }

    public int getDartboardHeight() {
        return dartboardHeight;
    }

    public void setDartboardHeight(int dartboardHeight) {
        this.dartboardHeight = dartboardHeight;
    }

    public int getDartboardWidth() {
        return dartboardWidth;
    }

    public void setDartboardWidth(int dartboardWidth) {
        this.dartboardWidth = dartboardWidth;
    }
}
