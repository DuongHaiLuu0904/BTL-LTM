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
    
    private java.util.List<FlyingDart> flyingDarts = new java.util.ArrayList<>();
    private Image dartImageBlue;
    private Image dartImageRed;
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

    // Đặt ID của người chơi 1 để xác định màu phi tiêu
    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public DartBoardPanel() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.WHITE);
        // Load ảnh phi tiêu
        dartImageBlue = new ImageIcon(getClass().getResource("/blue.png")).getImage();
        dartImageRed = new ImageIcon(getClass().getResource("/red.png")).getImage();
        Timer timer = new Timer(1000/60, e -> {
            updateFlyingDarts();
            repaint();
        });
        timer.start();
    }
    

    // PHẦN HIỂN THỊ
    public void addDart(double x, double y, int playerId) {
        darts.add(new DartPoint((int) Math.round(x), (int) Math.round(y), playerId));
        repaint();
    }

     // Thêm phi tiêu bay
    public void addFlyingDart(double x_hit, double y_hit, int playerId, double tHit) {
        int centerX = getWidth() / 2;
        int centerY = getHeight()/2;
        int startX = centerX;
        int startY = getHeight() - 50; 
        int endX = centerX + (int) x_hit;
        int endY = centerY + (int) y_hit;

        FlyingDart dart = new FlyingDart(startX, startY, endX, endY, playerId, tHit);
        flyingDarts.add(dart);
    }

    private void updateFlyingDarts(){
        List<FlyingDart> finished = new ArrayList<>();
        for(FlyingDart dart: flyingDarts){
            dart.update();
            if(dart.isFinished()){
                addDart(dart.getCurrentX(), dart.getCurrentY(), dart.getPlayerId());
                finished.add(dart);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                darts.add(new DartPoint(dart.getCurrentX() + centerX, dart.getCurrentY() + centerY, dart.getPlayerId()));
            }
        }
        flyingDarts.removeAll(finished);
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

        for(FlyingDart dart : flyingDarts) {
            // Vẽ đường bay
            g2.setColor(dart.getPlayerId() == player1Id ? new Color(0, 100, 255, 120) : new Color(255, 0, 0, 120));
            g2.setStroke(new BasicStroke(2));
            Point prev = null;
            for(Point p : dart.getTrail()) {
                if(prev != null) g2.drawLine(prev.x, prev.y, p.x, p.y);
                prev = p;
            }

            // Vẽ phi tiêu
            int size = 20;
            int x = dart.getCurrentX() - size/2;
            int y = dart.getCurrentY() - size/2;
            Image img = (dart.getPlayerId() == player1Id ? dartImageBlue : dartImageRed);
            g2.drawImage(img, x, y, size, size, null);
        }
        
        // Xoay bảng quanh tâm
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
        g2.setColor(new Color(50, 255, 50));
        g2.fillOval(centerX - MIDDLE_RADIUS, centerY - MIDDLE_RADIUS, MIDDLE_RADIUS * 2, MIDDLE_RADIUS * 2);
        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_RADIUS * 2, INNER_RADIUS * 2);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(centerX - OUTER_RADIUS, centerY - OUTER_RADIUS, OUTER_RADIUS * 2, OUTER_RADIUS * 2);
        g2.drawOval(centerX - MIDDLE_RADIUS, centerY - MIDDLE_RADIUS, MIDDLE_RADIUS * 2, MIDDLE_RADIUS * 2);
        g2.drawOval(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_RADIUS * 2, INNER_RADIUS * 2);

        // Vẽ số điểm (giữ số thẳng đứng)

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < numSectors; i++) {
            double theta = Math.toRadians(-90 - i * angleStep - angleStep / 2);
            int r = (OUTER_RADIUS + MIDDLE_RADIUS) / 2;
            int x = (int) (centerX + r * Math.cos(theta));
            int y = (int) (centerY + r * Math.sin(theta));

            String score = String.valueOf(SECTOR_SCORES[i]);
            int textWidth = fm.stringWidth(score);
            int textHeight = fm.getAscent();

            Graphics2D g2Copy = (Graphics2D) g2.create();
            g2Copy.translate(x, y);
            g2Copy.rotate(Math.toRadians(-rotationAngle));
            g2Copy.drawString(score, -textWidth / 2, textHeight / 2);
            g2Copy.dispose();
        }
        
        // Vẽ các phi tiêu đã cắm trên bảng
        for (DartPoint dart : darts) {
            int size = 20;
            int x = dart.x - size/2;
            int y = dart.y - size/2;
            Image img = (dart.playerId == player1Id) ? dartImageBlue : dartImageRed;
            g2.drawImage(img, x, y, size, size, null);
        }
    }

    private Color getSectorColor(int i) {
        switch (i % 4) {
            case 0: return new Color(255, 215, 0);//vàng
            case 1: return new Color(0, 120, 215);//xanh dương
            case 2: return new Color(255, 128, 0);//cam
            case 3: return new Color(245, 245, 245);//xám
            default: return Color.GRAY;
        }
    }

    
}
