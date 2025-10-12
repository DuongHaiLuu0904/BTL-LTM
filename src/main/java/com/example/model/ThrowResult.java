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
    
    public ThrowResult() {
    }
    
    public ThrowResult(int playerId, double x, double y, int score, double power, double angle) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.score = score;
        this.power = power;
        this.angle = angle;
    }
    
    // Getters and Setters
    public int getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public double getPower() {
        return power;
    }
    
    public void setPower(double power) {
        this.power = power;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
}
