package com.example.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        Properties properties = new Properties();
        
        // Try to load from root directory first
        try (InputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
            URL = properties.getProperty("db.url");
            USERNAME = properties.getProperty("db.username");
            PASSWORD = properties.getProperty("db.password");
            System.out.println("Loaded database configuration from application.properties");
        } catch (IOException e) {
            // Try to load from classpath
            try (InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                    URL = properties.getProperty("db.url");
                    USERNAME = properties.getProperty("db.username");
                    PASSWORD = properties.getProperty("db.password");
                    System.out.println("Loaded database configuration from classpath");
                } else {
                    throw new IOException("application.properties not found in classpath");
                }
            } catch (IOException ex) {
                System.err.println("Failed to load application.properties, using defaults");
                URL = "jdbc:mysql://localhost:3306/dart_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                USERNAME = "root";
                PASSWORD = "09042004";
            }
        }
    }
    
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null || !isConnectionValid()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    private static boolean isConnectionValid() {
        try {
            return instance != null && 
                   instance.connection != null && 
                   !instance.connection.isClosed() && 
                   instance.connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
