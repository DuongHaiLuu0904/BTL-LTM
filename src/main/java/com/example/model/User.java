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

    // Constructor mặc định
    public User() {
    }

    // Constructor với tham số khởi tạo user với thông tin cơ bản
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
    // Lấy ID của người dùng
    public int getUserId() {
        return userId;
    }

    // Đặt ID cho người dùng
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Lấy tên đăng nhập
    public String getUsername() {
        return username;
    }

    // Đặt tên đăng nhập
    public void setUsername(String username) {
        this.username = username;
    }

    // Lấy mật khẩu
    public String getPassword() {
        return password;
    }

    // Đặt mật khẩu
    public void setPassword(String password) {
        this.password = password;
    }

    // Lấy tổng điểm
    public int getTotalScore() {
        return totalScore;
    }

    // Đặt tổng điểm
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    // Lấy số trận thắng
    public int getTotalWins() {
        return totalWins;
    }

    // Đặt số trận thắng
    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    // Lấy số trận thua
    public int getTotalLosses() {
        return totalLosses;
    }

    // Đặt số trận thua
    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    // Lấy số trận hòa
    public int getTotalDraws() {
        return totalDraws;
    }

    // Đặt số trận hòa
    public void setTotalDraws(int totalDraws) {
        this.totalDraws = totalDraws;
    }

    // Kiểm tra người dùng có đang online không
    public boolean isOnline() {
        return isOnline;
    }

    // Đặt trạng thái online
    public void setOnline(boolean online) {
        isOnline = online;
    }

    // Kiểm tra người dùng có đang bận không
    public boolean isBusy() {
        return isBusy;
    }

    // Đặt trạng thái bận
    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    // Lấy trạng thái hiển thị của người dùng
    public String getStatus() {
        if (isBusy) {
            return "Đang bận";
        } else if (isOnline) {
            return "Đang rỗi";
        } else {
            return "Offline";
        }
    }

    // Chuyển đổi thông tin user thành chuỗi để hiển thị
    @Override
    public String toString() {
        return username + " - Điểm: " + totalScore + " - " + getStatus();
    }
}
