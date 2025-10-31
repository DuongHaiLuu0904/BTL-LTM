package com.example.controller;

public class DartScoreCalculator {

    private static final int[] SECTOR_SCORES = {10, 20, 15, 10, 10, 20, 15, 10};
    private static final double INNER_RADIUS = 40;
    private static final double MIDDLE_RADIUS = 100;
    private static final double OUTER_RADIUS = 180;

    public static int calculateScore(double x, double y, double rotationAngle) {
        // Lật trục Y để phù hợp hệ tọa độ toán học (Oy hướng lên)
        y = -y;

        // Khi bảng quay thuận chiều kim đồng hồ rotationAngle,
        // ta xoay ngược lại tọa độ phi tiêu để đưa về hệ gốc.
        double angleRad = Math.toRadians(-rotationAngle);
        double x_rotated = x * Math.cos(angleRad) - y * Math.sin(angleRad);
        double y_rotated = x * Math.sin(angleRad) + y * Math.cos(angleRad);

        // Tính bán kính r từ tâm
        double r = Math.sqrt(x_rotated * x_rotated + y_rotated * y_rotated);

        // Xác định điểm theo vòng
        if (r > OUTER_RADIUS) return 0;
        if (r <= INNER_RADIUS) return 50;  // bullseye
        if (r <= MIDDLE_RADIUS) return 30; // vòng giữa

        // Tính góc theo trục Oy hướng lên (0° ở trên, tăng ngược chiều kim đồng hồ)
        double theta = Math.toDegrees(Math.atan2(y_rotated, x_rotated));
        theta = (360 + 90 - theta) % 360; // dịch 0° về hướng lên trên

        int numSectors = SECTOR_SCORES.length;
        double sectorAngle = 360.0 / numSectors;
        int sectorIndex = (int) (theta / sectorAngle);

        return SECTOR_SCORES[sectorIndex];
    }
}
