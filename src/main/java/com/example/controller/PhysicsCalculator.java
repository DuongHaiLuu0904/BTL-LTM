package com.example.controller;

import com.example.model.ThrowResult;
import java.util.Random;

/**
 * PhysicsCalculator - Tính toán vật lý chính xác cho game ném phi tiêu 3D
 * 
 * Quy ước hệ tọa độ:
 * - Người chơi đứng tại gốc (0,0,0)
 * - Trục Z dương: hướng về bảng
 * - Trục X: ngang (trái/phải)
 * - Trục Y: dọc (lên/xuống)
 * - Tâm bảng tại (0,0,D)
 */
public class PhysicsCalculator {
    
    private static final double GRAVITY = 9.81; // m/s²
    private static final double DEFAULT_BOARD_DISTANCE = 2.37; // m
    private static final double DEFAULT_BOARD_RADIUS = 0.225; // m
    private static final double DEFAULT_V0_MIN = 5.0; // m/s
    private static final double DEFAULT_V0_MAX = 15.0; // m/s
    private Random random = new Random();

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

        if (enableNoise) {
            theta_deg += random.nextGaussian() * noiseStdDeg;
            phi_deg += random.nextGaussian() * noiseStdDeg;
        }

        double theta = Math.toRadians(theta_deg);
        double phi = Math.toRadians(phi_deg);

        double v0 = v0_min + (power_percent / 100.0) * (v0_max - v0_min);
        double v0x = v0 * Math.cos(theta) * Math.sin(phi);
        double v0y = v0 * Math.sin(theta);
        double v0z = v0 * Math.cos(theta) * Math.cos(phi);

        if (v0z <= 0) {
            throwResult.setHitBoard(false);
            throwResult.setX_hit(0);
            throwResult.setY_hit(0);
            throwResult.setR(Double.MAX_VALUE);
            return throwResult;
        }

        double t_hit = boardDistance / v0z;
        double x_hit = v0x * t_hit;
        double y_hit = v0y * t_hit - 0.5 * GRAVITY * t_hit * t_hit;

        double angleRad = Math.toRadians(boardAngle_deg);
        double x_rotated = x_hit * Math.cos(angleRad) - y_hit * Math.sin(angleRad);
        double y_rotated = x_hit * Math.sin(angleRad) + y_hit * Math.cos(angleRad);

//        double r = Math.sqrt(x_rotated * x_rotated + y_rotated * y_rotated);
          double r = Math.sqrt(x_hit * x_hit + y_hit * y_hit);
          boolean hitBoard = r <= boardRadius;

            throwResult.setT_hit(t_hit);
//            throwResult.setX_hit(x_rotated);
//            throwResult.setY_hit(y_rotated);
          
//        throwResult.setR(r);
        throwResult.setHitBoard(hitBoard);

        // Pixel scale cho panel
        double pixelScale = 180.0 / DEFAULT_BOARD_RADIUS; // OUTER_RADIUS = 180px
        throwResult.setX(x_hit * pixelScale);
        throwResult.setY(y_hit * pixelScale); 
        throwResult.setX_hit(x_rotated * pixelScale);
        throwResult.setY_hit(y_rotated * pixelScale);

        return throwResult;
    }

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
