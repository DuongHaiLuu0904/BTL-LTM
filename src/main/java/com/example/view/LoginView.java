package com.example.view;

import com.example.model.Message;
import com.example.tcp.GameClient;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private GameClient client;

    public LoginView() {
        setTitle("Đăng nhập - Game Ném Phi Tiêu");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();

        // Kết nối đến server
        client = new GameClient(this::handleServerMessage);
        if (!client.connect()) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến server!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        JLabel titleLabel = new JLabel("GAME NÉM PHI TIÊU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel form
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        formPanel.add(new JLabel("Tên đăng nhập:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Mật khẩu:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        loginButton = new JButton("Đăng nhập");
        loginButton.setPreferredSize(new Dimension(120, 35));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        registerButton = new JButton("Đăng ký");
        registerButton.setPreferredSize(new Dimension(120, 35));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Thêm listener phím Enter
        passwordField.addActionListener(e -> handleLogin());

        add(mainPanel);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Message message = new Message(Message.LOGIN, username + ":" + password);
        client.sendMessage(message);

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 3 || password.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập và mật khẩu phải có ít nhất 3 ký tự!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Message message = new Message(Message.REGISTER, username + ":" + password);
        client.sendMessage(message);
    }

    private void handleServerMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case Message.LOGIN_SUCCESS:
                    com.example.model.User user = (com.example.model.User) message.getData();
                    dispose();
                    new LobbyView(client, user).setVisible(true);
                    break;

                case Message.LOGIN_FAILED:
                    JOptionPane.showMessageDialog(this,
                            message.getData().toString(),
                            "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                    break;

                case Message.REGISTER_SUCCESS:
                    JOptionPane.showMessageDialog(this,
                            message.getData().toString(),
                            "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
                    passwordField.setText("");
                    break;

                case Message.REGISTER_FAILED:
                    JOptionPane.showMessageDialog(this,
                            message.getData().toString(),
                            "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
                    break;

                case Message.ERROR:
                    JOptionPane.showMessageDialog(this,
                            message.getData().toString(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginView().setVisible(true);
        });
    }
}
