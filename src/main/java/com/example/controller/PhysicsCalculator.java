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
    
    // Các vùng điểm trên bảng phi tiêu (đơn vị: meters)
    private static final double BULLSEYE_RADIUS = 0.0127; // 12.7mm - tâm chính
    private static final double BULL_RADIUS = 0.0318; // 31.8mm - vòng bull
    private static final double TRIPLE_INNER = 0.099; // 99mm
    private static final double TRIPLE_OUTER = 0.107; // 107mm
    private static final double DOUBLE_INNER = 0.162; // 162mm
    private static final double DOUBLE_OUTER = 0.170; // 170mm
    
    // Các segment tiêu chuẩn trên bảng phi tiêu (bắt đầu từ 0° và đi theo chiều kim đồng hồ)
    private static final int[] SEGMENTS = {6, 13, 4, 18, 1, 20, 5, 12, 9, 14, 11, 8, 16, 7, 19, 3, 17, 2, 15, 10};
    
    private Random random;
    
    public PhysicsCalculator() {
        this.random = new Random();
    }
    
    /**
     * Tính toán kết quả ném phi tiêu với đầy đủ vật lý 3D
     * 
     * @param throwResult - Object chứa thông tin đầu vào (theta_deg, phi_deg, power_percent)
     * @param boardDistance - Khoảng cách đến bảng (meters)
     * @param boardRadius - Bán kính bảng (meters)
     * @param boardAngle_deg - Góc xoay bảng (degrees)
     * @param v0_min - Vận tốc tối thiểu (m/s)
     * @param v0_max - Vận tốc tối đa (m/s)
     * @param enableNoise - Có thêm nhiễu ngẫu nhiên không
     * @param noiseStdDeg - Độ lệch chuẩn của nhiễu (degrees)
     * @return ThrowResult với đầy đủ thông tin tính toán
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
        
        // Lấy tham số đầu vào
        double theta_deg = throwResult.getTheta_deg();
        double phi_deg = throwResult.getPhi_deg();
        double power_percent = throwResult.getPower_percent();
        
        // Thêm nhiễu nếu được kích hoạt
        if (enableNoise) {
            theta_deg += random.nextGaussian() * noiseStdDeg;
            phi_deg += random.nextGaussian() * noiseStdDeg;
        }
        
        // Chuyển đổi góc sang radian
        double theta = Math.toRadians(theta_deg);
        double phi = Math.toRadians(phi_deg);
        
        // Tính vận tốc ban đầu từ power
        double v0 = v0_min + (power_percent / 100.0) * (v0_max - v0_min);
        
        // Phân rá vận tốc theo các trục
        double v0x = v0 * Math.cos(theta) * Math.sin(phi);
        double v0y = v0 * Math.sin(theta);
        double v0z = v0 * Math.cos(theta) * Math.cos(phi);
        
        // Kiểm tra xem phi tiêu có bay về phía bảng không
        if (v0z <= 0) {
            // Không bay về phía bảng
            throwResult.setT_hit(null);
            throwResult.setX_hit(0);
            throwResult.setY_hit(0);
            throwResult.setR(Double.MAX_VALUE);
            throwResult.setHitBoard(false);
            throwResult.setScore(0);
            return throwResult;
        }
        
        // Tính thời gian chạm bảng
        double t_hit = boardDistance / v0z;
        
        // Tính tọa độ chạm bảng
        double x_hit = v0x * t_hit;
        double y_hit = v0y * t_hit - 0.5 * GRAVITY * t_hit * t_hit;
        
        // Xử lý xoay bảng (quay ngược hệ tọa độ)
        double boardAngleRad = Math.toRadians(-boardAngle_deg);
        double x_rotated = x_hit * Math.cos(boardAngleRad) - y_hit * Math.sin(boardAngleRad);
        double y_rotated = x_hit * Math.sin(boardAngleRad) + y_hit * Math.cos(boardAngleRad);
        
        // Tính khoảng cách đến tâm
        double r = Math.sqrt(x_rotated * x_rotated + y_rotated * y_rotated);
        
        // Kiểm tra trúng bảng
        boolean hitBoard = r <= boardRadius;
        
        // Tính điểm
        int score = 0;
        if (hitBoard) {
            score = calculateScore(x_rotated, y_rotated, r);
        }
        
        // Cập nhật kết quả
        throwResult.setT_hit(t_hit);
        throwResult.setX_hit(x_hit);
        throwResult.setY_hit(y_hit);
        throwResult.setR(r);
        throwResult.setHitBoard(hitBoard);
        throwResult.setScore(score);
        
        // Cập nhật tọa độ legacy để hiển thị (chuyển từ meters sang pixels)
        // Board radius = 0.225m → OUTER_RADIUS = 180 pixels
        // Scale: 180 pixels / 0.225 m = 800 pixels/meter
        // Nhưng để hiển thị tốt hơn, dùng scale phù hợp với DartBoardPanel
        double pixelScale = 180.0 / DEFAULT_BOARD_RADIUS; // 180 pixels / 0.225m = 800
        throwResult.setX(x_hit * pixelScale);
        throwResult.setY(-y_hit * pixelScale); // Đảo dấu Y vì màn hình Y đi xuống
        
        return throwResult;
    }
    
    /**
     * Tính toán kết quả ném với cấu hình mặc định
     */
    public ThrowResult calculateThrow(ThrowResult throwResult, double boardAngle_deg) {
        return calculateThrow(
            throwResult,
            DEFAULT_BOARD_DISTANCE,
            DEFAULT_BOARD_RADIUS,
            boardAngle_deg,
            DEFAULT_V0_MIN,
            DEFAULT_V0_MAX,
            true,  // enableNoise
            0.5    // noiseStdDeg - độ lệch nhỏ
        );
    }
    
    /**
     * Tính điểm dựa trên tọa độ và khoảng cách đến tâm
     */
    private int calculateScore(double x, double y, double r) {
        // Bullseye (tâm chính)
        if (r <= BULLSEYE_RADIUS) {
            return 50;
        }
        
        // Bull (vòng bull)
        if (r <= BULL_RADIUS) {
            return 25;
        }
        
        // Nằm ngoài bảng
        if (r > DOUBLE_OUTER) {
            return 0;
        }
        
        // Tính góc để xác định segment
        double angle = Math.toDegrees(Math.atan2(y, x));
        if (angle < 0) {
            angle += 360;
        }
        
        // Điều chỉnh góc bắt đầu (segment 6 bắt đầu từ -9° đến 9°)
        // Mỗi segment rộng 18°
        angle = (angle + 9) % 360;
        int segmentIndex = (int)(angle / 18.0);
        
        if (segmentIndex < 0 || segmentIndex >= 20) {
            segmentIndex = 0;
        }
        
        int baseValue = SEGMENTS[segmentIndex];
        
        // Triple ring
        if (r >= TRIPLE_INNER && r <= TRIPLE_OUTER) {
            return baseValue * 3;
        }
        
        // Double ring
        if (r >= DOUBLE_INNER && r <= DOUBLE_OUTER) {
            return baseValue * 2;
        }
        
        // Single value (vùng giữa triple và bull, hoặc giữa double và triple)
        return baseValue;
    }
    
}
