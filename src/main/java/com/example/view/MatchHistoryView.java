package com.example.view;

import com.example.model.GameMatchDTO;
import com.example.controller.GameMatchDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class MatchHistoryView extends JFrame {
    private int currentUserId;
    private JTable matchTable;
    private DefaultTableModel tableModel;
    private GameMatchDAO matchDAO;
    private JButton viewDetailsButton;
    private JButton refreshButton;
    private JButton closeButton;

    public MatchHistoryView(int userId) {
        this.currentUserId = userId;
        this.matchDAO = new GameMatchDAO();

        setTitle("Lịch Sử Trận Đấu");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadMatchHistory();
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel
        JLabel titleLabel = new JLabel("LỊCH SỬ TRẬN ĐẤU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columnNames = { "Mã Trận", "Đối Thủ", "Tỷ Số", "Kết Quả", "Thời Gian" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        matchTable = new JTable(tableModel);
        matchTable.setFont(new Font("Arial", Font.PLAIN, 13));
        matchTable.setRowHeight(25);
        matchTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        matchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < matchTable.getColumnCount(); i++) {
            matchTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(matchTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        viewDetailsButton = new JButton("Xem Chi Tiết");
        viewDetailsButton.setFont(new Font("Arial", Font.PLAIN, 13));
        viewDetailsButton.addActionListener(e -> viewMatchDetails());

        refreshButton = new JButton("Làm Mới");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 13));
        refreshButton.addActionListener(e -> loadMatchHistory());

        closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 13));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadMatchHistory() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Load matches from database
        List<GameMatchDTO> matches = matchDAO.getMatchHistoryWithNames(currentUserId, 50);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (GameMatchDTO match : matches) {
            Object[] row = new Object[5];
            row[0] = "#" + match.getMatchId();
            row[1] = match.getOpponentUsername(currentUserId);
            row[2] = match.getFormattedScore(currentUserId);

            // Result
            String result = match.getMatchResult(currentUserId);
            if (result.equals("WIN")) {
                row[3] = "THẮNG";
            } else if (result.equals("LOSE")) {
                row[3] = "THUA";
            } else {
                row[3] = "HÒA";
            }

            row[4] = match.getEndTime() != null ? dateFormat.format(match.getEndTime()) : "N/A";

            tableModel.addRow(row);
        }

        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bạn chưa có trận đấu nào!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewMatchDetails() {
        int selectedRow = matchTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một trận đấu để xem chi tiết!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get match ID from selected row
        String matchIdStr = tableModel.getValueAt(selectedRow, 0).toString();
        int matchId = Integer.parseInt(matchIdStr.substring(1)); // Remove "#" prefix

        // Open match detail view
        MatchDetailView detailView = new MatchDetailView(matchId, currentUserId);
        detailView.setVisible(true);
    }

    public static void main(String[] args) {
        // Test the view
        SwingUtilities.invokeLater(() -> {
            MatchHistoryView view = new MatchHistoryView(1);
            view.setVisible(true);
        });
    }
}
