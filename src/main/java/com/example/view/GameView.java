package com.example.view;

import com.example.model.GameMatch;
import com.example.model.Message;
import com.example.model.ThrowResult;
import com.example.model.User;
import com.example.tcp.GameClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class GameView extends JFrame {
    private GameClient client;
    private User currentUser;
    private GameMatch match;
    
    private DartboardPanel dartboardPanel;
    private JLabel player1Label;
    private JLabel player2Label;
    private JLabel turnLabel;
    private JLabel timerLabel;
    private JProgressBar powerBar;
    private JButton throwButton;
    private JButton rotateButton;
    private JButton exitButton;
    
    private double aimAngle = 0;
    private double powerLevel = 0.0;
    private Timer powerTimer;
    private Timer turnTimer;
    private int timeLeft = 20;
    private boolean powerIncreasing = true;
    private boolean canThrow = false;
    private boolean canRotate = false;
    
    public GameView(GameClient client, User currentUser, GameMatch match) {
        this.client = client;
        this.currentUser = currentUser;
        this.match = match;
        
        setTitle("Game Ném Phi Tiêu - " + currentUser.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        updateGameState();
        
        // Set message handler
        client = new GameClient(this::handleServerMessage);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Player info
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        player1Label = new JLabel("", SwingConstants.CENTER);
        player1Label.setFont(new Font("Arial", Font.BOLD, 16));
        player1Label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        topPanel.add(player1Label);
        
        player2Label = new JLabel("", SwingConstants.CENTER);
        player2Label.setFont(new Font("Arial", Font.BOLD, 16));
        player2Label.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        topPanel.add(player2Label);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Dartboard
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        turnLabel = new JLabel("", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));
        centerPanel.add(turnLabel, BorderLayout.NORTH);
        
        dartboardPanel = new DartboardPanel();
        centerPanel.add(dartboardPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - Controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(200, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Timer
        timerLabel = new JLabel("Thời gian: 20s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(timerLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Power bar
        JLabel powerLabel = new JLabel("Lực ném:", SwingConstants.CENTER);
        powerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(powerLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        powerBar = new JProgressBar(0, 100);
        powerBar.setOrientation(JProgressBar.VERTICAL);
        powerBar.setPreferredSize(new Dimension(50, 150));
        powerBar.setMaximumSize(new Dimension(50, 150));
        powerBar.setStringPainted(true);
        powerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(powerBar);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Throw button
        throwButton = new JButton("NÉM");
        throwButton.setFont(new Font("Arial", Font.BOLD, 16));
        throwButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        throwButton.setEnabled(false);
        throwButton.addActionListener(e -> handleThrow());
        rightPanel.add(throwButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Rotate button
        rotateButton = new JButton("Xoay bảng");
        rotateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rotateButton.setEnabled(false);
        rotateButton.addActionListener(e -> handleRotate());
        rightPanel.add(rotateButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Exit button
        exitButton = new JButton("Thoát");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> handleExit());
        rightPanel.add(exitButton);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel);
        
        // Mouse listener for aiming
        dartboardPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (canThrow) {
                    int centerX = dartboardPanel.getWidth() / 2;
                    int centerY = dartboardPanel.getHeight() / 2;
                    aimAngle = Math.toDegrees(Math.atan2(e.getY() - centerY, 
                        e.getX() - centerX));
                    dartboardPanel.setAimAngle(aimAngle);
                    dartboardPanel.repaint();
                }
            }
        });
    }
    
    private void updateGameState() {
        // Update player info
        player1Label.setText(String.format("Người chơi 1 - Điểm: %d - Còn: %d lượt",
            match.getPlayer1Score(), match.getPlayer1ThrowsLeft()));
        player2Label.setText(String.format("Người chơi 2 - Điểm: %d - Còn: %d lượt",
            match.getPlayer2Score(), match.getPlayer2ThrowsLeft()));
        
        // Update turn
        boolean isMyTurn = match.getCurrentPlayerId() == currentUser.getUserId();
        if (isMyTurn) {
            turnLabel.setText("LƯỢTcủa bạn!");
            turnLabel.setForeground(Color.GREEN);
            enableTurn();
        } else {
            turnLabel.setText("Lượt của đối thủ...");
            turnLabel.setForeground(Color.GRAY);
            disableTurn();
        }
        
        // Update board rotation
        dartboardPanel.setRotation(match.getBoardRotation());
        dartboardPanel.repaint();
    }
    
    private void enableTurn() {
        canThrow = true;
        throwButton.setEnabled(true);
        startPowerBar();
        startTurnTimer();
    }
    
    private void disableTurn() {
        canThrow = false;
        canRotate = false;
        throwButton.setEnabled(false);
        rotateButton.setEnabled(false);
        stopPowerBar();
        stopTurnTimer();
    }
    
    private void startPowerBar() {
        powerLevel = 0.0;
        powerIncreasing = true;
        
        powerTimer = new Timer(50, e -> {
            if (powerIncreasing) {
                powerLevel += 0.05;
                if (powerLevel >= 1.0) {
                    powerLevel = 1.0;
                    powerIncreasing = false;
                }
            } else {
                powerLevel -= 0.05;
                if (powerLevel <= 0.0) {
                    powerLevel = 0.0;
                    powerIncreasing = true;
                }
            }
            powerBar.setValue((int) (powerLevel * 100));
        });
        powerTimer.start();
    }
    
    private void stopPowerBar() {
        if (powerTimer != null) {
            powerTimer.stop();
            powerBar.setValue(0);
        }
    }
    
    private void startTurnTimer() {
        timeLeft = 20;
        timerLabel.setText("Thời gian: " + timeLeft + "s");
        
        turnTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Thời gian: " + timeLeft + "s");
            
            if (timeLeft <= 5) {
                timerLabel.setForeground(Color.RED);
            }
            
            if (timeLeft <= 0) {
                stopTurnTimer();
                if (canThrow) {
                    // Auto throw with current power
                    handleThrow();
                } else if (canRotate) {
                    // Auto skip rotation
                    handleRotate();
                }
            }
        });
        turnTimer.start();
    }
    
    private void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            timerLabel.setForeground(Color.BLACK);
        }
    }
    
    private void handleThrow() {
        stopPowerBar();
        throwButton.setEnabled(false);
        
        // Calculate throw position based on power and angle
        double maxDistance = 170.0; // Maximum radius on dartboard
        double distance = powerLevel * maxDistance;
        
        // Add some randomness
        distance += (Math.random() - 0.5) * 20;
        double angleRad = Math.toRadians(aimAngle);
        angleRad += (Math.random() - 0.5) * 0.2;
        
        double x = distance * Math.cos(angleRad);
        double y = distance * Math.sin(angleRad);
        
        ThrowResult result = new ThrowResult(currentUser.getUserId(), x, y, 0, 
            powerLevel, aimAngle);
        
        // Send to server
        client.sendMessage(new Message(Message.THROW_DART, result));
        
        // Show on dartboard
        dartboardPanel.addDart(x, y);
        
        canThrow = false;
    }
    
    private void handleRotate() {
        stopTurnTimer();
        
        String[] options = {"0°", "45°", "90°", "135°", "180°", "Không xoay"};
        int choice = JOptionPane.showOptionDialog(this,
            "Chọn góc xoay bảng bia:",
            "Xoay bảng",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[5]);
        
        int rotation = 0;
        if (choice >= 0 && choice < 5) {
            rotation = choice * 45;
        }
        
        client.sendMessage(new Message(Message.ROTATE_BOARD, rotation));
        
        canRotate = false;
        rotateButton.setEnabled(false);
    }
    
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn thoát? Bạn sẽ thua trận đấu này.",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.sendMessage(new Message(Message.EXIT_GAME));
            returnToLobby();
        }
    }
    
    private void handleServerMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case Message.THROW_RESULT:
                    ThrowResult result = (ThrowResult) message.getData();
                    dartboardPanel.addDart(result.getX(), result.getY());
                    
                    // Show score popup
                    if (result.getPlayerId() == currentUser.getUserId()) {
                        JOptionPane.showMessageDialog(this,
                            "Bạn ghi được: " + result.getScore() + " điểm!",
                            "Kết quả ném",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Enable rotate button
                        canRotate = true;
                        rotateButton.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Đối thủ ghi được: " + result.getScore() + " điểm!",
                            "Kết quả ném",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                    
                case Message.GAME_STATE:
                    match = (GameMatch) message.getData();
                    updateGameState();
                    break;
                    
                case Message.TURN_CHANGED:
                    match = (GameMatch) message.getData();
                    dartboardPanel.clearDarts();
                    updateGameState();
                    break;
                    
                case Message.GAME_OVER:
                    handleGameOver(message.getData().toString());
                    break;
                    
                case Message.OPPONENT_LEFT:
                    stopTurnTimer();
                    stopPowerBar();
                    JOptionPane.showMessageDialog(this,
                        message.getData().toString(),
                        "Trận đấu kết thúc",
                        JOptionPane.INFORMATION_MESSAGE);
                    returnToLobby();
                    break;
                    
                case Message.REMATCH_REQUEST:
                    handleRematchRequest(message);
                    break;
                    
                case Message.ERROR:
                    JOptionPane.showMessageDialog(this,
                        message.getData().toString(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });
    }
    
    private void handleGameOver(String resultData) {
        stopTurnTimer();
        stopPowerBar();
        
        String[] parts = resultData.split(":");
        String result = parts[0];
        int myScore = Integer.parseInt(parts[1]);
        int opponentScore = Integer.parseInt(parts[2]);
        
        String message;
        if ("WIN".equals(result)) {
            message = String.format("Bạn đã THẮNG!\n\nĐiểm của bạn: %d\nĐiểm đối thủ: %d\n\nBạn được cộng 3 điểm!",
                myScore, opponentScore);
        } else if ("LOSS".equals(result)) {
            message = String.format("Bạn đã THUA!\n\nĐiểm của bạn: %d\nĐiểm đối thủ: %d",
                myScore, opponentScore);
        } else {
            message = String.format("HÒA!\n\nĐiểm của bạn: %d\nĐiểm đối thủ: %d\n\nCả hai được cộng 1 điểm!",
                myScore, opponentScore);
        }
        
        int choice = JOptionPane.showOptionDialog(this,
            message + "\n\nBạn có muốn chơi lại không?",
            "Trận đấu kết thúc",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Chơi lại", "Về sảnh"},
            "Về sảnh");
        
        if (choice == 0) {
            // Request rematch
            int opponentId = (match.getPlayer1Id() == currentUser.getUserId())
                ? match.getPlayer2Id() : match.getPlayer1Id();
            client.sendMessage(new Message(Message.REMATCH_REQUEST, opponentId));
        } else {
            returnToLobby();
        }
    }
    
    private void handleRematchRequest(Message message) {
        User requester = (User) message.getData();
        
        int response = JOptionPane.showConfirmDialog(this,
            requester.getUsername() + " muốn chơi lại. Bạn có đồng ý không?",
            "Lời mời chơi lại",
            JOptionPane.YES_NO_OPTION);
        
        String responseStr = (response == JOptionPane.YES_OPTION) ? "ACCEPTED" : "REJECTED";
        Message responseMsg = new Message(Message.REMATCH_RESPONSE,
            responseStr, currentUser.getUserId(), message.getSenderId());
        client.sendMessage(responseMsg);
        
        if (response == JOptionPane.NO_OPTION) {
            returnToLobby();
        }
    }
    
    private void returnToLobby() {
        dispose();
        new LobbyView(client, currentUser).setVisible(true);
    }
    
    // Inner class for dartboard panel
    private class DartboardPanel extends JPanel {
        private int rotation = 0;
        private double aimAngle = 0;
        private List<Point2D> darts = new ArrayList<>();
        
        public DartboardPanel() {
            setBackground(Color.DARK_GRAY);
        }
        
        public void setRotation(int rotation) {
            this.rotation = rotation;
        }
        
        public void setAimAngle(double angle) {
            this.aimAngle = angle;
        }
        
        public void addDart(double x, double y) {
            darts.add(new Point2D(x, y));
            repaint();
        }
        
        public void clearDarts() {
            darts.clear();
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(getWidth(), getHeight()) / 2 - 50;
            
            // Draw dartboard
            drawDartboard(g2d, centerX, centerY, radius);
            
            // Draw aiming line
            if (canThrow) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(2));
                double angleRad = Math.toRadians(aimAngle);
                int x2 = centerX + (int) (radius * Math.cos(angleRad));
                int y2 = centerY + (int) (radius * Math.sin(angleRad));
                g2d.drawLine(centerX, centerY, x2, y2);
            }
            
            // Draw darts
            g2d.setColor(Color.YELLOW);
            for (Point2D dart : darts) {
                int x = centerX + (int) dart.x;
                int y = centerY + (int) dart.y;
                g2d.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            }
        }
        
        private void drawDartboard(Graphics2D g2d, int cx, int cy, int radius) {
            // Outer circle (black)
            g2d.setColor(Color.BLACK);
            g2d.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            
            // Main playing area
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(cx - (int)(radius * 0.95), cy - (int)(radius * 0.95), 
                (int)(radius * 1.9), (int)(radius * 1.9));
            
            // Draw segments (simplified - 20 segments)
            for (int i = 0; i < 20; i++) {
                double angle1 = Math.toRadians(i * 18 + rotation - 9);
                double angle2 = Math.toRadians((i + 1) * 18 + rotation - 9);
                
                // Alternate colors
                g2d.setColor(i % 2 == 0 ? Color.WHITE : Color.BLACK);
                
                int[] xPoints = new int[4];
                int[] yPoints = new int[4];
                
                xPoints[0] = cx;
                yPoints[0] = cy;
                xPoints[1] = cx + (int)(radius * 0.95 * Math.cos(angle1));
                yPoints[1] = cy + (int)(radius * 0.95 * Math.sin(angle1));
                xPoints[2] = cx + (int)(radius * 0.95 * Math.cos(angle2));
                yPoints[2] = cy + (int)(radius * 0.95 * Math.sin(angle2));
                xPoints[3] = cx;
                yPoints[3] = cy;
                
                g2d.fillPolygon(xPoints, yPoints, 4);
            }
            
            // Inner bull
            g2d.setColor(Color.GREEN);
            int innerBull = (int)(radius * 0.18);
            g2d.fillOval(cx - innerBull, cy - innerBull, innerBull * 2, innerBull * 2);
            
            // Bullseye
            g2d.setColor(Color.RED);
            int bullseye = (int)(radius * 0.075);
            g2d.fillOval(cx - bullseye, cy - bullseye, bullseye * 2, bullseye * 2);
        }
    }
    
    // Helper class for 2D points
    private static class Point2D {
        double x, y;
        
        Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
