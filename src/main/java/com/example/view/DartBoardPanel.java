package com.example.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DartBoardPanel extends JPanel {
    // Bảng điểm đơn giản chia 8 sector
    private static final int[] SECTOR_SCORES = { 10, 20, 15, 10, 10, 20, 15, 10 };
    private static final int INNER_RADIUS = 40;
    private static final int MIDDLE_RADIUS = 100;
    private static final int OUTER_RADIUS = 180;

    private double rotationAngle = 0;
    private final List<DartPoint> darts = new ArrayList<>();
    private int player1Id = -1; // ID của người chơi 1 (để xác định màu)
    
    // Class để lưu thông tin phi tiêu
    private static class DartPoint {
        int x, y;
        int playerId;
        
        DartPoint(int x, int y, int playerId) {
            this.x = x;
            this.y = y;
            this.playerId = playerId;
        }
    }

    public DartBoardPanel() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.WHITE);
    }
    
    // Đặt ID của người chơi 1 để xác định màu phi tiêu
    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    // PHẦN HIỂN THỊ
    public void addDart(double x, double y, int playerId) {
        darts.add(new DartPoint((int) Math.round(x), (int) Math.round(y), playerId));
        repaint();
    }

    public void clearDarts() {
        darts.clear();
        repaint();
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle % 360;
        if (this.rotationAngle < 0)
            this.rotationAngle += 360;
        repaint();
    }

    public void rotateBoard(double deltaAngle) {
        this.rotationAngle = (this.rotationAngle + deltaAngle) % 360;
        if (this.rotationAngle < 0)
            this.rotationAngle += 360; // tránh âm
        repaint();
    }

    public static int getOUTER_RADIUS() {
        return OUTER_RADIUS;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int numSectors = SECTOR_SCORES.length;
        double angleStep = 360.0 / numSectors;

        // ✅ Xoay bảng quanh tâm
        g2.translate(centerX, centerY);
        g2.rotate(Math.toRadians(rotationAngle));
        g2.translate(-centerX, -centerY);

        // Vẽ các sector ngoài cùng
        for (int i = 0; i < numSectors; i++) {
            double startAngle = -90 - i * angleStep;
            g2.setColor(getSectorColor(i));
            g2.fillArc(centerX - OUTER_RADIUS, centerY - OUTER_RADIUS,
                    OUTER_RADIUS * 2, OUTER_RADIUS * 2,
                    (int) startAngle, (int) -angleStep);
        }

        // Vẽ các vòng tròn
        g2.setColor(Color.GREEN);
        g2.fillOval(centerX - MIDDLE_RADIUS, centerY - MIDDLE_RADIUS, MIDDLE_RADIUS * 2, MIDDLE_RADIUS * 2);
        g2.setColor(Color.RED);
        g2.fillOval(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_RADIUS * 2, INNER_RADIUS * 2);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(centerX - OUTER_RADIUS, centerY - OUTER_RADIUS, OUTER_RADIUS * 2, OUTER_RADIUS * 2);
        g2.drawOval(centerX - MIDDLE_RADIUS, centerY - MIDDLE_RADIUS, MIDDLE_RADIUS * 2, MIDDLE_RADIUS * 2);
        g2.drawOval(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_RADIUS * 2, INNER_RADIUS * 2);

//        // Vẽ số điểm (giữ số thẳng đứng)
//        g2.translate(centerX, centerY);
//        g2.rotate(Math.toRadians(-rotationAngle));
//        g2.translate(-centerX, -centerY);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        for (int i = 0; i < numSectors; i++) {
            double theta = Math.toRadians(-90 - i * angleStep - angleStep / 2);
            int r = (OUTER_RADIUS + MIDDLE_RADIUS) / 2;
            int x = (int) (centerX + r * Math.cos(theta)) - 10;
            int y = (int) (centerY + r * Math.sin(theta)) + 8;
            g2.drawString(String.valueOf(SECTOR_SCORES[i]), x, y);
        }

//        // Vẽ phi tiêu (sau khi quay ngược lại)
//        g2.translate(centerX, centerY);
//        g2.rotate(Math.toRadians(rotationAngle));
//        g2.translate(-centerX, -centerY);

        // Vẽ phi tiêu với màu theo người chơi
        for (DartPoint dart : darts) {
            // Player 1: Blue, Player 2: Red
            if (dart.playerId == player1Id) {
                g2.setColor(Color.BLUE);
            } else {
                g2.setColor(Color.RED);
            }
            g2.fillOval(dart.x - 5, dart.y - 5, 10, 10);
            // Vẽ viền đen để phi tiêu rõ hơn
            g2.setColor(Color.BLACK);
            g2.drawOval(dart.x - 5, dart.y - 5, 10, 10);
        }
    }

    private Color getSectorColor(int i) {
        switch (i % 4) {
            case 0: return Color.YELLOW;
            case 1: return Color.BLUE;
            case 2: return Color.ORANGE;
            case 3: return Color.WHITE;
            default: return Color.GRAY;
        }
    }
}