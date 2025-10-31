package com.example.controller;

public class DartScoreCalculator {

    private static final int[] SECTOR_SCORES = {10, 20, 15, 10, 10, 20, 15, 10};
    private static final double INNER_RADIUS = 40;   // pixel
    private static final double MIDDLE_RADIUS = 100; // pixel
    private static final double OUTER_RADIUS = 180;  // pixel

    /**
     * Tính điểm từ tọa độ pixel, tương thích với rotation bảng
     */
    public static int calculateScore(double x, double y, double rotationAngle) {
         y = -y; // lật trục Y (trên UI)

        // 🔁 Xoay điểm NGƯỢC chiều quay của bảng (vì bảng quay thuận)
        double angleRad = Math.toRadians(-rotationAngle);
        double x_rotated = x * Math.cos(angleRad) - y * Math.sin(angleRad);
        double y_rotated = x * Math.sin(angleRad) + y * Math.cos(angleRad);

        // Khoảng cách
        double r = Math.sqrt(x_rotated * x_rotated + y_rotated * y_rotated);
        if (r > OUTER_RADIUS) return 0;
        if (r <= INNER_RADIUS) return 50;
        if (r <= MIDDLE_RADIUS) return 30;

        // Góc tính theo trục Oy hướng lên
        double theta = Math.toDegrees(Math.atan2(y_rotated, x_rotated));
        theta = (360 + 90 - theta) % 360;

        int numSectors = SECTOR_SCORES.length;
        double sectorAngle = 360.0 / numSectors;
        int sectorIndex = (int) (theta / sectorAngle);

        return SECTOR_SCORES[sectorIndex];

    }
}
