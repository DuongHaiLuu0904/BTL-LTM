package com.example.model;

import java.io.Serializable;

public class ThrowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int playerId;
    
    // Tham số đầu vào từ client
    private double theta_deg;      // Góc ném đứng (elevation angle) - độ
    private double phi_deg;        // Góc ném ngang (horizontal angle) - độ
    private double power_percent;  // Power từ 0 đến 100
    
    // Kết quả tính toán từ server
    private Double t_hit;          // Thời gian chạm bảng (seconds), null nếu không chạm
    private double x_hit;          // Tọa độ X trên bảng (meters)
    private double y_hit;          // Tọa độ Y trên bảng (meters)
    private double r;              // Khoảng cách đến tâm (meters)
    private boolean hitBoard;      // Có trúng bảng không
    private int score;             // Điểm số
    
    // Legacy fields for backward compatibility (pixel coordinates)
    private double x; // X coordinate on dartboard (pixels for display)
    private double y; // Y coordinate on dartboard (pixels for display)
    private double power; // Power level (0.0 to 1.0) - legacy
    private double angle; // Aiming angle in degrees - legacy
    
    // Constructor mặc định
    public ThrowResult() {
    }
    
    // Constructor với đầy đủ thông tin kết quả ném (legacy)
    public ThrowResult(int playerId, double x, double y, int score, double power, double angle) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.score = score;
        this.power = power;
        this.angle = angle;
    }
    
    // Constructor mới với tham số đầu vào từ client
    public ThrowResult(int playerId, double theta_deg, double phi_deg, double power_percent) {
        this.playerId = playerId;
        this.theta_deg = theta_deg;
        this.phi_deg = phi_deg;
        this.power_percent = power_percent;
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
    
    // Getters and Setters cho các trường mới
    
    public double getTheta_deg() {
        return theta_deg;
    }
    
    public void setTheta_deg(double theta_deg) {
        this.theta_deg = theta_deg;
    }
    
    public double getPhi_deg() {
        return phi_deg;
    }
    
    public void setPhi_deg(double phi_deg) {
        this.phi_deg = phi_deg;
    }
    
    public double getPower_percent() {
        return power_percent;
    }
    
    public void setPower_percent(double power_percent) {
        this.power_percent = power_percent;
    }
    
    public Double getT_hit() {
        return t_hit;
    }
    
    public void setT_hit(Double t_hit) {
        this.t_hit = t_hit;
    }
    
    public double getX_hit() {
        return x_hit;
    }
    
    public void setX_hit(double x_hit) {
        this.x_hit = x_hit;
    }
    
    public double getY_hit() {
        return y_hit;
    }
    
    public void setY_hit(double y_hit) {
        this.y_hit = y_hit;
    }
    
    public double getR() {
        return r;
    }
    
    public void setR(double r) {
        this.r = r;
    }
    
    public boolean isHitBoard() {
        return hitBoard;
    }
    
    public void setHitBoard(boolean hitBoard) {
        this.hitBoard = hitBoard;
    }
}
