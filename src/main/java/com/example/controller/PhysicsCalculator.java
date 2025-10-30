package com.example.controller;

import com.example.model.ThrowResult;
import java.util.Random;

/**
 * PhysicsCalculator - Tính toán vật lý chính xác cho game ném phi tiêu 3D
 * 
 * Quy ước hệ tọa độ:
 * - Người chơi đứng tại gốc (0, 0, 0)
 * - Trục Z dương: hướng về phía bảng
 * - Trục X: ngang (trái/phải)
 * - Trục Y: dọc (lên/xuống)
 * - Tâm bảng tại (0, 0, D)
 */
public class PhysicsCalculator {
    
    // Hằng số vật lý
    private static final double GRAVITY = 9.81; // m/s²
    
    // Cấu hình mặc định
    private static final double DEFAULT_BOARD_DISTANCE = 2.37; // meters (khoảng cách chuẩn dart)
    private static final double DEFAULT_BOARD_RADIUS = 0.225; // meters (22.5 cm - bán kính bảng)
    private static final double DEFAULT_V0_MIN = 5.0; // m/s - vận tốc tối thiểu
    private static final double DEFAULT_V0_MAX = 15.0; // m/s - vận tốc tối đa
    
    private Random random = new Random();

    /**
     * Tính toán tọa độ va chạm của phi tiêu trên bảng
     *
     * @param throwResult - Dữ liệu đầu vào (theta, phi, power)
     * @param boardDistance - Khoảng cách tới bảng (m)
     * @param boardRadius - Bán kính bảng (m)
     * @param boardAngle_deg - Góc xoay bảng (°)
     * @param v0_min - Vận tốc min
     * @param v0_max - Vận tốc max
     * @param enableNoise - Thêm nhiễu ngẫu nhiên?
     * @param noiseStdDeg - Độ lệch chuẩn của nhiễu (°)
     * @return ThrowResult đã cập nhật x, y, r, hitBoard
     */
    public ThrowResult calculateThrow(
            ThrowResult throwResult,
            double boardDistance,
            double boardRadius,
            double boardAngle_deg,
            double v0_min,
            double v0_max,
            boolean enableNoise,
            double noiseStdDeg) {

        double theta_deg = throwResult.getTheta_deg();
        double phi_deg = throwResult.getPhi_deg();
        double power_percent = throwResult.getPower_percent();

        // Thêm nhiễu ngẫu nhiên
        if (enableNoise) {
            theta_deg += random.nextGaussian() * noiseStdDeg;
            phi_deg += random.nextGaussian() * noiseStdDeg;
        }

        // Chuyển sang radian
        double theta = Math.toRadians(theta_deg);
        double phi = Math.toRadians(phi_deg);

        // Tính vận tốc ban đầu
        double v0 = v0_min + (power_percent / 100.0) * (v0_max - v0_min);

        // Phân rã vận tốc theo trục
        double v0x = v0 * Math.cos(theta) * Math.sin(phi);
        double v0y = v0 * Math.sin(theta);
        double v0z = v0 * Math.cos(theta) * Math.cos(phi);

        // Không bay về phía bảng
        if (v0z <= 0) {
            throwResult.setHitBoard(false);
            throwResult.setX_hit(0);
            throwResult.setY_hit(0);
            throwResult.setR(Double.MAX_VALUE);
            return throwResult;
        }

        // Tính thời gian chạm bảng
        double t_hit = boardDistance / v0z;

        // Tính vị trí chạm bảng
        double x_hit = v0x * t_hit;
        double y_hit = v0y * t_hit - 0.5 * GRAVITY * t_hit * t_hit;

        // Xoay ngược bảng để tính đúng tọa độ
        double angleRad = Math.toRadians(boardAngle_deg);
        double x_rotated = x_hit * Math.cos(angleRad) - y_hit * Math.sin(angleRad);
        double y_rotated = x_hit * Math.sin(angleRad) + y_hit * Math.cos(angleRad);

        // Khoảng cách đến tâm
        double r = Math.sqrt(x_rotated * x_rotated + y_rotated * y_rotated);
        boolean hitBoard = r <= boardRadius;

        // Cập nhật kết quả
        throwResult.setT_hit(t_hit);
        throwResult.setX_hit(x_rotated);
        throwResult.setY_hit(y_rotated);
        throwResult.setR(r);
        throwResult.setHitBoard(hitBoard);

        // Chuyển sang pixel cho giao diện
        double pixelScale = 180.0 / DEFAULT_BOARD_RADIUS; // 180px tương ứng 0.225m
        throwResult.setX(x_rotated * pixelScale);
        throwResult.setY(-y_rotated * pixelScale); // đảo trục Y để hiển thị đúng

        return throwResult;
    }

    /**
     * Tính với cấu hình mặc định
     */
    public ThrowResult calculateThrow(ThrowResult throwResult, double boardAngle_deg) {
        return calculateThrow(
                throwResult,
                DEFAULT_BOARD_DISTANCE,
                DEFAULT_BOARD_RADIUS,
                boardAngle_deg,
                DEFAULT_V0_MIN,
                DEFAULT_V0_MAX,
                true,
                0.5
        );
    }
}
