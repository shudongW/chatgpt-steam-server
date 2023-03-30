package com.chatgpt.steam.server.service;

import com.chatgpt.steam.server.entity.ChatObject;
import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.beans.factory.InitializingBean;

import java.util.Observer;

public interface SocketIOService extends Observer, InitializingBean {



    void sendMessageToAllUser(ChatObject chat);
    // 推送信息
    void pushMessageToUser(ChatObject chat);

    SocketIOClient getClientByUsername(String userName);

}
