package com.example.tcp;

import com.example.controller.GameMatchDAO;
import com.example.controller.UserDAO;
import com.example.model.GameMatch;
import com.example.model.Message;
import com.example.model.ThrowResult;
import com.example.model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameServer server;
    private User currentUser;
    private UserDAO userDAO;
    private GameMatchDAO gameMatchDAO;
    
    // Constructor khởi tạo handler cho client mới
    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.userDAO = new UserDAO();
        this.gameMatchDAO = new GameMatchDAO();
        
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Phương thức chính chạy luồng xử lý client
    @Override
    public void run() {
        try {
            Message message;
            while ((message = (Message) in.readObject()) != null) {
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + 
                (currentUser != null ? currentUser.getUsername() : "Unknown"));
        } finally {
            cleanup();
        }
    }
    
    // Xử lý message từ client dựa trên loại message
    private void handleMessage(Message message) {
        try {
            switch (message.getType()) {
                case Message.LOGIN:
                    handleLogin(message);
                    break;
                case Message.REGISTER:
                    handleRegister(message);
                    break;
                case Message.LOGOUT:
                    handleLogout();
                    break;
                case Message.GET_ONLINE_USERS:
                    handleGetOnlineUsers();
                    break;
                case Message.CHALLENGE_REQUEST:
                    handleChallengeRequest(message);
                    break;
                case Message.CHALLENGE_RESPONSE:
                    handleChallengeResponse(message);
                    break;
                case Message.THROW_DART:
                    handleThrowDart(message);
                    break;
                case Message.ROTATE_BOARD:
                    handleRotateBoard(message);
                    break;
                case Message.EXIT_GAME:
                    handleExitGame();
                    break;
                case Message.REMATCH_REQUEST:
                    handleRematchRequest(message);
                    break;
                case Message.REMATCH_RESPONSE:
                    handleRematchResponse(message);
                    break;
                case Message.GET_LEADERBOARD:
                    handleGetLeaderboard();
                    break;
                default:
                    System.out.println("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message(Message.ERROR, "Server error: " + e.getMessage()));
        }
    }
    
    // Xử lý yêu cầu đăng nhập
    private void handleLogin(Message message) {
        String[] credentials = ((String) message.getData()).split(":");
        String username = credentials[0];
        String password = credentials[1];
        
        User user = userDAO.loginUser(username, password);
        
        if (user != null) {
            // Kiểm tra xem user đã đăng nhập ở nơi khác chưa
            if (server.isUserOnline(user.getUserId())) {
                sendMessage(new Message(Message.LOGIN_FAILED, 
                    "Tài khoản đang được đăng nhập ở nơi khác!"));
                return;
            }
            
            this.currentUser = user;
            server.addClient(user.getUserId(), this);
            sendMessage(new Message(Message.LOGIN_SUCCESS, user));
            server.broadcastUserStatusChange();
            System.out.println("User logged in: " + username);
        } else {
            sendMessage(new Message(Message.LOGIN_FAILED, 
                "Tên đăng nhập hoặc mật khẩu không đúng!"));
        }
    }
    
    // Xử lý yêu cầu đăng ký tài khoản mới
    private void handleRegister(Message message) {
        String[] credentials = ((String) message.getData()).split(":");
        String username = credentials[0];
        String password = credentials[1];
        
        if (userDAO.usernameExists(username)) {
            sendMessage(new Message(Message.REGISTER_FAILED, 
                "Tên đăng nhập đã tồn tại!"));
            return;
        }
        
        if (userDAO.registerUser(username, password)) {
            sendMessage(new Message(Message.REGISTER_SUCCESS, 
                "Đăng ký thành công! Vui lòng đăng nhập."));
            System.out.println("New user registered: " + username);
        } else {
            sendMessage(new Message(Message.REGISTER_FAILED, 
                "Đăng ký thất bại! Vui lòng thử lại."));
        }
    }
    
    // Xử lý yêu cầu đăng xuất
    private void handleLogout() {
        if (currentUser != null) {
            userDAO.logoutUser(currentUser.getUserId());
            userDAO.updateBusyStatus(currentUser.getUserId(), false);
            server.removeClient(currentUser.getUserId());
            server.broadcastUserStatusChange();
            System.out.println("User logged out: " + currentUser.getUsername());
        }
    }
    
    // Xử lý yêu cầu lấy danh sách người chơi online
    private void handleGetOnlineUsers() {
        // Lấy danh sách users đang thực sự kết nối với server
        List<User> onlineUsers = server.getOnlineUsers();
        sendMessage(new Message(Message.ONLINE_USERS_LIST, onlineUsers));
    }
    
    // Xử lý yêu cầu thách đấu từ người chơi
    private void handleChallengeRequest(Message message) {
        int opponentId = (int) message.getData();
        ClientHandler opponentHandler = server.getClientHandler(opponentId);
        
        if (opponentHandler != null && opponentHandler.currentUser != null) {
            if (opponentHandler.currentUser.isBusy()) {
                sendMessage(new Message(Message.ERROR, 
                    "Người chơi này đang bận!"));
                return;
            }
            
            Message challengeMsg = new Message(Message.CHALLENGE_REQUEST, 
                currentUser, currentUser.getUserId(), opponentId);
            opponentHandler.sendMessage(challengeMsg);
        } else {
            sendMessage(new Message(Message.ERROR, 
                "Người chơi không khả dụng!"));
        }
    }
    
    // Xử lý phản hồi thách đấu (chấp nhận hoặc từ chối)
    private void handleChallengeResponse(Message message) {
        String response = (String) message.getData();
        int challengerId = message.getReceiverId();
        ClientHandler challengerHandler = server.getClientHandler(challengerId);
        
        if (challengerHandler != null) {
            if ("ACCEPTED".equals(response)) {
                // Tạo trận đấu mới
                int matchId = gameMatchDAO.createMatch(challengerId, currentUser.getUserId());
                
                if (matchId > 0) {
                    GameMatch match = gameMatchDAO.getMatchById(matchId);
                    
                    // Cập nhật trạng thái bận cho cả hai người chơi
                    userDAO.updateBusyStatus(challengerId, true);
                    userDAO.updateBusyStatus(currentUser.getUserId(), true);
                    
                    // Thông báo cho cả hai người chơi
                    challengerHandler.sendMessage(new Message(Message.GAME_START, match));
                    sendMessage(new Message(Message.GAME_START, match));
                    
                    // Phát sóng thay đổi trạng thái
                    server.broadcastUserStatusChange();
                }
            } else {
                challengerHandler.sendMessage(new Message(Message.CHALLENGE_REJECTED, 
                    currentUser.getUsername() + " đã từ chối thách đấu."));
            }
        }
    }
    
    // Xử lý hành động ném phi tiêu của người chơi
    private void handleThrowDart(Message message) {
        ThrowResult throwResult = (ThrowResult) message.getData();
        GameMatch match = gameMatchDAO.getActiveMatchForPlayer(currentUser.getUserId());
        
        if (match == null) {
            sendMessage(new Message(Message.ERROR, "Không tìm thấy trận đấu!"));
            return;
        }
        
        // Xác minh đến lượt của người chơi
        if (match.getCurrentPlayerId() != currentUser.getUserId()) {
            sendMessage(new Message(Message.ERROR, "Chưa đến lượt của bạn!"));
            return;
        }
        
        // Tính điểm dựa trên vị trí
        int score = calculateScore(throwResult.getX(), throwResult.getY(), 
            match.getBoardRotation());
        throwResult.setScore(score);
        
        // Cập nhật trạng thái trận đấu
        match.addScore(currentUser.getUserId(), score);
        match.decrementThrows(currentUser.getUserId());
        
        // Gửi kết quả ném cho cả hai người chơi
        int opponentId = (match.getPlayer1Id() == currentUser.getUserId()) 
            ? match.getPlayer2Id() : match.getPlayer1Id();
        ClientHandler opponentHandler = server.getClientHandler(opponentId);
        
        sendMessage(new Message(Message.THROW_RESULT, throwResult));
        if (opponentHandler != null) {
            opponentHandler.sendMessage(new Message(Message.THROW_RESULT, throwResult));
        }
        
        // Kiểm tra xem trò chơi đã kết thúc chưa
        if (match.isGameOver()) {
            handleGameOver(match);
        } else {
            // Chưa chuyển lượt - cho phép người chơi hiện tại xoay bảng
            gameMatchDAO.updateMatchState(match);
            
            // Gửi trạng thái game đã cập nhật
            sendMessage(new Message(Message.GAME_STATE, match));
            if (opponentHandler != null) {
                opponentHandler.sendMessage(new Message(Message.GAME_STATE, match));
            }
        }
    }
    
    // Xử lý xoay bảng phi tiêu sau khi ném
    private void handleRotateBoard(Message message) {
        int rotation = (int) message.getData();
        GameMatch match = gameMatchDAO.getActiveMatchForPlayer(currentUser.getUserId());
        
        if (match != null) {
            match.setBoardRotation(rotation);
            match.switchPlayer(); // Bây giờ chuyển sang lượt của đối thủ
            gameMatchDAO.updateMatchState(match);
            
            // Thông báo cho cả hai người chơi
            int opponentId = (match.getPlayer1Id() == currentUser.getUserId()) 
                ? match.getPlayer2Id() : match.getPlayer1Id();
            ClientHandler opponentHandler = server.getClientHandler(opponentId);
            
            Message updateMsg = new Message(Message.TURN_CHANGED, match);
            sendMessage(updateMsg);
            if (opponentHandler != null) {
                opponentHandler.sendMessage(updateMsg);
            }
        }
    }
    
    // Xử lý khi trò chơi kết thúc và tính toán kết quả
    private void handleGameOver(GameMatch match) {
        int winnerId = 0;
        String result1 = "", result2 = "";
        
        if (match.getPlayer1Score() > match.getPlayer2Score()) {
            winnerId = match.getPlayer1Id();
            userDAO.updateUserStats(match.getPlayer1Id(), 3, "WIN");
            userDAO.updateUserStats(match.getPlayer2Id(), 0, "LOSS");
            result1 = "WIN";
            result2 = "LOSS";
        } else if (match.getPlayer2Score() > match.getPlayer1Score()) {
            winnerId = match.getPlayer2Id();
            userDAO.updateUserStats(match.getPlayer2Id(), 3, "WIN");
            userDAO.updateUserStats(match.getPlayer1Id(), 0, "LOSS");
            result1 = "LOSS";
            result2 = "WIN";
        } else {
            // Hòa
            userDAO.updateUserStats(match.getPlayer1Id(), 1, "DRAW");
            userDAO.updateUserStats(match.getPlayer2Id(), 1, "DRAW");
            result1 = "DRAW";
            result2 = "DRAW";
        }
        
        gameMatchDAO.endMatch(match.getMatchId(), winnerId, "FINISHED");
        
        // Cập nhật trạng thái bận
        userDAO.updateBusyStatus(match.getPlayer1Id(), false);
        userDAO.updateBusyStatus(match.getPlayer2Id(), false);
        
        // Thông báo cho cả hai người chơi
        ClientHandler player1Handler = server.getClientHandler(match.getPlayer1Id());
        ClientHandler player2Handler = server.getClientHandler(match.getPlayer2Id());
        
        if (player1Handler != null) {
            player1Handler.sendMessage(new Message(Message.GAME_OVER, 
                result1 + ":" + match.getPlayer1Score() + ":" + match.getPlayer2Score()));
        }
        if (player2Handler != null) {
            player2Handler.sendMessage(new Message(Message.GAME_OVER, 
                result2 + ":" + match.getPlayer2Score() + ":" + match.getPlayer1Score()));
        }
        
        server.broadcastUserStatusChange();
    }
    
    // Xử lý khi người chơi thoát game giữa chừng
    private void handleExitGame() {
        GameMatch match = gameMatchDAO.getActiveMatchForPlayer(currentUser.getUserId());
        
        if (match != null) {
            int opponentId = (match.getPlayer1Id() == currentUser.getUserId()) 
                ? match.getPlayer2Id() : match.getPlayer1Id();
            ClientHandler opponentHandler = server.getClientHandler(opponentId);
            
            // Xác định người thắng (đối thủ thắng)
            userDAO.updateUserStats(opponentId, 3, "WIN");
            userDAO.updateUserStats(currentUser.getUserId(), 0, "LOSS");
            gameMatchDAO.endMatch(match.getMatchId(), opponentId, "FINISHED");
            
            // Cập nhật trạng thái bận
            userDAO.updateBusyStatus(match.getPlayer1Id(), false);
            userDAO.updateBusyStatus(match.getPlayer2Id(), false);
            
            // Thông báo cho đối thủ
            if (opponentHandler != null) {
                opponentHandler.sendMessage(new Message(Message.OPPONENT_LEFT, 
                    currentUser.getUsername() + " đã thoát. Bạn thắng!"));
            }
            
            server.broadcastUserStatusChange();
        }
    }
    
    // Xử lý yêu cầu chơi lại từ người chơi
    private void handleRematchRequest(Message message) {
        int opponentId = (int) message.getData();
        ClientHandler opponentHandler = server.getClientHandler(opponentId);
        
        if (opponentHandler != null) {
            opponentHandler.sendMessage(new Message(Message.REMATCH_REQUEST, 
                currentUser, currentUser.getUserId()));
        }
    }
    
    // Xử lý phản hồi yêu cầu chơi lại
    private void handleRematchResponse(Message message) {
        String response = (String) message.getData();
        int requesterId = message.getReceiverId();
        ClientHandler requesterHandler = server.getClientHandler(requesterId);
        
        if (requesterHandler != null) {
            if ("ACCEPTED".equals(response)) {
                // Tạo trận đấu mới
                int matchId = gameMatchDAO.createMatch(requesterId, currentUser.getUserId());
                
                if (matchId > 0) {
                    GameMatch match = gameMatchDAO.getMatchById(matchId);
                    
                    // Cập nhật trạng thái bận
                    userDAO.updateBusyStatus(requesterId, true);
                    userDAO.updateBusyStatus(currentUser.getUserId(), true);
                    
                    // Thông báo cho cả hai người chơi
                    requesterHandler.sendMessage(new Message(Message.GAME_START, match));
                    sendMessage(new Message(Message.GAME_START, match));
                    
                    server.broadcastUserStatusChange();
                }
            } else {
                requesterHandler.sendMessage(new Message(Message.ERROR, 
                    currentUser.getUsername() + " đã từ chối chơi lại."));
            }
        }
    }
    
    // Xử lý yêu cầu lấy bảng xếp hạng
    private void handleGetLeaderboard() {
        List<User> leaderboard = userDAO.getLeaderboard();
        sendMessage(new Message(Message.LEADERBOARD_DATA, leaderboard));
    }
    
    // Tính điểm dựa trên vị trí phi tiêu đâm vào bảng
    private int calculateScore(double x, double y, int rotation) {
        // Tính khoảng cách từ tâm
        double distance = Math.sqrt(x * x + y * y);
        
        // Các vùng trên bảng phi tiêu (đơn giản hóa)
        // Bullseye (tâm): 50 điểm
        // Vòng trong: 25 điểm
        // Vòng ba lần: điểm cơ sở * 3
        // Vòng hai lần: điểm cơ sở * 2
        // Vùng đơn: điểm cơ sở
        
        if (distance <= 12.7) {
            return 50; // Bullseye
        } else if (distance <= 31.8) {
            return 25; // Vòng trong
        } else if (distance <= 170) {
            // Tính góc và xác định giá trị phân đoạn
            double angle = Math.toDegrees(Math.atan2(y, x));
            angle = (angle + rotation) % 360;
            if (angle < 0) angle += 360;
            
            // Các phân đoạn tiêu chuẩn trên bảng phi tiêu: 20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5
            int[] segments = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};
            int segmentIndex = (int) (angle / 18) % 20;
            int baseValue = segments[segmentIndex];
            
            // Xác định vòng
            if (distance >= 99 && distance <= 107) {
                return baseValue * 3; // Vòng ba lần
            } else if (distance >= 162 && distance <= 170) {
                return baseValue * 2; // Vòng hai lần
            } else {
                return baseValue; // Vùng đơn
            }
        }
        
        return 0; // Trượt
    }
    
    // Gửi message đến client
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Dọn dẹp và đóng kết nối khi client ngắt kết nối
    private void cleanup() {
        handleLogout();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Lấy thông tin user hiện tại đang kết nối
    public User getCurrentUser() {
        return currentUser;
    }
}
