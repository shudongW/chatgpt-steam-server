package com.tech.chatgpt.service.Impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tech.chatgpt.OpenAiStreamClient;
import com.tech.chatgpt.entity.chat.ChatCompletion;
import com.tech.chatgpt.entity.chat.Message;
import com.tech.chatgpt.exception.BaseException;
import com.tech.chatgpt.config.LocalCache;
import com.tech.chatgpt.controller.request.ChatRequest;
import com.tech.chatgpt.controller.response.ChatResponse;
import com.tech.chatgpt.listener.OpenAIEventSourceListener;
import com.tech.chatgpt.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 *
 * @author https:www.tech.com
 * @date 2023-04-08
 */
@Service
@Slf4j
public class SseServiceImpl implements SseService {

    private final OpenAiStreamClient openAiStreamClient;

    public SseServiceImpl(OpenAiStreamClient openAiStreamClient) {
        this.openAiStreamClient = openAiStreamClient;
    }

    @Override
    public SseEmitter createSse(String uid) {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束连接...................", uid);
            LocalCache.CACHE.remove(uid);
        });
        //超时回调
        sseEmitter.onTimeout(() -> {
            log.info("[{}]连接超时...................", uid);
        });
        //异常回调
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info("[{}]连接异常,{}", uid, throwable.toString());
                        sseEmitter.send(SseEmitter.event()
                                .id(uid)
                                .name("发生异常！")
                                .data(Message.builder().content("发生异常请重试！").build())
                                .reconnectTime(3000));
                        LocalCache.CACHE.put(uid, sseEmitter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        LocalCache.CACHE.put(uid, sseEmitter);
        log.info("[{}]创建sse连接成功！", uid);
        return sseEmitter;
    }

    @Override
    public void closeSse(String uid) {
        SseEmitter sse = (SseEmitter) LocalCache.CACHE.get(uid);
        if (sse != null) {
            sse.complete();
            //移除
            LocalCache.CACHE.remove(uid);
        }
    }

    @Override
    public ChatResponse sseChat(String uid, ChatRequest chatRequest) {
        if (StrUtil.isBlank(chatRequest.getMsg())) {
            log.info("参数异常，msg为null", uid);
            throw new BaseException("参数异常，msg不能为空~");
        }
        String messageContext = (String) LocalCache.CACHE.get("msg" + uid);
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                messages = messages.subList(1, 10);
            }
            Message currentMessage = Message.builder().content(chatRequest.getMsg()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(chatRequest.getMsg()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }

        SseEmitter sseEmitter = (SseEmitter) LocalCache.CACHE.get(uid);

        if (sseEmitter == null) {
            log.info("聊天消息推送失败uid:[{}],没有创建连接，请重试。", uid);
            throw new BaseException("聊天消息推送失败uid:[{}],没有创建连接，请重试。~");
        }
        OpenAIEventSourceListener openAIEventSourceListener = new OpenAIEventSourceListener(sseEmitter);
        ChatCompletion completion = ChatCompletion
                .builder()
                .messages(messages)
                .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                .build();
        openAiStreamClient.streamChatCompletion(completion, openAIEventSourceListener);
        LocalCache.CACHE.put("msg" + uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        ChatResponse response = new ChatResponse();
        response.setQuestionTokens(completion.tokens());
        return response;
    }
}
