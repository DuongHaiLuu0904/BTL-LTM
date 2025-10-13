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
    private Map<Integer, ClientHandler> clients; // userId -> ClientHandler
    private boolean isRunning;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        Properties properties = new Properties();
        
        // Try to load from root directory first
        try (InputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
            PORT = Integer.parseInt(properties.getProperty("server.port", "5000"));
            System.out.println("Loaded server configuration from application.properties");
        } catch (IOException | NumberFormatException e) {
            // Try to load from classpath
            try (InputStream input = GameServer.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                    PORT = Integer.parseInt(properties.getProperty("server.port", "5000"));
                    System.out.println("Loaded server configuration from classpath");
                } else {
                    System.err.println("application.properties not found, using default port: " + PORT);
                }
            } catch (IOException | NumberFormatException ex) {
                System.err.println("Failed to load application.properties, using default port: " + PORT);
            }
        }
    }
    
    public GameServer() {
        clients = new ConcurrentHashMap<>();
        
        // Reset all users to offline when server starts
        com.example.controller.UserDAO userDAO = new com.example.controller.UserDAO();
        userDAO.resetAllUsersToOffline();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Game Server started on port " + PORT);
            
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    
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
    
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Disconnect all clients
            for (ClientHandler handler : clients.values()) {
                handler.sendMessage(new Message(Message.ERROR, "Server is shutting down"));
            }
            clients.clear();
            System.out.println("Server stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addClient(int userId, ClientHandler handler) {
        clients.put(userId, handler);
        System.out.println("Client added: " + userId + " (Total: " + clients.size() + ")");
    }
    
    public void removeClient(int userId) {
        clients.remove(userId);
        System.out.println("Client removed: " + userId + " (Total: " + clients.size() + ")");
    }
    
    public ClientHandler getClientHandler(int userId) {
        return clients.get(userId);
    }
    
    public boolean isUserOnline(int userId) {
        return clients.containsKey(userId);
    }
    
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
    
    public void broadcastUserStatusChange() {
        Message msg = new Message(Message.USER_STATUS_CHANGED);
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(msg);
        }
    }
    
    public void broadcastMessage(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop();
        }));
        
        server.start();
    }
}
