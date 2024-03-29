package com.tech.chatgpt.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tech.chatgpt.AzureOpenAIClient;
import com.tech.chatgpt.AzureOpenAISteamClient;
import com.tech.chatgpt.OpenAiClient;
import com.tech.chatgpt.OpenAiStreamClient;
import com.tech.chatgpt.config.LocalCache;
import com.tech.chatgpt.entity.chat.ChatCompletion;
import com.tech.chatgpt.entity.chat.ChatCompletionResponse;
import com.tech.chatgpt.entity.chat.Message;
import com.tech.chatgpt.entity.common.Choice;
import com.tech.chatgpt.entity.completions.Completion;
import com.tech.chatgpt.exception.BaseException;
import com.tech.chatgpt.exception.CommonError;
import com.tech.chatgpt.listener.CompletionEventSourceListener;
import com.tech.chatgpt.listener.Davinci003EventSourceListener;
import com.tech.chatgpt.listener.OpenAIDavinci003EventSourceListener;
import com.tech.chatgpt.listener.OpenAIEventSourceListener;
import com.tech.chatgpt.utils.BaiDuAiCheck;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @date 2023-03-01
 */
@RequestMapping("/api")
@Slf4j
@RestController
public class CommonController {

    private final OpenAiClient openAiClient;
    private final OpenAiStreamClient openAiStreamClient;

    private final AzureOpenAISteamClient azureOpenAISteamClient;

    private final AzureOpenAIClient azureOpenAIClient;

    @Value("${gpt.channel}")
    private String channel;
    @Value("${azure.apiPath.AI35}")
    private String ai35;
    @Value("${azure.apiPath.Davinci003}")
    private String davinci003;



    public CommonController(OpenAiClient openAiClient, OpenAiStreamClient openAiStreamClient, AzureOpenAISteamClient azureOpenAISteamClient, AzureOpenAIClient azureOpenAIClient) {
        this.openAiClient = openAiClient;
        this.openAiStreamClient = openAiStreamClient;
        this.azureOpenAISteamClient = azureOpenAISteamClient;
        this.azureOpenAIClient = azureOpenAIClient;
    }


    @RequestMapping(value = "/sd", method = RequestMethod.GET)
    @CrossOrigin
    public String chatBySD(@RequestParam("question") String question) {
        if(BaiDuAiCheck.checkText(question)){
            return "Filter: 很抱歉！我无法回答你的问题。换个主题吧";
        }
        //聊天模型：gpt-3.5
        Message message = Message.builder().role(Message.Role.USER).content(question).build();
        ChatCompletion chatCompletion = ChatCompletion
                .builder()
                .messages(Arrays.asList(message))
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        StringBuilder sb = new StringBuilder();
        try {
            if(channel.equalsIgnoreCase("azure")){
                log.info(LocalDateTime.now() + ", channel： azure");
                ChatCompletionResponse chatCompletionResponse = azureOpenAIClient.chatCompletion(chatCompletion, ai35);
                chatCompletionResponse.getChoices().forEach(e -> {
                    sb.append(e.getMessage().getContent());
                });
            }else {
                ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
                chatCompletionResponse.getChoices().forEach(e -> {
                    sb.append(e.getMessage().getContent());
                });
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }

        return sb.toString();
    }

    @RequestMapping(value = "/chat", method = RequestMethod.GET)
    @CrossOrigin
    public String chat(@RequestParam("question") String question, @RequestHeader Map<String, String> headers) {
        if(BaiDuAiCheck.checkText(question)){
            return "Filter: 很抱歉！我无法回答你的问题。换个主题吧";
        }
        String uid = headers.get("uid");
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }
        log.info("question: ["+question+"], uid:[" +uid+"]");
        String messageContext = (String) LocalCache.CACHE.get(uid);
        log.info("messageContext: [" + messageContext + "]");
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                messages = messages.subList(1, 10);
            }
            Message currentMessage = Message.builder().content(question).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(question).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }
        //聊天模型：gpt-3.5
//        Message message = Message.builder().role(Message.Role.USER).content(question).build();
        ChatCompletion chatCompletion = ChatCompletion
                .builder()
                .messages(messages)
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        StringBuilder sb = new StringBuilder();
        try {
            if(channel.equalsIgnoreCase("azure")){
                log.info(LocalDateTime.now() + ", channel： azure");
                ChatCompletionResponse chatCompletionResponse = azureOpenAIClient.chatCompletion(chatCompletion, ai35);
                chatCompletionResponse.getChoices().forEach(e -> {
                    sb.append(e.getMessage().getContent());
                });
            }else {
                ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
                chatCompletionResponse.getChoices().forEach(e -> {
                    sb.append(e.getMessage().getContent());
                });
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }

        log.info("aws:" + sb.toString());
        LocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        return sb.toString();
    }

    @RequestMapping(value = "/chain", method = RequestMethod.POST)
    @CrossOrigin
    public String chatPost(@RequestBody ChatCompletion chatCompletion, @RequestHeader Map<String, String> headers) {
        StringBuilder txt = new StringBuilder();
        for(Message msg : chatCompletion.getMessages()){
            txt.append(msg.getContent());
        }
        if(BaiDuAiCheck.checkText(txt.toString())){
            return "Filter: 很抱歉！我无法回答你的问题。换个主题吧";
        }

        String uid = headers.get("uid");
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }
//        String question = json.getStr("message");
//        if(StringUtil.isNullOrEmpty(question)){
//            return "message is null";
//        }
//        log.info("question: ["+question+"], uid:[" +uid+"]");
//        String messageContext = (String) LocalCache.CACHE.get(uid);
//        log.info("messageContext: [" + messageContext + "]");
//        List<Message> messages = new ArrayList<>();
//        if (StrUtil.isNotBlank(messageContext)) {
//            messages = JSONUtil.toList(messageContext, Message.class);
//            if (messages.size() >= 10) {
//                messages = messages.subList(1, 10);
//            }
//            Message currentMessage = Message.builder().content(question).role(Message.Role.USER).build();
//            messages.add(currentMessage);
//        } else {
//            Message currentMessage = Message.builder().content(question).role(Message.Role.USER).build();
//            messages.add(currentMessage);
//        }
        //聊天模型：gpt-3.5
//        Message message = Message.builder().role(Message.Role.USER).content(question).build();
//        ChatCompletion chatCompletion = ChatCompletion
//                .builder()
//                .messages(messages)
//                .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
//                .build();
        StringBuilder sb = new StringBuilder();
        try {
            if(channel.equalsIgnoreCase("azure")){
                log.info(LocalDateTime.now() + ", channel： azure");
                ChatCompletionResponse chatCompletionResponse = azureOpenAIClient.chatCompletion(chatCompletion, ai35);
                chatCompletionResponse.getChoices().forEach(e -> {
                    sb.append(e.getMessage().getContent());
                });
            }else {
                ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
                chatCompletionResponse.getChoices().forEach(e -> {
                    sb.append(e.getMessage().getContent());
                });
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }
        log.info("aws:" + sb.toString());
//        LocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        return sb.toString();
    }

    @RequestMapping(value = "/stream/chat/completion", method = RequestMethod.POST)
    @CrossOrigin
    public SseEmitter chatByCompletion(@RequestBody Completion completion, @RequestHeader Map<String, String> headers) throws IOException {

        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        String uid = headers.get("uid");
        if(BaiDuAiCheck.checkText(completion.getPrompt())){
            Choice choice = new Choice();
            choice.setText("很抱歉！我无法回答你的问题。换个主题吧");
            choice.setIndex(0);
            sseEmitter.send(SseEmitter.event().id("cmpl-"+System.currentTimeMillis()).name("Filter").data(choice).reconnectTime(3000));
            sseEmitter.send(SseEmitter.event().id("[DONE]").data("[DONE]").reconnectTime(3000));
            return sseEmitter;
        }
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }
        if (completion == null) {
            throw new BaseException(CommonError.SYS_ERROR);
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
                Davinci003EventSourceListener openAIEventSourceListener = new Davinci003EventSourceListener(sseEmitter);
                azureOpenAISteamClient.streamCompletions(completion, openAIEventSourceListener, davinci003);
            }else {
                OpenAIDavinci003EventSourceListener openAIEventSourceListener = new OpenAIDavinci003EventSourceListener(sseEmitter);
                openAiStreamClient.streamCompletions(completion, openAIEventSourceListener);
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }
        return sseEmitter;
    }

    @RequestMapping(value = "/stream/chat", method = RequestMethod.POST)
    @CrossOrigin
    public SseEmitter ChatByChatCompletion(@RequestBody ChatCompletion chatCompletion, @RequestHeader Map<String, String> headers) throws IOException {
        StringBuilder txt = new StringBuilder();
        for(Message msg : chatCompletion.getMessages()){
            txt.append(msg.getContent());
        }

        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        String uid = headers.get("uid");
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }
        if(BaiDuAiCheck.checkText(txt.toString())){
            Message message = Message.builder().role(Message.Role.ASSISTANT).content("很抱歉！我无法回答你的问题。换个主题吧").build();
            sseEmitter.send(SseEmitter.event().id("chatcmpl-"+System.currentTimeMillis()).name("Filter").data(message).reconnectTime(3000));
            sseEmitter.send(SseEmitter.event().id("[DONE]").data("[DONE]").reconnectTime(3000));
            return sseEmitter;
        }
        if (chatCompletion == null) {
            throw new BaseException(CommonError.SYS_ERROR);
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
                azureOpenAISteamClient.streamChatCompletion(chatCompletion, openAIEventSourceListener, ai35);
            }else {
                OpenAIEventSourceListener openAIEventSourceListener = new OpenAIEventSourceListener(sseEmitter);
                openAiStreamClient.streamChatCompletion(chatCompletion, openAIEventSourceListener);
            }
        } catch (Exception e) {
            throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
        }
        
        return sseEmitter;
    }




}
