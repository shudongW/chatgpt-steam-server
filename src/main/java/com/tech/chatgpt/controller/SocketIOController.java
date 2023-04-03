package com.tech.chatgpt.controller;

import com.tech.chatgpt.OpenAiStreamClient;
import com.tech.chatgpt.entity.ChatObject;
import com.tech.chatgpt.service.SocketIOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/socket")
public class SocketIOController {
    @Autowired
    private SocketIOService socketIOService;

    private final OpenAiStreamClient openAiStreamClient;

    public SocketIOController(OpenAiStreamClient openAiStreamClient) {
        this.openAiStreamClient = openAiStreamClient;
    }


    @GetMapping("/")
    public String index() {
        return "dist/index.html";
    }

    @RequestMapping(value = "/myServlet", method = RequestMethod.GET)
    public String MyHttpServlet(){
        return "ok";
    }

    // 推送信息给指定客户端
    @RequestMapping(value = "/sengMsg", method = RequestMethod.POST)
    public String SendMsg(@RequestBody ChatObject chat){
        socketIOService.sendMessageToAllUser(chat);
        return "发送成功!";
    }

    @RequestMapping(value = "/sengTMsg", method = RequestMethod.POST)
    public String SendTMsg(@RequestBody ChatObject chat){
        String msg = chat.getMessage();
        String userName = chat.getUserName();

//        log.info("msg: ["+msg+"], uuid:[" +userName+"]");
//        String messageContext = (String) LocalCache.CACHE.get(userName);
//
//        List<Message> messages = new ArrayList<>();
//        if (StrUtil.isNotBlank(messageContext)) {
//            messages = JSONUtil.toList(messageContext, Message.class);
//            if (messages.size() >= 10) {
//                messages = messages.subList(1, 10);
//            }
//            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
//            messages.add(currentMessage);
//        } else {
//            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
//            messages.add(currentMessage);
//        }
//        SocketIOClient client = socketIOService.getClientByUsername(userName);
//        SocketIOListener socketIOListener = new SocketIOListener(client);
//        openAiStreamClient.streamChatCompletion(messages, socketIOListener);
//        LocalCache.CACHE.put(userName, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);

        return "发送成功!";
    }


}
