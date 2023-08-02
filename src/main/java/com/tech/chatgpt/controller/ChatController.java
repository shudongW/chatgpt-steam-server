package com.tech.chatgpt.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tech.chatgpt.AzureOpenAISteamClient;
import com.tech.chatgpt.config.LocalCache;
import com.tech.chatgpt.entity.ChatObject;
import com.tech.chatgpt.listener.CompletionEventSourceListener;
import com.tech.chatgpt.listener.OpenAIEventSourceListener;
import com.tech.chatgpt.OpenAiStreamClient;
import com.tech.chatgpt.entity.chat.Message;
import com.tech.chatgpt.exception.BaseException;
import com.tech.chatgpt.exception.CommonError;
import com.tech.chatgpt.utils.BaiDuAiCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @date 2023-03-01
 */
@RequestMapping("/chat")
@Slf4j
@RestController
public class ChatController {

    private final OpenAiStreamClient openAiStreamClient;
    private final AzureOpenAISteamClient azureOpenAISteamClient;

    @Value("${gpt.channel}")
    private String channel;
    @Value("${azure.apiPath.AI35}")
    private String ai35;

    public ChatController(OpenAiStreamClient openAiStreamClient, AzureOpenAISteamClient azureOpenAISteamClient) {
        this.openAiStreamClient = openAiStreamClient;
        this.azureOpenAISteamClient = azureOpenAISteamClient;
    }


    @RequestMapping(value = "/v1", method = RequestMethod.GET)
    @CrossOrigin
    public SseEmitter chat(@RequestParam("message") String msg, @RequestHeader Map<String, String> headers) throws IOException {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        String uid = headers.get("uid");
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }
        if(BaiDuAiCheck.checkText(msg)){
            Message message = Message.builder().role(Message.Role.ASSISTANT).content("很抱歉！我无法回答你的问题。换个主题吧").build();
            sseEmitter.send(SseEmitter.event().id("chatcmpl-"+System.currentTimeMillis()).name("Filter").data(message).reconnectTime(3000));
            sseEmitter.send(SseEmitter.event().id("[DONE]").data("[DONE]").reconnectTime(3000));
            return sseEmitter;
        }
        log.info("msg: ["+msg+"], uuid:[" +uid+"]");

        String messageContext = (String) LocalCache.CACHE.get(uid);
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
        try {
            sseEmitter.send(SseEmitter.event().id(uid).name("连接成功！！！！").data(LocalDateTime.now()).reconnectTime(3000));
            sseEmitter.onCompletion(() -> {
                log.info(LocalDateTime.now() + ", uid#" + uid + ", on completion");
            });
            sseEmitter.onTimeout(() -> log.info(LocalDateTime.now() + ", uid#" + uid + ", on timeout#" + sseEmitter.getTimeout()));
            sseEmitter.onError(
                    throwable -> {
                        try {
                            log.info(LocalDateTime.now() + ", uid#" + "765431" + ", on error#" + throwable.toString());
                            sseEmitter.send(SseEmitter.event().id("765431").name("发生异常！").data(throwable.getMessage()).reconnectTime(3000));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
            if(channel.equalsIgnoreCase("azure")){
                log.info(LocalDateTime.now() + ", channel： azure");
                CompletionEventSourceListener openAIEventSourceListener = new CompletionEventSourceListener(sseEmitter);
                azureOpenAISteamClient.streamChatCompletion(messages, openAIEventSourceListener, ai35);
            }else {
                OpenAIEventSourceListener openAIEventSourceListener = new OpenAIEventSourceListener(sseEmitter);
                openAiStreamClient.streamChatCompletion(messages, openAIEventSourceListener);
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }
        LocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        return sseEmitter;
    }

    @RequestMapping(value = "/v2", method = RequestMethod.POST)
    @CrossOrigin
    public SseEmitter chat2(@RequestBody ChatObject chat, @RequestHeader Map<String, String> headers) throws IOException {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        String uid = headers.get("uid");
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }
        if(BaiDuAiCheck.checkText(chat.getMessage())){
            Message message = Message.builder().role(Message.Role.ASSISTANT).content("很抱歉！我无法回答你的问题。换个主题吧").build();
            sseEmitter.send(SseEmitter.event().id("chatcmpl-"+System.currentTimeMillis()).name("Filter").data(message).reconnectTime(3000));
            sseEmitter.send(SseEmitter.event().id("[DONE]").data("[DONE]").reconnectTime(3000));
            return sseEmitter;
        }
        log.info("msg: ["+chat.getMessage()+"], uuid:[" +uid+"]");

        String messageContext = (String) LocalCache.CACHE.get(uid);
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                messages = messages.subList(1, 10);
            }
            Message currentMessage = Message.builder().content(chat.getMessage()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(chat.getMessage()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }
        try {
            sseEmitter.send(SseEmitter.event().id(uid).name("连接成功！！！！").data(LocalDateTime.now()).reconnectTime(3000));
            sseEmitter.onCompletion(() -> {
                log.info(LocalDateTime.now() + ", uid#" + uid + ", on completion");
            });
            sseEmitter.onTimeout(() -> log.info(LocalDateTime.now() + ", uid#" + uid + ", on timeout#" + sseEmitter.getTimeout()));
            sseEmitter.onError(
                    throwable -> {
                        try {
                            log.info(LocalDateTime.now() + ", uid#" + "765431" + ", on error#" + throwable.toString());
                            sseEmitter.send(SseEmitter.event().id("765431").name("发生异常！").data(throwable.getMessage()).reconnectTime(3000));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
            if(channel.equalsIgnoreCase("azure")){
                log.info(LocalDateTime.now() + ", channel： azure");
                CompletionEventSourceListener openAIEventSourceListener = new CompletionEventSourceListener(sseEmitter);
                azureOpenAISteamClient.streamChatCompletion(messages, openAIEventSourceListener, ai35);
            }else {
                OpenAIEventSourceListener openAIEventSourceListener = new OpenAIEventSourceListener(sseEmitter);
                openAiStreamClient.streamChatCompletion(messages, openAIEventSourceListener);
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }
        LocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        return sseEmitter;
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET)
    public String clearSession(@RequestParam("uid") String uid) {
        LocalCache.CACHE.put(uid, "");
        log.info(LocalDateTime.now() + ", uid#" + uid + ", clear session success");
        return "success";
    }





    @GetMapping("/2")
    public String index() {
        return "2.html";
    }


//    private final SseService sseService;
//
//    public ChatController(SseService sseService) {
//        this.sseService = sseService;
//    }

    /**
     * 创建sse连接
     *
     * @param headers
     * @return
     */
//    @CrossOrigin
//    @GetMapping("/createSse")
//    public SseEmitter createConnect(@RequestHeader Map<String, String> headers) {
//        String uid = getUid(headers);
//        return sseService.createSse(uid);
//    }

    /**
     * 聊天接口
     *
     * @param chatRequest
     * @param headers
     */
//    @CrossOrigin
//    @PostMapping("/chat")
//    @ResponseBody
//    public ChatResponse sseChat(@RequestBody ChatRequest chatRequest, @RequestHeader Map<String, String> headers, HttpServletResponse response) {
//        String uid = getUid(headers);
//
//        return sseService.sseChat(uid, chatRequest);
//    }

    /**
     * 关闭连接
     *
     * @param headers
     */
//    @CrossOrigin
//    @GetMapping("/closeSse")
//    public void closeConnect(@RequestHeader Map<String, String> headers) {
//        String uid = getUid(headers);
//        sseService.closeSse(uid);
//    }


    /**
     * 获取uid
     *
     * @param headers
     * @return
     */
//    private String getUid(Map<String, String> headers) {
//        String uid = headers.get("uid");
//        if (StrUtil.isBlank(uid)) {
//            throw new BaseException(CommonError.SYS_ERROR);
//        }
//    }

}