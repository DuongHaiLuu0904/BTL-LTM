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
    private JSlider angleSlider;
    private JSlider powerSlider;
    private JButton throwButton;
    private JButton rotateButton;
    private JButton exitButton;

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

        initComponents();
        updateGameState();

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

        // Power slider
        powerSlider = new JSlider(0, 100, 50);
        powerSlider.setMajorTickSpacing(25);
        powerSlider.setPaintTicks(true);
        powerSlider.setPaintLabels(true);
        powerSlider.setBorder(BorderFactory.createTitledBorder("Lực ném (%)"));
        rightPanel.add(powerSlider);

        // Angle slider
        angleSlider = new JSlider(0, 360, 90);
        angleSlider.setMajorTickSpacing(90);
        angleSlider.setPaintTicks(true);
        angleSlider.setPaintLabels(true);
        angleSlider.setBorder(BorderFactory.createTitledBorder("Góc ném (°)"));
        rightPanel.add(angleSlider);

        // Throw button
        throwButton = new JButton("NÉM");
        throwButton.setFont(new Font("Arial", Font.BOLD, 16));
        throwButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        throwButton.setEnabled(false);
        
        rightPanel.add(throwButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        throwButton.addActionListener(e -> handleThrow());
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
        if (isMyTurn) {
            turnLabel.setText("LƯỢT của bạn!");
            turnLabel.setForeground(Color.GREEN);
            enableThrow();
        } else {
            turnLabel.setText("Lượt của đối thủ...");
            turnLabel.setForeground(Color.GRAY);
            disableThrow();
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
                if (canRotate) handleRotate(); // xoay mặc định 0°
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
    
    
    double powerLevel = powerSlider.getValue() / 100.0;
    double aimAngle = angleSlider.getValue();

    double maxDistance = DartBoardPanel.getOUTER_RADIUS(); // 180
    double distance = powerLevel * maxDistance;
    distance += (Math.random() - 0.5) * 20; // độ sai lệch ngẫu nhiên
    double angleRad = Math.toRadians(aimAngle);
    angleRad += (Math.random() - 0.5) * 0.2; // độ sai lệch góc

    double x = distance * Math.cos(angleRad);
    double y = distance * Math.sin(angleRad);

    // Tính điểm dựa trên bảng bia
    int score = dartboardPanel.calculateScore(x,  y);
//    dartboardPanel.setLastScore(score); // cập nhật điểm mới

    // Tạo kết quả ném
    ThrowResult result = new ThrowResult(
        currentUser.getUserId(),
        x, y,
        score,
        powerLevel,
        aimAngle
    );

    // Gửi thông điệp tới server
    client.sendMessage(new Message(Message.THROW_DART, result));

    // Lưu phi tiêu vào bảng
    dartboardPanel.addDart(x + dartboardPanel.getWidth() / 2,
                           y + dartboardPanel.getHeight() / 2);

    disableThrow();
}


    private void handleRotate() {
        stopRotateTimer();
        rotateButton.setEnabled(false);
        JSlider rotationSlider = new JSlider(0, 180, 0);
        rotationSlider.setMajorTickSpacing(45);
        rotationSlider.setPaintTicks(true);
        rotationSlider.setPaintLabels(true);
        rotationSlider.setSnapToTicks(true);
        rotationSlider.setBorder(BorderFactory.createTitledBorder("Chọn góc xoay (°)"));

        int result = JOptionPane.showConfirmDialog(this,
                rotationSlider,
                "Xoay bảng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        int rotation = (result == JOptionPane.OK_OPTION) ? rotationSlider.getValue() : 0;

        client.sendMessage(new Message(Message.ROTATE_BOARD, rotation));

        canRotate = false;
        
        if (match.getPlayer1ThrowsLeft() <= 0 && match.getPlayer2ThrowsLeft() <= 0) {
            client.sendMessage(new Message(Message.GAME_OVER));
            return;
        }

        client.sendMessage(new Message(Message.TURN_CHANGED, match));
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

                    if (result.getPlayerId() == currentUser.getUserId()) {
                        JOptionPane.showMessageDialog(this,
                                "Bạn ghi được: " + result.getScore() + " điểm!",
                                "Kết quả ném",
                                JOptionPane.INFORMATION_MESSAGE);

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
                    if(match.getCurrentPlayerId() == currentUser.getUserId()){
                        enableThrow();
                    }
                    else{
                        disableThrow();
                    }
                    break;

                case Message.TURN_CHANGED:
                    match = (GameMatch) message.getData();
                    stopRotateTimer();
                    resetTurnTimer();
                    updateGameState();
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
