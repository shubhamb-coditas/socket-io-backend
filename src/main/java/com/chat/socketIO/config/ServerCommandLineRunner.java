package com.chat.socketIO.config;

import com.chat.socketIO.socket.SocketModule;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServerCommandLineRunner implements CommandLineRunner {

    private final SocketIOServer server;
    private final SocketModule socketModule;

    @Override
    public void run(String... args) throws Exception {
        socketModule.startSocketModule();
        server.start();
    }
}
