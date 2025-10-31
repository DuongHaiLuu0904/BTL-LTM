//package com.example;
//
////import com.example.tcp.GameServer;
//import com.example.view.LoginView;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//import java.util.Scanner;
//
//public class App 
//{
//    public static void main(String[] args) {
//        try (Scanner scanner = new Scanner(System.in)) {
//            System.out.println("===========================================");
//            System.out.println("    DART GAME - ONLINE MULTIPLAYER");
//            System.out.println("===========================================");
//            System.out.println();
//            System.out.println("Chọn chế độ chạy:");
//            System.out.println("1. Chạy Server (Máy chủ)");
//            System.out.println("2. Chạy Client (Máy khách)");
//            System.out.println("3. Chạy cả Server và Client");
//            System.out.println("0. Thoát");
//            System.out.println("===========================================");
//            System.out.print("Nhập lựa chọn của bạn (0-3): ");
//            
//            int choice = scanner.nextInt();
//            
//            switch (choice) {
//                case 1:
//                    startServer();
//                    break;
//                case 2:
//                    startClient();
//                    break;
//                case 3:
//                    startBoth();
//                    break;
//                case 0:
//                    System.out.println("Tạm biệt!");
//                    System.exit(0);
//                    break;
//                default:
//                    System.out.println("Lựa chọn không hợp lệ!");
//                    System.exit(1);
//            }
//        } catch (Exception e) {
//            System.out.println("Lỗi: Vui lòng nhập số từ 0-3");
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
//    
//    private static void startServer() {
//        System.out.println("\n>>> Đang khởi động Server...");
//        Thread serverThread = new Thread(() -> {
////            GameServer server = new GameServer();
////            server.start();
//        });
//        serverThread.start();
//        
//        System.out.println(">>> Server đã khởi động!");
//        System.out.println(">>> Nhấn Ctrl+C để dừng server");
//    }
//    
//    private static void startClient() {
//        System.out.println("\n>>> Đang khởi động Client...");
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            
//            // Start the login view
//            LoginView loginView = new LoginView();
//            loginView.setVisible(true);
//        });
//        System.out.println(">>> Client đã khởi động!");
//    }
//    
//    private static void startBoth() {
//        System.out.println("\n>>> Đang khởi động cả Server và Client...");
//        
//        // Start Server in background thread
//        Thread serverThread = new Thread(() -> {
////            GameServer server = new GameServer();
////            server.start();
//        });
//        serverThread.start();
//        
//        System.out.println(">>> Server đã khởi động!");
//        
//        // Wait a bit for server to fully start
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        
//        // Start Client
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            
//            LoginView loginView = new LoginView();
//            loginView.setVisible(true);
//        });
//        
//        System.out.println(">>> Client đã khởi động!");
//        System.out.println(">>> Nhấn Ctrl+C để dừng");
//    }
//}
