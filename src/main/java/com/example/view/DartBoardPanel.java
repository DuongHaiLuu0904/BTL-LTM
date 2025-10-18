package com.example.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DartBoardPanel extends JPanel {
    private static final int[] SECTOR_SCORES = {20, 15, 10, 10, 10, 15, 20, 10};
    private static final int INNER_RADIUS = 40;
    private static final int MIDDLE_RADIUS = 100;
    private static final int OUTER_RADIUS = 180;

    private int lastScore = 0;
    private double rotationAngle = 0;
    private final Random random = new Random();

    // ✅ Danh sách lưu tất cả phi tiêu
    private final List<Point> darts = new ArrayList<>();

    public DartBoardPanel() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.WHITE);
    }

//    public void throwDart(double angleDeg, double power) {
//        int centerX = getWidth() / 2;
//        int centerY = getHeight() / 2;
//
//        double distance = (power / 100.0) * OUTER_RADIUS;
//        double angleRad = Math.toRadians(angleDeg - 90);
//
//        double hitX = centerX + distance * Math.cos(angleRad);
//        double hitY = centerY + distance * Math.sin(angleRad);
//
//        // ✅ Lưu phi tiêu vào danh sách
//        addDart(hitX, hitY);
//
//        repaint();
//        
//        JOptionPane.showMessageDialog(this,
//                "🎯 Bạn ném được: " + lastScore + " điểm!",
//                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
//
////        // ✅ Sau khi ném xong, xoay bảng ngẫu nhiên
////        rotationAngle += random.nextInt(30) + 15;
////        rotationAngle %= 360;
////        repaint();
//    }

    // ✅ Thêm 1 phi tiêu vào danh sách
    public void addDart(double x, double y) {
        darts.add(new Point((int) x, (int) y));
        repaint();
    }

    // ✅ Xóa toàn bộ phi tiêu
    public void clearDarts() {
        darts.clear();
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // ✅ Xoay bảng quanh tâm
        g2.translate(centerX, centerY);
        g2.rotate(Math.toRadians(rotationAngle));
        g2.translate(-centerX, -centerY);

        int numSectors = SECTOR_SCORES.length;
        double angleStep = 360.0 / numSectors;

        for (int i = 0; i < numSectors; i++) {
            double startAngle = -90 - i * angleStep;
            g2.setColor(getSectorColor(i));
            g2.fillArc(centerX - OUTER_RADIUS, centerY - OUTER_RADIUS,
                    OUTER_RADIUS * 2, OUTER_RADIUS * 2,
                    (int) startAngle, (int) -angleStep);
        }

        g2.setColor(Color.GREEN);
        g2.fillOval(centerX - MIDDLE_RADIUS, centerY - MIDDLE_RADIUS,
                MIDDLE_RADIUS * 2, MIDDLE_RADIUS * 2);

        g2.setColor(Color.RED);
        g2.fillOval(centerX - INNER_RADIUS, centerY - INNER_RADIUS,
                INNER_RADIUS * 2, INNER_RADIUS * 2);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(centerX - OUTER_RADIUS, centerY - OUTER_RADIUS, OUTER_RADIUS * 2, OUTER_RADIUS * 2);
        g2.drawOval(centerX - MIDDLE_RADIUS, centerY - MIDDLE_RADIUS, MIDDLE_RADIUS * 2, MIDDLE_RADIUS * 2);
        g2.drawOval(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_RADIUS * 2, INNER_RADIUS * 2);

        // Vẽ số điểm (cũng xoay theo bảng)
        g2.translate(centerX, centerY);
        g2.rotate(Math.toRadians(-rotationAngle)); // giữ số đứng thẳng
        g2.translate(-centerX, -centerY);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        for (int i = 0; i < numSectors; i++) {
            double theta = Math.toRadians(-90 - i * angleStep - angleStep / 2);
            int r = (OUTER_RADIUS + MIDDLE_RADIUS) / 2;
            int x = (int) (centerX + r * Math.cos(theta)) - 10;
            int y = (int) (centerY + r * Math.sin(theta)) + 8;
            g2.drawString(String.valueOf(SECTOR_SCORES[i]), x, y);
        }

        // ✅ Quay ngược lại hệ toạ độ để vẽ phi tiêu không xoay
        g2.translate(centerX, centerY);
        g2.rotate(Math.toRadians(-rotationAngle));
        g2.translate(-centerX, -centerY);

        // ✅ Vẽ tất cả phi tiêu đã ném
        g2.setColor(Color.BLACK);
        for (Point p : darts) {
            double dx = p.x - centerX;
            double dy = p.y - centerY;
            
            double rad = Math.toRadians(rotationAngle);
            double rotatedX = dx * Math.cos(rad) - dy * Math.sin(rad);
            double rotatedY = dy * Math.sin(rad) + dy * Math.cos(rad);
            
            int drawX = (int)(centerX + rotatedX);
            int drawY = (int)(centerY + rotatedY);
            g2.fillOval(drawX - 5, drawY - 5, 10, 10);
        }

        // Điểm hiện tại
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        
    }

    private Color getSectorColor(int i) {
        switch (i % 4) {
            case 0: return Color.YELLOW;
            case 1: return Color.BLUE;
            case 2: return Color.ORANGE;
            case 3: return Color.WHITE;
        }
        return Color.GRAY;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public static int getOUTER_RADIUS() {
        return OUTER_RADIUS;
    }
    
 
    public int calculateScore(double x, double y) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Tính toạ độ tương đối với tâm
        double dx = x - centerX;
        double dy = y - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Nếu ném ra ngoài bia => 0 điểm
        if (distance > OUTER_RADIUS) {
            return 0;
        }

        // Xác định sector
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        angle = (angle + rotationAngle) % 360; // bù xoay bảng
        if (angle < 0) angle += 360;

        int numSectors = SECTOR_SCORES.length;
        double sectorAngle = 360.0 / numSectors;
        int sectorIndex = (int) (angle / sectorAngle) % numSectors;
        int baseScore = SECTOR_SCORES[sectorIndex];

        // Xác định vòng tròn
        if (distance <= INNER_RADIUS) {
            return baseScore * 3; // Vùng đỏ trung tâm: nhân 3
        } else if (distance <= MIDDLE_RADIUS) {
            return baseScore * 2; // Vùng xanh ngoài trung tâm: nhân 2
        } else {
            return 0; // Vùng trắng ngoài cùng: điểm cơ bản
        }
    }
    public void rotateBoard(double deltaAngle) {
        this.rotationAngle += deltaAngle;
        this.rotationAngle %= 360; // giữ trong 0-360°
        repaint(); // vẽ lại bảng
    }

}
