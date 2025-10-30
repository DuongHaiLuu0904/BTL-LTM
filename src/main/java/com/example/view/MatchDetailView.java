package com.example.view;

import com.example.model.GameMatchDTO;
import com.example.model.MatchThrowDetail;
import com.example.controller.GameMatchDAO;
import com.example.controller.MatchThrowDetailDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * View for displaying detailed information about a specific match
 */
public class MatchDetailView extends JFrame {
    private int matchId;
    private GameMatchDTO match;
    private GameMatchDAO matchDAO;
    private MatchThrowDetailDAO throwDAO;
    
    private JLabel matchInfoLabel;
    private JLabel player1Label;
    private JLabel player2Label;
    private JTable throwTable;
    private DefaultTableModel tableModel;
    private DartBoardPanel dartBoardPanel;
    
    public MatchDetailView(int matchId, int currentUserId) {
        this.matchId = matchId;
        this.matchDAO = new GameMatchDAO();
        this.throwDAO = new MatchThrowDetailDAO();
        
        setTitle("Chi Tiết Trận Đấu #" + matchId);
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        loadMatchData();
    }
    
    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Match info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Thông Tin Trận Đấu"));
        
        matchInfoLabel = new JLabel("", SwingConstants.CENTER);
        matchInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(matchInfoLabel, BorderLayout.NORTH);
        
        // Players info
        JPanel playersPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        playersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        player1Label = new JLabel("", SwingConstants.CENTER);
        player1Label.setFont(new Font("Arial", Font.BOLD, 16));
        player1Label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        
        player2Label = new JLabel("", SwingConstants.CENTER);
        player2Label.setFont(new Font("Arial", Font.BOLD, 16));
        player2Label.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        
        playersPanel.add(player1Label);
        playersPanel.add(player2Label);
        topPanel.add(playersPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Split into table and dartboard
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setResizeWeight(0.6);
        
        // Left - Throw details table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Chi Tiết Các Lượt Ném"));
        
        String[] columnNames = {"Lượt", "Người Chơi", "Điểm", "Góc θ", "Góc φ", "Lực (%)", "Trúng"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        throwTable = new JTable(tableModel);
        throwTable.setFont(new Font("Arial", Font.PLAIN, 13));
        throwTable.setRowHeight(25);
        throwTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < throwTable.getColumnCount(); i++) {
            throwTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Add row selection listener to show throw on dartboard
        throwTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedThrowOnBoard();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(throwTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        centerSplit.setLeftComponent(tablePanel);
        
        // Right - Dartboard visualization
        JPanel dartBoardContainer = new JPanel(new BorderLayout());
        dartBoardContainer.setBorder(BorderFactory.createTitledBorder("Bảng Phi Tiêu"));
        
        dartBoardPanel = new DartBoardPanel();
        dartBoardPanel.setPreferredSize(new Dimension(400, 400));
        
        JPanel dartBoardWrapper = new JPanel(new GridBagLayout());
        dartBoardWrapper.add(dartBoardPanel);
        dartBoardContainer.add(dartBoardWrapper, BorderLayout.CENTER);
        
        // Add button to show all throws
        JButton showAllButton = new JButton("Hiện Tất Cả Phi Tiêu");
        showAllButton.setFont(new Font("Arial", Font.BOLD, 12));
        showAllButton.addActionListener(e -> showAllThrowsOnBoard());
        
        JPanel dartControlPanel = new JPanel(new FlowLayout());
        dartControlPanel.add(showAllButton);
        dartBoardContainer.add(dartControlPanel, BorderLayout.SOUTH);
        
        centerSplit.setRightComponent(dartBoardContainer);
        
        mainPanel.add(centerSplit, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadMatchData() {
        // Load match info
        match = matchDAO.getMatchWithNames(matchId);
        
        if (match == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy thông tin trận đấu!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        // Update match info label
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateStr = match.getEndTime() != null ? dateFormat.format(match.getEndTime()) : "N/A";
        matchInfoLabel.setText(String.format("Trận Đấu #%d - %s", matchId, dateStr));
        
        // Update player labels
        updatePlayerLabel(player1Label, match.getPlayer1Username(), match.getPlayer1Score(), 
                         match.getWinnerId() == match.getPlayer1Id());
        updatePlayerLabel(player2Label, match.getPlayer2Username(), match.getPlayer2Score(),
                         match.getWinnerId() == match.getPlayer2Id());
        
        // Load throw details
        loadThrowDetails();
    }
    
    private void updatePlayerLabel(JLabel label, String username, int score, boolean isWinner) {
        String winnerMark = isWinner ? " 🏆" : "";
        label.setText(String.format("<html><center>%s%s<br/>Điểm: %d</center></html>", 
                                   username, winnerMark, score));
    }
    
    private void loadThrowDetails() {
        tableModel.setRowCount(0);
        
        List<MatchThrowDetail> throwList = throwDAO.getThrowDetailsByMatch(matchId);
        
        for (MatchThrowDetail throwDetail : throwList) {
            Object[] row = new Object[7];
            row[0] = throwDetail.getThrowNumber();
            row[1] = throwDetail.getPlayerUsername();
            row[2] = throwDetail.getScore();
            row[3] = String.format("%.1f°", throwDetail.getThetaDeg());
            row[4] = String.format("%.1f°", throwDetail.getPhiDeg());
            row[5] = String.format("%.0f%%", throwDetail.getPowerPercent());
            row[6] = throwDetail.isHitBoard() ? "✓" : "✗";
            
            tableModel.addRow(row);
        }
        
        if (throwList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Trận đấu này chưa có dữ liệu chi tiết!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showSelectedThrowOnBoard() {
        int selectedRow = throwTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        dartBoardPanel.clearDarts();
        
        List<MatchThrowDetail> throwList = throwDAO.getThrowDetailsByMatch(matchId);
        if (selectedRow < throwList.size()) {
            MatchThrowDetail throwDetail = throwList.get(selectedRow);
            
            // Convert hit coordinates to pixel coordinates for display
            // xHit and yHit are in meters, need to convert to pixels
            double pixelScale = 180.0 / 0.225; // 180 pixels / 0.225m
            int centerX = dartBoardPanel.getWidth() / 2;
            int centerY = dartBoardPanel.getHeight() / 2;
            
            double x = centerX + throwDetail.getXHit() * pixelScale;
            double y = centerY - throwDetail.getYHit() * pixelScale; // Invert Y
            
            dartBoardPanel.addDart(x, y);
        }
    }
    
    private void showAllThrowsOnBoard() {
        dartBoardPanel.clearDarts();
        
        List<MatchThrowDetail> throwList = throwDAO.getThrowDetailsByMatch(matchId);
        
        double pixelScale = 180.0 / 0.225;
        int centerX = dartBoardPanel.getWidth() / 2;
        int centerY = dartBoardPanel.getHeight() / 2;
        
        for (MatchThrowDetail throwDetail : throwList) {
            double x = centerX + throwDetail.getXHit() * pixelScale;
            double y = centerY - throwDetail.getYHit() * pixelScale;
            dartBoardPanel.addDart(x, y);
        }
    }
    
    public static void main(String[] args) {
        // Test the view
        SwingUtilities.invokeLater(() -> {
            MatchDetailView view = new MatchDetailView(1, 1);
            view.setVisible(true);
        });
    }
}
