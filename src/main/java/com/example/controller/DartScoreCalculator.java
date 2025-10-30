package com.example.controller;

/**
 * Lớp tính điểm phi tiêu, tương thích hoàn toàn với DartBoardPanel.
 * 
 * Cấu trúc:
 * - 8 sector (20, 15, 10, 5, 10, 15, 20, 5)
 * - Vùng đỏ (bullseye) = 50 điểm
 * - Vùng xanh = 30 điểm
 * - Ngoài vùng = 0 điểm
 * 
 * Hỗ trợ tính điểm chính xác khi bảng phi tiêu xoay.
 * 
 * @author lecao
 */
public class DartScoreCalculator {
// Phải trùng với DartBoardPanel
    private static final int[] SECTOR_SCORES = {20, 15, 10, 10, 10, 15, 20, 10};
    private static final double INNER_RADIUS = 40;
    private static final double MIDDLE_RADIUS = 100;
    private static final double OUTER_RADIUS = 180;

    /**
     * Tính điểm theo toạ độ hit (đã ở hệ pixel của panel).
     */
    public static int calculateScore(double x, double y, double rotationAngle,
                                     int boardWidth, int boardHeight) {

        double centerX = boardWidth / 2.0;
        double centerY = boardHeight / 2.0;

        // Dịch gốc về tâm
        double dx = x - centerX;
        double dy = y - centerY;

        // Java: trục Y hướng xuống => đảo chiều Y để có hướng "toán học" thuận
        dy = -dy;

        double r = Math.sqrt(dx * dx + dy * dy);
        if (r > OUTER_RADIUS) return 0;

        // Vùng bullseye
        if (r <= INNER_RADIUS) return 50;
        if (r <= MIDDLE_RADIUS) return 30;

        // Tính góc với 0° ở hướng lên (trục Oy âm)
        double theta = Math.toDegrees(Math.atan2(dx, dy)); // đổi chỗ dx,dy để 0° ở hướng lên
        if (theta < 0) theta += 360;

        // Cộng góc xoay bảng (xoay thuận chiều kim đồng hồ)
        theta = (theta + rotationAngle) % 360;

        // Xác định sector
        int numSectors = SECTOR_SCORES.length;
        double sectorAngle = 360.0 / numSectors;
        int sectorIndex = (int) (theta / sectorAngle);
        if (sectorIndex < 0 || sectorIndex >= numSectors) sectorIndex = 0;

        return SECTOR_SCORES[sectorIndex];
    }
}
