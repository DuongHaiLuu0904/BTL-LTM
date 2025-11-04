package com.example.model;

import java.io.Serializable;
import java.util.Date;

public class MatchThrowDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private int throwId;
    private int matchId;
    private int playerId;
    private int throwNumber;

    // Throw parameters
    private double thetaDeg;
    private double phiDeg;
    private double powerPercent;

    // Result data
    private double xHit;
    private double yHit;
    private int score;
    private boolean hitBoard;
    private Date throwTime;

    // Additional info (will be loaded from joins)
    private String playerUsername;

    public MatchThrowDetail() {
    }

    public MatchThrowDetail(int matchId, int playerId, int throwNumber,
            double thetaDeg, double phiDeg, double powerPercent,
            double xHit, double yHit, int score, boolean hitBoard) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.throwNumber = throwNumber;
        this.thetaDeg = thetaDeg;
        this.phiDeg = phiDeg;
        this.powerPercent = powerPercent;
        this.xHit = xHit;
        this.yHit = yHit;
        this.score = score;
        this.hitBoard = hitBoard;
        this.throwTime = new Date();
    }

    // Getters and Setters
    public int getThrowId() {
        return throwId;
    }

    public void setThrowId(int throwId) {
        this.throwId = throwId;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getThrowNumber() {
        return throwNumber;
    }

    public void setThrowNumber(int throwNumber) {
        this.throwNumber = throwNumber;
    }

    public double getThetaDeg() {
        return thetaDeg;
    }

    public void setThetaDeg(double thetaDeg) {
        this.thetaDeg = thetaDeg;
    }

    public double getPhiDeg() {
        return phiDeg;
    }

    public void setPhiDeg(double phiDeg) {
        this.phiDeg = phiDeg;
    }

    public double getPowerPercent() {
        return powerPercent;
    }

    public void setPowerPercent(double powerPercent) {
        this.powerPercent = powerPercent;
    }

    public double getXHit() {
        return xHit;
    }

    public void setXHit(double xHit) {
        this.xHit = xHit;
    }

    public double getYHit() {
        return yHit;
    }

    public void setYHit(double yHit) {
        this.yHit = yHit;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isHitBoard() {
        return hitBoard;
    }

    public void setHitBoard(boolean hitBoard) {
        this.hitBoard = hitBoard;
    }

    public Date getThrowTime() {
        return throwTime;
    }

    public void setThrowTime(Date throwTime) {
        this.throwTime = throwTime;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }

    @Override
    public String toString() {
        return String.format("Throw #%d by Player %d: Score=%d, Hit=%b, Position=(%.2f, %.2f)",
                throwNumber, playerId, score, hitBoard, xHit, yHit);
    }
}
