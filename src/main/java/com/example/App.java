package com.example;

import com.example.tcp.GameServer;
import com.example.view.LoginView;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    startServer();
                    break;
                case 2:
                    startClient();
                    break;
                case 3:
                    startBoth();
                    break;
                case 0:
                    System.exit(0);
                    break;
                default:
                    System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startServer() {
        Thread serverThread = new Thread(() -> {
            GameServer server = new GameServer();
            server.start();
        });
        serverThread.start();
    }

    private static void startClient() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Start the login view
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }

    private static void startBoth() {
        // Start Server in background thread
        Thread serverThread = new Thread(() -> {
            GameServer server = new GameServer();
            server.start();
        });
        serverThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start Client
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
