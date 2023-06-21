package com.tech.chatgpt.entity;

import com.tech.chatgpt.entity.chat.Message;
import lombok.Data;

import java.util.List;

@Data
public class Chat {

    private String uid;

    private List<Message> message;
}
