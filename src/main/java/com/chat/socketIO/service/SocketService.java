package com.chat.socketIO.service;

import com.chat.socketIO.model.Message;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocketService {

    public void sendMessage(String room,String eventName, SocketIOClient senderClient, Message message) {
//        message.setSender(senderClient.getSessionId().toString());
        log.info("Sending message to [get_message] event : {} " , message);
        for (
                SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                client.sendEvent(eventName, message);
            }
        }
    }

}
