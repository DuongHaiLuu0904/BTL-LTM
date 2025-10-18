package com.example.model;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private Object data;
    private int senderId;
    private int receiverId;
    
    // Constructor mặc định
    public Message() {
    }
    
    // Constructor với loại message
    public Message(String type) {
        this.type = type;
    }
    
    // Constructor với loại và dữ liệu
    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    // Constructor với loại, dữ liệu và người gửi
    public Message(String type, Object data, int senderId) {
        this.type = type;
        this.data = data;
        this.senderId = senderId;
    }
    
    // Constructor đầy đủ với loại, dữ liệu, người gửi và người nhận
    public Message(String type, Object data, int senderId, int receiverId) {
        this.type = type;
        this.data = data;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
    
    // Message types constants
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String REGISTER = "REGISTER";
    public static final String REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String REGISTER_FAILED = "REGISTER_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String GET_ONLINE_USERS = "GET_ONLINE_USERS";
    public static final String ONLINE_USERS_LIST = "ONLINE_USERS_LIST";
    public static final String CHALLENGE_REQUEST = "CHALLENGE_REQUEST";
    public static final String CHALLENGE_RESPONSE = "CHALLENGE_RESPONSE";
    public static final String CHALLENGE_ACCEPTED = "CHALLENGE_ACCEPTED";
    public static final String CHALLENGE_REJECTED = "CHALLENGE_REJECTED";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_STATE = "GAME_STATE";
    public static final String THROW_DART = "THROW_DART";
    public static final String THROW_RESULT = "THROW_RESULT";
    public static final String ROTATE_BOARD = "ROTATE_BOARD";
    public static final String TURN_CHANGED = "TURN_CHANGED";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String REMATCH_REQUEST = "REMATCH_REQUEST";
    public static final String REMATCH_RESPONSE = "REMATCH_RESPONSE";
    public static final String EXIT_GAME = "EXIT_GAME";
    public static final String OPPONENT_LEFT = "OPPONENT_LEFT";
    public static final String GET_LEADERBOARD = "GET_LEADERBOARD";
    public static final String LEADERBOARD_DATA = "LEADERBOARD_DATA";
    public static final String USER_STATUS_CHANGED = "USER_STATUS_CHANGED";
    public static final String ERROR = "ERROR";
    
    // Getters and Setters
    // Lấy loại message
    public String getType() {
        return type;
    }
    
    // Đặt loại message
    public void setType(String type) {
        this.type = type;
    }
    
    // Lấy dữ liệu message
    public Object getData() {
        return data;
    }
    
    // Đặt dữ liệu message
    public void setData(Object data) {
        this.data = data;
    }
    
    // Lấy ID người gửi
    public int getSenderId() {
        return senderId;
    }
    
    // Đặt ID người gửi
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    
    // Lấy ID người nhận
    public int getReceiverId() {
        return receiverId;
    }
    
    // Đặt ID người nhận
    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }
}
