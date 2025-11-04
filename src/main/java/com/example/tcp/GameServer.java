package com.example.tcp;

import com.example.model.Message;
import com.example.model.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private static int PORT = 5000;
    private ServerSocket serverSocket;
    private Map<Integer, ClientHandler> clients;
    private boolean isRunning;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
            PORT = Integer.parseInt(properties.getProperty("server.port", "5000"));
        } catch (IOException | NumberFormatException e) {
            try (InputStream input = GameServer.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                    PORT = Integer.parseInt(properties.getProperty("server.port", "5000"));
                } else {
                    System.err.println("application.properties not found, using default port: " + PORT);
                }
            } catch (IOException | NumberFormatException ex) {
                System.err.println("Failed to load application.properties, using default port: " + PORT);
            }
        }
    }

    // Constructor khởi tạo server và reset trạng thái users
    public GameServer() {
        clients = new ConcurrentHashMap<>();

        // Đặt lại tất cả users về offline khi server khởi động
        com.example.controller.UserDAO userDAO = new com.example.controller.UserDAO();
        userDAO.resetAllUsersToOffline();
    }

    // Khởi động server và lắng nghe kết nối từ clients
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                } catch (IOException e) {
                    if (isRunning) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT);
            e.printStackTrace();
        }
    }

    // Dừng server và ngắt kết nối tất cả clients
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Ngắt kết nối tất cả clients
            for (ClientHandler handler : clients.values()) {
                handler.sendMessage(new Message(Message.ERROR, "Server is shutting down"));
            }
            clients.clear();
            System.out.println("Server stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thêm client mới vào danh sách quản lý
    public void addClient(int userId, ClientHandler handler) {
        clients.put(userId, handler);
        System.out.println("Client added: " + userId + " (Total: " + clients.size() + ")");
    }

    // Xóa client khỏi danh sách quản lý
    public void removeClient(int userId) {
        clients.remove(userId);
        System.out.println("Client removed: " + userId + " (Total: " + clients.size() + ")");
    }

    // Lấy ClientHandler theo userId
    public ClientHandler getClientHandler(int userId) {
        return clients.get(userId);
    }

    // Kiểm tra xem user có đang online không
    public boolean isUserOnline(int userId) {
        return clients.containsKey(userId);
    }

    // Lấy danh sách tất cả users đang online
    public List<User> getOnlineUsers() {
        List<User> onlineUsers = new ArrayList<>();
        for (ClientHandler handler : clients.values()) {
            User user = handler.getCurrentUser();
            if (user != null) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    // Phát sóng thông báo thay đổi trạng thái user đến tất cả clients
    public void broadcastUserStatusChange() {
        Message msg = new Message(Message.USER_STATUS_CHANGED);
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(msg);
        }
    }

    // Phát sóng message đến tất cả clients
    public void broadcastMessage(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();

        // Thêm shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));

        server.start();
    }
}
