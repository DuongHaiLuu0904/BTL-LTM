package com.example.model;

import java.io.Serializable;

public class ThrowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int playerId;
    private double x; // X coordinate on dartboard
    private double y; // Y coordinate on dartboard
    private int score;
    private double power; // Power level (0.0 to 1.0)
    private double angle; // Aiming angle in degrees
    
    // Constructor mặc định
    public ThrowResult() {
    }
    
    // Constructor với đầy đủ thông tin kết quả ném
    public ThrowResult(int playerId, double x, double y, int score, double power, double angle) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.score = score;
        this.power = power;
        this.angle = angle;
    }
    
    // Getters and Setters
    // Lấy ID người chơi
    public int getPlayerId() {
        return playerId;
    }
    
    // Đặt ID người chơi
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
    // Lấy tọa độ X trên bảng
    public double getX() {
        return x;
    }
    
    // Đặt tọa độ X trên bảng
    public void setX(double x) {
        this.x = x;
    }
    
    // Lấy tọa độ Y trên bảng
    public double getY() {
        return y;
    }
    
    // Đặt tọa độ Y trên bảng
    public void setY(double y) {
        this.y = y;
    }
    
    // Lấy điểm số đạt được
    public int getScore() {
        return score;
    }
    
    // Đặt điểm số đạt được
    public void setScore(int score) {
        this.score = score;
    }
    
    // Lấy mức độ lực ném
    public double getPower() {
        return power;
    }
    
    // Đặt mức độ lực ném
    public void setPower(double power) {
        this.power = power;
    }
    
    // Lấy góc ngắm
    public double getAngle() {
        return angle;
    }
    
    // Đặt góc ngắm
    public void setAngle(double angle) {
        this.angle = angle;
    }
}
