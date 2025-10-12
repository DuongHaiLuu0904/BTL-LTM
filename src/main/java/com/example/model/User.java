package com.example.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    private String username;
    private String password;
    private int totalScore;
    private int totalWins;
    private int totalLosses;
    private int totalDraws;
    private boolean isOnline;
    private boolean isBusy;
    
    public User() {
    }
    
    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.totalScore = 0;
        this.totalWins = 0;
        this.totalLosses = 0;
        this.totalDraws = 0;
        this.isOnline = false;
        this.isBusy = false;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public int getTotalWins() {
        return totalWins;
    }
    
    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }
    
    public int getTotalLosses() {
        return totalLosses;
    }
    
    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }
    
    public int getTotalDraws() {
        return totalDraws;
    }
    
    public void setTotalDraws(int totalDraws) {
        this.totalDraws = totalDraws;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public boolean isBusy() {
        return isBusy;
    }
    
    public void setBusy(boolean busy) {
        isBusy = busy;
    }
    
    public String getStatus() {
        if (isBusy) {
            return "Đang bận";
        } else if (isOnline) {
            return "Đang rỗi";
        } else {
            return "Offline";
        }
    }
    
    @Override
    public String toString() {
        return username + " - Điểm: " + totalScore + " - " + getStatus();
    }
}
