package com.chat.socketIO.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@ToString
@Document(collection = "messages")
public class Message implements Serializable {
    @Id
    private String id;
    private MessageType type;
    private String text;
    private String room;
    private String sender;
    private byte[] mediaFile;
    private String mediaFileName;
    private String mediaFileType;
    private String status;

    public Message() {}

    public Message(MessageType type, String message) {
        this.type = type;
        this.text = message;
    }

    public Message(String message) {
        this.text = message;
    }

    public Message(String message, String room, String sender) {
        this.text = message;
        this.room = room;
        this.sender = sender;
    }

    public Message(String message, String room, String sender, byte[] mediaFile, String mediaFileName, String mediaFileType) {
        this.text = message;
        this.room = room;
        this.sender = sender;
        this.mediaFile = mediaFile;
        this.mediaFileName = mediaFileName;
        this.mediaFileType = mediaFileType;
    }
}