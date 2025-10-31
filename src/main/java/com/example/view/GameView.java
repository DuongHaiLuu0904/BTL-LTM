
package com.example.view;

import com.example.model.GameMatch;
import com.example.model.Message;
import com.example.model.ThrowResult;
import com.example.model.User;
import com.example.tcp.GameClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameView extends JFrame {
    private GameClient client;
    private User currentUser;
    private GameMatch match;

    private DartBoardPanel dartboardPanel;
    private JLabel player1Label;
    private JLabel player2Label;
    private JLabel turnLabel;
    private JLabel timerLabel;
    private JSlider thetaSlider;   // Góc ném đứng (elevation)
    private JSlider phiSlider;     // Góc ném ngang (horizontal)
    private JSlider powerSlider;
    private JButton throwButton;
    private JButton rotateButton;
    private JButton exitButton;
    
    // Legacy
    private JSlider angleSlider;

    private Timer throwTimer;
    private Timer rotateTimer;
    private int throwTimeLeft = 30;
    private int rotateTimeLeft = 30;
    private boolean canThrow = false;
    private boolean canRotate = false;
    private int timeLeft = 30; // chung cho reset timer mỗi lượt

    public GameView(GameClient client, User currentUser, GameMatch match) {
        this.client = client;
        this.currentUser = currentUser;
        this.match = match;

        setTitle("Game Ném Phi Tiêu - " + currentUser.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        System.out.println("=== GameView Constructor ===");
        System.out.println("Current user ID: " + currentUser.getUserId());
        System.out.println("Match current turn: " + match.getCurrentPlayerId());
        System.out.println("Is my turn: " + (match.getCurrentPlayerId() == currentUser.getUserId()));

        initComponents();
        updateGameState();
        
        // Enable throw cho người chơi đầu tiên (nếu là lượt của mình)
        if (match.getCurrentPlayerId() == currentUser.getUserId() && match.hasThrowsLeft(currentUser.getUserId())) {
            System.out.println("→ Enabling throw at game start");
            enableThrow();
        }

        client.setMessageHandler(this::handleServerMessage);

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

        dartboardPanel = new DartBoardPanel();
        centerPanel.add(dartboardPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Right panel - Controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(200, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        timerLabel = new JLabel("Thời gian: 30s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(timerLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Theta slider (góc ném đứng - elevation angle)
        // Range: -10° to +10° for fine control, default = 0°
        thetaSlider = new JSlider(-10, 10, 0);
        thetaSlider.setMajorTickSpacing(5);
        thetaSlider.setMinorTickSpacing(1);
        thetaSlider.setPaintTicks(true);
        thetaSlider.setPaintLabels(true);
        thetaSlider.setBorder(BorderFactory.createTitledBorder("Góc nâng θ (-10° to +10°)"));
        rightPanel.add(thetaSlider);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Phi slider (góc ném ngang - horizontal angle)
        // Range: -10° to +10° for fine control, default = 0°
        phiSlider = new JSlider(-10, 10, 0);
        phiSlider.setMajorTickSpacing(5);
        phiSlider.setMinorTickSpacing(1);
        phiSlider.setPaintTicks(true);
        phiSlider.setPaintLabels(true);
        phiSlider.setBorder(BorderFactory.createTitledBorder("Góc ngang φ (-10° to +10°)"));
        rightPanel.add(phiSlider);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Power slider
        powerSlider = new JSlider(0, 100, 75);
        powerSlider.setMajorTickSpacing(25);
        powerSlider.setPaintTicks(true);
        powerSlider.setPaintLabels(true);
        powerSlider.setBorder(BorderFactory.createTitledBorder("Lực ném (%)"));
        rightPanel.add(powerSlider);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        final int[] direction = {1}; // 1 tăng, -1 giảm
        Timer timer = new Timer(20, null); // 20ms cho mượt
        timer.addActionListener(e -> {
            int value = powerSlider.getValue();
            value += direction[0];
            if (value >= 100) {
                value = 100;
                direction[0] = -1;
            } else if (value <= 0) {
                value = 0;
                direction[0] = 1;
            }
            powerSlider.setValue(value);
        });

        timer.start();
        // Legacy angle slider (keep for backward compatibility)
        angleSlider = new JSlider(0, 360, 90);
        angleSlider.setVisible(false);

        // Throw button
        throwButton = new JButton("NÉM");
        throwButton.setFont(new Font("Arial", Font.BOLD, 16));
        throwButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        throwButton.setEnabled(false);
        
        rightPanel.add(throwButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        throwButton.addActionListener(e -> {
            timer.stop(); // Dừng slider
            int power = powerSlider.getValue();
            System.out.println("Lực ném hiện tại: " + power + "%");

            handleThrow();
            timer.start(); // Bật lại slider sau khi ném
        });
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
    }

    private void updateGameState() {
        player1Label.setText(String.format("Người chơi 1 - Điểm: %d - Còn: %d lượt",
                match.getPlayer1Score(), match.getPlayer1ThrowsLeft()));
        player2Label.setText(String.format("Người chơi 2 - Điểm: %d - Còn: %d lượt",
                match.getPlayer2Score(), match.getPlayer2ThrowsLeft()));

        boolean isMyTurn = match.getCurrentPlayerId() == currentUser.getUserId();
        boolean hasThrowsLeft = match.hasThrowsLeft(currentUser.getUserId());
        
        System.out.println("--- updateGameState() ---");
        System.out.println("Is my turn: " + isMyTurn + ", Has throws: " + hasThrowsLeft);
        System.out.println("canRotate: " + canRotate + ", canThrow: " + canThrow);
        
        // CHỈ CẬP NHẬT LABEL, KHÔNG tự động enable/disable throw
        // Việc enable throw được quản lý bởi TURN_CHANGED handler
        if (isMyTurn && hasThrowsLeft) {
            turnLabel.setText("LƯỢT của bạn!");
            turnLabel.setForeground(Color.GREEN);
        } else if (isMyTurn && !hasThrowsLeft) {
            turnLabel.setText("Bạn đã hết lượt!");
            turnLabel.setForeground(Color.RED);
        } else if (!hasThrowsLeft) {
            turnLabel.setText("Bạn đã hết lượt!");
            turnLabel.setForeground(Color.RED);
        } else {
            turnLabel.setText("Lượt của đối thủ...");
            turnLabel.setForeground(Color.GRAY);
        }

        dartboardPanel.setRotationAngle(match.getBoardRotation());
        dartboardPanel.repaint();
    }

    private void enableThrow() {
        canThrow = true;
        throwButton.setEnabled(true);
        startThrowTimer();
    }

    private void disableThrow() {
        canThrow = false;
        throwButton.setEnabled(false);
        stopThrowTimer();
    }

    private void startThrowTimer() {
        stopThrowTimer();
        throwTimeLeft = 30;
        timerLabel.setText("Thời gian ném: " + throwTimeLeft + "s");
        throwTimer = new Timer(1000, e -> {
            if (throwTimeLeft <= 0) {
                stopThrowTimer();
                if (canThrow) handleThrow();
                return;
            }
            throwTimeLeft--;
            timerLabel.setText("Thời gian ném: " + throwTimeLeft + "s");
            if (throwTimeLeft <= 5) timerLabel.setForeground(Color.RED);
        });
        throwTimer.start();
    }

    private void stopThrowTimer() {
        if (throwTimer != null) throwTimer.stop();
        timerLabel.setForeground(Color.BLACK);
    }

    private void startRotateTimer() {
        stopRotateTimer();
        rotateTimeLeft = 30;
        timerLabel.setText("Thời gian xoay: " + rotateTimeLeft + "s");
        rotateTimer = new Timer(1000, e -> {
            if (rotateTimeLeft <= 0) {
                stopRotateTimer();
                if (canRotate) {
                    // Hết giờ → tự động không xoay (rotation = 0) và chuyển lượt
                    rotateButton.setEnabled(false);
                    canRotate = false;
                    client.sendMessage(new Message(Message.ROTATE_BOARD, 0));
                }
                return;
            }
            rotateTimeLeft--;
            timerLabel.setText("Thời gian xoay: " + rotateTimeLeft + "s");
            if (rotateTimeLeft <= 5) timerLabel.setForeground(Color.RED);
        });
        rotateTimer.start();
    }

    private void stopRotateTimer() {
        if (rotateTimer != null) rotateTimer.stop();
        timerLabel.setForeground(Color.BLACK);
    }

    private void resetTurnTimer() {
        timeLeft = 30;
        timerLabel.setText("Thời gian: " + timeLeft + "s");
        timerLabel.setForeground(Color.BLACK);
    }

    private void handleThrow() {
        throwButton.setEnabled(false);
        stopThrowTimer();
        canThrow = false;
        
        System.out.println("=== CLIENT: handleThrow() ===");
        System.out.println("Current player ID: " + currentUser.getUserId());
        System.out.println("Match current turn: " + match.getCurrentPlayerId());
        
        // Lấy các tham số từ sliders
        double theta_deg = thetaSlider.getValue();     // Góc nâng (0-90°)
        double phi_deg = phiSlider.getValue();         // Góc ngang (0-360°)
        double power_percent = powerSlider.getValue(); // Power (0-100)
        
        System.out.println("Throw params: theta=" + theta_deg + "°, phi=" + phi_deg + "°, power=" + power_percent + "%");
        
        // Tạo ThrowResult với các tham số đầu vào
        // Server sẽ tính toán vật lý và trả về kết quả
        ThrowResult result = new ThrowResult(
            currentUser.getUserId(),
            theta_deg,
            phi_deg,
            power_percent
        );
        
        // Gửi thông điệp tới server
        client.sendMessage(new Message(Message.THROW_DART, result));
        System.out.println("Message sent to server");
        
        disableThrow();
    }


    private void handleRotate() {
        stopRotateTimer();
        rotateButton.setEnabled(false);
        canRotate = false;
        
        // Hỏi người chơi có muốn xoay bảng không
        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn xoay bảng để gây khó cho đối thủ không?",
                "Xoay bảng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        int rotation = 0;
        
        if (choice == JOptionPane.YES_OPTION) {
            // Cho phép chọn góc xoay
            JSlider rotationSlider = new JSlider(0, 180, 0);
            rotationSlider.setMajorTickSpacing(45);
            rotationSlider.setPaintTicks(true);
            rotationSlider.setPaintLabels(true);
            rotationSlider.setSnapToTicks(true);
            rotationSlider.setBorder(BorderFactory.createTitledBorder("Chọn góc xoay (°)"));

            int result = JOptionPane.showConfirmDialog(this,
                    rotationSlider,
                    "Chọn góc xoay bảng",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            rotation = (result == JOptionPane.OK_OPTION) ? rotationSlider.getValue() : 0;
        }
        
        // Gửi góc xoay (có thể là 0 nếu không xoay)
        client.sendMessage(new Message(Message.ROTATE_BOARD, rotation));
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
                case Message.GAME_START:
                    // Xử lý khi game bắt đầu (trường hợp được gửi lại)
                    match = (GameMatch) message.getData();
                    System.out.println("=== CLIENT: GAME_START received ===");
                    System.out.println("Match current turn: " + match.getCurrentPlayerId());
                    System.out.println("My ID: " + currentUser.getUserId());
                    updateGameState();
                    
                    // Enable throw nếu là lượt của mình
                    if (match.getCurrentPlayerId() == currentUser.getUserId() && match.hasThrowsLeft(currentUser.getUserId())) {
                        System.out.println("→ Enabling throw after GAME_START");
                        enableThrow();
                    }
                    break;
                    
                case Message.THROW_RESULT:
                    ThrowResult result = (ThrowResult) message.getData();
                    
                    // Hiển thị phi tiêu trên bảng
                    // result.getX() và result.getY() đã được chuyển đổi sang pixels
                    // Cần cộng thêm center offset
                    int centerX = dartboardPanel.getWidth() / 2;
                    int centerY = dartboardPanel.getHeight() / 2;
                    
                    if (result.isHitBoard()) {
                        // Thêm phi tiêu vào vị trí (centerX + x, centerY + y)
                        dartboardPanel.addDart(centerX + result.getX(), centerY + result.getY());
                    }

                    if (result.getPlayerId() == currentUser.getUserId()) {
                        String msg = result.isHitBoard() 
                            ? String.format("Bạn ghi được: %d điểm!\nKhoảng cách đến tâm: %.2f cm", 
                                result.getScore(), result.getR() * 100)
                            : "Trượt! Không trúng bảng.";
                        
                        JOptionPane.showMessageDialog(this,
                                msg,
                                "Kết quả ném",
                                JOptionPane.INFORMATION_MESSAGE);

                        canRotate = true;
                        rotateButton.setEnabled(true);
                        startRotateTimer();
                    } else {
                        String msg = result.isHitBoard()
                            ? String.format("Đối thủ ghi được: %d điểm!\nKhoảng cách đến tâm: %.2f cm",
                                result.getScore(), result.getR() * 100)
                            : "Đối thủ trượt! Không trúng bảng.";
                        
                        JOptionPane.showMessageDialog(this,
                                msg,
                                "Kết quả ném",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    break;

                case Message.GAME_STATE:
                    match = (GameMatch) message.getData();
                    System.out.println("=== CLIENT: GAME_STATE received ===");
                    System.out.println("Current turn: " + match.getCurrentPlayerId());
                    System.out.println("My ID: " + currentUser.getUserId());
                    System.out.println("Can rotate: " + canRotate);
                    
                    updateGameState();
                    
                    // *** KHÔNG enable throw button ở đây ***
                    // Vì GAME_STATE được gửi NGAY SAU KHI NÉM
                    // currentPlayerId vẫn là người vừa ném, nhưng họ phải xoay bảng trước
                    // Chỉ enable throw khi nhận TURN_CHANGED (sau khi xoay bảng)
                    break;

                case Message.TURN_CHANGED:
                    match = (GameMatch) message.getData();
                    System.out.println("=== CLIENT: TURN_CHANGED received ===");
                    System.out.println("New current turn: " + match.getCurrentPlayerId());
                    System.out.println("My ID: " + currentUser.getUserId());
                    System.out.println("Is my turn: " + (match.getCurrentPlayerId() == currentUser.getUserId()));
                    
                    stopRotateTimer();
                    resetTurnTimer();
                    updateGameState();
                    
                    // Enable throw nếu đến lượt của mình
                    if (match.getCurrentPlayerId() == currentUser.getUserId() && match.hasThrowsLeft(currentUser.getUserId())) {
                        System.out.println("→ Enabling throw for new turn");
                        enableThrow();
                    }
                    break;

                case Message.GAME_OVER:
                    handleGameOver(message.getData().toString());
                    break;

                case Message.OPPONENT_LEFT:
                    stopThrowTimer();
                    stopRotateTimer();
                    JOptionPane.showMessageDialog(this,
                            message.getData().toString(),
                            "Trận đấu kết thúc",
                            JOptionPane.INFORMATION_MESSAGE);
                    returnToLobby();
                    break;

                case Message.REMATCH_REQUEST:
                    handleRematchRequest(message);
                    dartboardPanel.clearDarts();
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
        stopThrowTimer();
        stopRotateTimer();

        String[] parts = resultData.split(":");
        String result = parts[0];
        int myScore = Integer.parseInt(parts[1]);
        int opponentScore = Integer.parseInt(parts[2]);

        String message;
        if ("WIN".equals(result)) {
            message = String.format("Bạn đã THẮNG!\nĐiểm của bạn: %d\nĐiểm đối thủ: %d", myScore, opponentScore);
        } else if ("LOSS".equals(result)) {
            message = String.format("Bạn đã THUA!\nĐiểm của bạn: %d\nĐiểm đối thủ: %d", myScore, opponentScore);
        } else {
            message = String.format("HÒA!\nĐiểm của bạn: %d\nĐiểm đối thủ: %d", myScore, opponentScore);
        }

        int choice = JOptionPane.showOptionDialog(this,
                message + "\nBạn có muốn chơi lại không?",
                "Trận đấu kết thúc",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Chơi lại", "Về sảnh"},
                "Về sảnh");

        if (choice == 0) {
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
}
