package com.chat.socketIO.socket;

import com.chat.socketIO.model.TypingStatus;
import com.chat.socketIO.model.User;
import com.chat.socketIO.model.UserStatus;
import com.chat.socketIO.repository.MessageRepository;
import com.chat.socketIO.repository.UserRepository;
//import com.chat.socketIO.service.SocketService;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.chat.socketIO.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@CrossOrigin
public class SocketModule {

    private final SocketIOServer server;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final Map<String, String> userTokenMap = new ConcurrentHashMap<>();

    @Autowired
    public SocketModule(SocketIOServer server, UserRepository userRepository, MessageRepository messageRepository) {
        this.server = server;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        startSocketModule();
    }

    public void startSocketModule() {
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("send_message", Message.class, onChatReceived());
        server.addEventListener("typing", TypingStatus.class, onTypingReceived());
        server.addEventListener("update_message_status", Message.class, onMessageStatusUpdated()); // New event listener for status update

        //server.start();
    }

    private ConnectListener onConnected() {
        return (client) -> {
            HandshakeData handshakeData = client.getHandshakeData();
            String token = handshakeData.getSingleUrlParam("token");
            String username = handshakeData.getSingleUrlParam("username");
            String room = handshakeData.getSingleUrlParam("room");

            // Find the user by token or username and room
            Optional<User> existingUserByToken = token != null ? userRepository.findByToken(token) : Optional.empty();
            Optional<User> existingUserByNameAndRoom = userRepository.findByUsernameAndRoom(username, room);

            User user;
            if (existingUserByToken.isPresent()) {
                user = existingUserByToken.get();
            } else if (existingUserByNameAndRoom.isPresent()) {
                user = existingUserByNameAndRoom.get();
            } else {
                user = new User();
                user.setToken(generateNewToken());
                user.setUsername(username);
                user.setRoom(room);
                // Set user status to online
                user.setOnline(true);
                userRepository.save(user);
            }

            token = user.getToken();
            userTokenMap.put(client.getSessionId().toString(), token);

            client.joinRoom(room);

            // Send chat history to the client
            List<Message> chatHistory = messageRepository.findByRoom(room);
            client.sendEvent("chat_history", chatHistory);

            // Notify the newly connected client of the current online status of all users in the room
            List<User> usersInRoom = userRepository.findByRoom(room);
            for (User u : usersInRoom) {
                client.sendEvent("user_status", new UserStatus(u.getUsername(), u.isOnline()));
            }

            // Notify other users in the room that a user has joined
            server.getRoomOperations(room).sendEvent("user_status", new UserStatus(username, true));

            log.info("Client[{}] - Connected with token: {} and username: {}", client.getSessionId().toString(), token, username);
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            String token = userTokenMap.remove(client.getSessionId().toString());
            if (token != null) {
                Optional<User> userOpt = userRepository.findByToken(token);
                userOpt.ifPresent(user -> {
                    user.setOnline(false); // Set user status to offline
                    userRepository.save(user);
                    // Notify other users in the room that a user has left
                    server.getRoomOperations(user.getRoom()).sendEvent("user_status", new UserStatus(user.getUsername(), false));
                });
            }
            log.info("Client[{}] - Disconnected from socket", client.getSessionId().toString());
        };
    }

    private DataListener<Message> onChatReceived() {
        return (client, data, ackSender) -> {
            String token = userTokenMap.get(client.getSessionId().toString());

            if (token != null) {
                // Save chat message to the database
                data.setStatus("SENT");
                messageRepository.save(data);
                // Broadcast the message to the room
                server.getRoomOperations(data.getRoom()).sendEvent("get_message", data);
                ackSender.sendAckData("message sent in room : " + data.getRoom());
            }
        };
    }

    private DataListener<TypingStatus> onTypingReceived() {
        return (client, data, ackSender) -> {
            String token = userTokenMap.get(client.getSessionId().toString());

            if (token != null) {
                log.info("Typing event received: {}", data);
                // Broadcast the typing status to the room
                server.getRoomOperations(data.getRoom()).sendEvent("typing_status", data);
            }
        };
    }

    private DataListener<Message> onMessageStatusUpdated() {
        return (client, data, ackSender) -> {
            Optional<Message> optionalMessage = messageRepository.findById(data.getId());
            if (optionalMessage.isPresent()) {
                Message message = optionalMessage.get();
                message.setStatus(data.getStatus());
                messageRepository.save(message);
                server.getRoomOperations(message.getRoom()).sendEvent("message_status_updated", message);
            }
        };
    }

    private String generateNewToken() {
        return java.util.UUID.randomUUID().toString();
    }
}