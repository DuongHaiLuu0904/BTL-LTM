package com.example.tcp;

import com.example.model.Message;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.function.Consumer;

public class GameClient {
    private static String SERVER_HOST = "localhost";
    private static int SERVER_PORT = 5000;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<Message> messageHandler;
    private boolean isConnected;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        Properties properties = new Properties();
        
        // Try to load from root directory first
        try (InputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
            SERVER_HOST = properties.getProperty("server.host", "localhost");
            SERVER_PORT = Integer.parseInt(properties.getProperty("server.port", "5000"));
            System.out.println("Loaded client configuration from application.properties");
        } catch (IOException | NumberFormatException e) {
            // Try to load from classpath
            try (InputStream input = GameClient.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                    SERVER_HOST = properties.getProperty("server.host", "localhost");
                    SERVER_PORT = Integer.parseInt(properties.getProperty("server.port", "5000"));
                    System.out.println("Loaded client configuration from classpath");
                } else {
                    System.err.println("application.properties not found, using defaults: " + SERVER_HOST + ":" + SERVER_PORT);
                }
            } catch (IOException | NumberFormatException ex) {
                System.err.println("Failed to load application.properties, using defaults: " + SERVER_HOST + ":" + SERVER_PORT);
            }
        }
    }
    
    public GameClient(Consumer<Message> messageHandler) {
        this.messageHandler = messageHandler;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            
            // Start listening for messages
            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
            return false;
        }
    }
    
    private void listenForMessages() {
        try {
            while (isConnected) {
                Message message = (Message) in.readObject();
                if (messageHandler != null) {
                    messageHandler.accept(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (isConnected) {
                System.err.println("Connection lost: " + e.getMessage());
                isConnected = false;
                if (messageHandler != null) {
                    messageHandler.accept(new Message(Message.ERROR, 
                        "Mất kết nối với server!"));
                }
            }
        }
    }
    
    public void sendMessage(Message message) {
        try {
            if (out != null && isConnected) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setMessageHandler(Consumer<Message> messageHandler) {
        this.messageHandler = messageHandler;
    }
    
    public void disconnect() {
        isConnected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}
