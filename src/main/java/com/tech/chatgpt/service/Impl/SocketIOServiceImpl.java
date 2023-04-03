package com.tech.chatgpt.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tech.chatgpt.entity.ChatObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.tech.chatgpt.OpenAiStreamClient;
import com.tech.chatgpt.config.LocalCache;
import com.tech.chatgpt.entity.chat.Message;
import com.tech.chatgpt.handler.SocketIOMessageEventHandler;
import com.tech.chatgpt.listener.SocketIOListener;
import com.tech.chatgpt.service.SocketIOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SocketIOServiceImpl implements SocketIOService {

    @Autowired
    private SocketIOServer server;
    @Autowired
    private SocketIOMessageEventHandler socketIOHandler;
    @Resource
    private OpenAiStreamClient openAiStreamClient;



    //收到事件
    public static final String CLIENT_CHANNEL = "client_channel";
    //推送的事件
    public static final String SERVER_CHANNEL = "server_channel";
    private static Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>();



    // 客户端推送`client_info_event`事件时
    @OnEvent(value = CLIENT_CHANNEL)
    public void startOrderDetailChangeListener(SocketIOClient client, AckRequest request, ChatObject chat){
        //数据校验
        if(chat.getUserName() == null){
            return;
        }

        String sessionId = client.getSessionId().toString();
        log.info("SocketIO-消息通知-新增连接-sessionId: " + sessionId);
        clientMap.put(chat.getUserName(), client);

        String msg = chat.getMessage();
        String userName = chat.getUserName();

        log.info("msg: ["+msg+"], uuid:[" +userName+"]");
        String messageContext = (String) LocalCache.CACHE.get(userName);
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                messages = messages.subList(1, 10);
            }
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }
        SocketIOListener socketIOListener = new SocketIOListener(client);
        openAiStreamClient.streamChatCompletion(messages, socketIOListener);
        LocalCache.CACHE.put(userName, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        log.info("event end");
    }

    @Override
    public void sendMessageToAllUser(ChatObject chat) {
        if(clientMap.isEmpty()){
            return;
        }
        clientMap.entrySet().forEach(entry ->{
            entry.getValue().sendEvent(SERVER_CHANNEL, chat);
        });
    }

    @Override
    public void pushMessageToUser(ChatObject chat) {
        SocketIOClient client = getClientByUsername(chat.getUserName());
        if (client != null){

            client.sendEvent(SERVER_CHANNEL, chat.getMessage());
        }
    }

    /**
     * 获取连接的客户端ip地址
     *
     * @param client: 客户端
     * @return: java.lang.String
     */
    private String getIpByClient(SocketIOClient client) {
        String sa = client.getRemoteAddress().toString();
        String clientIp = sa.substring(1, sa.indexOf(":"));
        return clientIp;
    }

    /**
     * 此方法为获取client连接中的参数，可根据需求更改
     * @param client
     * @return
     */
    private String getParamsByClient(SocketIOClient client) {
        // 从请求的连接中拿出参数（这里的loginUserNum必须是唯一标识）
        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
        List<String> list = params.get("loginUserNum");
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
    @Override
    public SocketIOClient getClientByUsername(String userName) {
        SocketIOClient client = null;
        if(null != userName){
            return client;
        }
        if(clientMap.isEmpty()){
            return client;
        }
        for(String key : clientMap.keySet()){
            if(userName.equals(key)){
                client = clientMap.get(key);
            }
        }
        return client;
    }

    //观察者模式中的通知
    @Override
    public void update(Observable o, Object arg) {
        if(!( o instanceof SocketIOMessageEventHandler)){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        if(arg instanceof Map){
            map =(Map<String, Object>) arg;
        }
        log.info("客户端收到通知：" + map.toString());
        Object type = map.get("type");
        if(null == type){
            return;
        }
        if(type.equals("disconnect")){
            this.disconnect(map);
        }

    }

    private void disconnect(Map<String, Object> map){
        Object sessionId = map.get("sessionId");
        if(null == sessionId){
            return ;
        }
        List<String> keyList = clientMap.keySet().parallelStream().filter(k->k.equals(sessionId.toString())).collect(Collectors.toList());
        if(null != keyList && keyList.size() > 0){
            clientMap.remove(keyList.get(0));
        }
    }

    // 注册观察者模式
    @Override
    public void afterPropertiesSet() throws Exception {
        // spring 为bean实现提供了两种实现：配置init-methid ，实现initializingBean；
        // 只要实现initializingBean接口，spring就会在类初始化时自动调用该afterPropertiesSet方法

        //将对象注册进观察者模式中
        socketIOHandler.addObserver(this);
    }
}
