package com.chat.socketIO.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatus {
    private String room;
    private String sender;
    private boolean typing;
}