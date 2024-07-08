package com.chat.socketIO.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@ToString
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String token;
    private String username;
    private String room;

    // Getters and Setters
}
