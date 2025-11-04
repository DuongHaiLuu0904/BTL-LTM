package com.example.view;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class FlyingDart {

    private final double startX, startY; // điểm xuất phát
    private final double endX, endY;     // điểm đích (x_hit, y_hit)
    private final int playerId;          // ID người chơi
    private double progress;             // 0 -> 1
    private final double speedFactor;    // tốc độ bay dựa trên thời gian t_hit
    private java.util.List<Point> trail = new ArrayList<>();
    
    public FlyingDart(double startX, double startY, double endX, double endY, int playerId, double tHit) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.playerId = playerId;
        this.progress = 0;
        this.speedFactor = 1.0 / (tHit * 60); // giả sử Timer chạy 60 FPS
        this.trail = new ArrayList<>();
    }

    public void update() {
        progress += speedFactor;
        if(progress > 1) progress = 1;
        trail.add(new Point(getCurrentX(), getCurrentY()));
    }
    
    public java.util.List<Point> getTrail() {
        return trail;
    }
    
    public int getCurrentX() {
        return (int) (startX + (endX - startX) * progress);
    }

    public int getCurrentY() {
        return (int) (startY + (endY - startY) * progress);
    }

    public boolean isFinished() {
        return progress >= 1.0;
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }
    
    public int getPreviousX(double progressStep) {
        return (int) (startX + (endX - startX) * Math.max(0, progress - progressStep));
    }

    public int getPreviousY(double progressStep) {
        return (int) (startY + (endY - startY) * Math.max(0, progress - progressStep));
    }

}
