package com.example.controller;

public class DartScoreCalculator {

    private static final int[] SECTOR_SCORES = {10, 20, 15, 10, 10, 20, 15, 10};
    private static final double INNER_RADIUS = 40;
    private static final double MIDDLE_RADIUS = 100;
    private static final double OUTER_RADIUS = 180;

    public static int calculateScore(double x, double y, double rotationAngle) {
        // Lật trục Y cho đúng hệ toán học
        // y = -y;

        // Bán kính từ tâm
        double r = Math.sqrt(x * x + y * y);
        if (r > OUTER_RADIUS) return 0;
        if (r <= INNER_RADIUS) return 50;
        if (r <= MIDDLE_RADIUS) return 30;

        // ✅ Tính góc (0° hướng lên, tăng theo chiều kim đồng hồ)
        double theta = (450 - Math.toDegrees(Math.atan2(y, x))) % 360;

        // ✅ Điều chỉnh theo góc xoay của bảng
        theta = (theta - rotationAngle + 360) % 360;

        // ✅ Xác định sector
        int numSectors = SECTOR_SCORES.length;
        double sectorAngle = 360.0 / numSectors;
        int sectorIndex = (int) (theta / sectorAngle);

        return SECTOR_SCORES[sectorIndex];
    }
}
