package com.example.view;

import com.example.model.Message;
import com.example.model.User;
import com.example.tcp.GameClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LobbyView extends JFrame {
    private GameClient client;
    private User currentUser;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel userInfoLabel;
    private JButton refreshButton;
    private JButton challengeButton;
    private JButton leaderboardButton;
    private JButton logoutButton;
    private Timer refreshTimer;
    
    public LobbyView(GameClient client, User currentUser) {
        this.client = client;
        this.currentUser = currentUser;
        
        setTitle("Sảnh chờ - " + currentUser.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        
        // Set message handler for this view
        client.setMessageHandler(this::handleServerMessage);
        
        // Request online users immediately
        requestOnlineUsers();
        
        // Auto refresh every 5 seconds
        refreshTimer = new Timer(5000, e -> requestOnlineUsers());
        refreshTimer.start();
        
        // Add window closing listener
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleLogout();
            }
        });
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - User info
        JPanel topPanel = new JPanel(new BorderLayout());
        userInfoLabel = new JLabel(String.format(
            "Người chơi: %s | Điểm: %d | Thắng: %d | Thua: %d | Hòa: %d",
            currentUser.getUsername(),
            currentUser.getTotalScore(),
            currentUser.getTotalWins(),
            currentUser.getTotalLosses(),
            currentUser.getTotalDraws()
        ));
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(userInfoLabel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - User list
        JPanel centerPanel = new JPanel(new BorderLayout());
        JLabel listLabel = new JLabel("DANH SÁCH NGƯỜI CHƠI ONLINE");
        listLabel.setFont(new Font("Arial", Font.BOLD, 16));
        listLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(listLabel, BorderLayout.NORTH);
        
        // Table (5 columns, last one hidden for userId)
        String[] columnNames = {"Tên người chơi", "Tổng điểm", "Thắng", "Trạng thái", "ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        // Hide the ID column (index 4)
        userTable.getColumnModel().getColumn(4).setMinWidth(0);
        userTable.getColumnModel().getColumn(4).setMaxWidth(0);
        userTable.getColumnModel().getColumn(4).setWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> requestOnlineUsers());
        bottomPanel.add(refreshButton);
        
        challengeButton = new JButton("Thách đấu");
        challengeButton.addActionListener(e -> handleChallenge());
        bottomPanel.add(challengeButton);
        
        leaderboardButton = new JButton("Bảng xếp hạng");
        leaderboardButton.addActionListener(e -> showLeaderboard());
        bottomPanel.add(leaderboardButton);
        
        logoutButton = new JButton("Đăng xuất");
        logoutButton.addActionListener(e -> handleLogout());
        bottomPanel.add(logoutButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void requestOnlineUsers() {
        if (client != null) {
            client.sendMessage(new Message(Message.GET_ONLINE_USERS));
        }
    }
    
    private void handleChallenge() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn người chơi để thách đấu!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedUsername = (String) tableModel.getValueAt(selectedRow, 0);
        if (selectedUsername.equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, 
                "Bạn không thể thách đấu chính mình!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String status = (String) tableModel.getValueAt(selectedRow, 3);
        if ("Đang bận".equals(status)) {
            JOptionPane.showMessageDialog(this, 
                "Người chơi này đang bận!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get user ID from username (stored in table)
        int opponentId = (int) tableModel.getValueAt(selectedRow, 4); // Hidden column
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có muốn thách đấu " + selectedUsername + "?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.sendMessage(new Message(Message.CHALLENGE_REQUEST, opponentId));
        }
    }
    
    private void showLeaderboard() {
        client.sendMessage(new Message(Message.GET_LEADERBOARD));
    }
    
    private void handleLogout() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.sendMessage(new Message(Message.LOGOUT));
            client.disconnect();
            dispose();
            new LoginView().setVisible(true);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleServerMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case Message.ONLINE_USERS_LIST:
                    updateUserList((List<User>) message.getData());
                    break;
                    
                case Message.CHALLENGE_REQUEST:
                    handleChallengeRequest(message);
                    break;
                    
                case Message.CHALLENGE_REJECTED:
                    JOptionPane.showMessageDialog(this, 
                        message.getData().toString(), 
                        "Thách đấu bị từ chối", JOptionPane.INFORMATION_MESSAGE);
                    break;
                    
                case Message.GAME_START:
                    if (refreshTimer != null) {
                        refreshTimer.stop();
                    }
                    com.example.model.GameMatch match = 
                        (com.example.model.GameMatch) message.getData();
                    dispose();
                    new GameView(client, currentUser, match).setVisible(true);
                    break;
                    
                case Message.USER_STATUS_CHANGED:
                    requestOnlineUsers();
                    break;
                    
                case Message.LEADERBOARD_DATA:
                    showLeaderboardDialog((List<User>) message.getData());
                    break;
                    
                case Message.ERROR:
                    JOptionPane.showMessageDialog(this, 
                        message.getData().toString(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });
    }
    
    private void updateUserList(List<User> users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            if (!user.getUsername().equals(currentUser.getUsername())) {
                tableModel.addRow(new Object[]{
                    user.getUsername(),
                    user.getTotalScore(),
                    user.getTotalWins(),
                    user.getStatus(),
                    user.getUserId() // Hidden column for user ID
                });
            }
        }
    }
    
    private void handleChallengeRequest(Message message) {
        User challenger = (User) message.getData();
        
        int response = JOptionPane.showConfirmDialog(this, 
            challenger.getUsername() + " muốn thách đấu với bạn. Bạn có chấp nhận không?",
            "Lời mời thách đấu", JOptionPane.YES_NO_OPTION);
        
        String responseStr = (response == JOptionPane.YES_OPTION) ? "ACCEPTED" : "REJECTED";
        Message responseMsg = new Message(Message.CHALLENGE_RESPONSE, 
            responseStr, currentUser.getUserId(), message.getSenderId());
        client.sendMessage(responseMsg);
    }
    
    private void showLeaderboardDialog(List<User> users) {
        JDialog dialog = new JDialog(this, "Bảng xếp hạng", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        String[] columnNames = {"Hạng", "Tên", "Tổng điểm", "Thắng", "Thua", "Hòa"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        int rank = 1;
        for (User user : users) {
            model.addRow(new Object[]{
                rank++,
                user.getUsername(),
                user.getTotalScore(),
                user.getTotalWins(),
                user.getTotalLosses(),
                user.getTotalDraws()
            });
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
}
