package com.example.controller;

public class DartScoreCalculator {

    private static final int[] SECTOR_SCORES = {10, 15, 20, 10, 10, 15, 20, 10};
    private static final double INNER_RADIUS = 40;   // pixel
    private static final double MIDDLE_RADIUS = 100; // pixel
    private static final double OUTER_RADIUS = 180;  // pixel

    /**
     * Tính điểm từ tọa độ pixel, tương thích với rotation bảng
     */
    public static int calculateScore(double x, double y, double rotationAngle) {
        y = -y;
        // Xoay ngược điểm theo rotation bảng
        // double angleRad = Math.toRadians(rotationAngle);
        double x_rot = x * Math.cos(rotationAngle) + y * Math.sin(rotationAngle);
        double y_rot = -x * Math.sin(rotationAngle) + y * Math.cos(rotationAngle);

        double r = Math.sqrt(x_rot * x_rot + y_rot * y_rot);

        if (r > OUTER_RADIUS) return 0;
        if (r <= INNER_RADIUS) return 50;
        if (r <= MIDDLE_RADIUS) return 30;

        double theta = Math.toDegrees(Math.atan2(x_rot, y_rot));
        if (theta < 0) theta += 360;

        int numSectors = SECTOR_SCORES.length;
        double sectorAngle = 360.0 / numSectors;
        int sectorIndex = (int) (theta / sectorAngle);
        if (sectorIndex < 0 || sectorIndex >= numSectors) sectorIndex = 0;

        return SECTOR_SCORES[sectorIndex];
    }
}
