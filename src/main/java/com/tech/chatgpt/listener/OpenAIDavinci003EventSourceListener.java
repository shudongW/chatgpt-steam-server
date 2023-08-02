package com.tech.chatgpt.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.chatgpt.entity.completions.CompletionResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

/**
 * 描述：OpenAI Davinci003
 */
@Slf4j
public class OpenAIDavinci003EventSourceListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    public OpenAIDavinci003EventSourceListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        if (data.equals("[DONE]")) {
            log.info("OpenAI返回数据结束了");
            sseEmitter.send(SseEmitter.event()
                    .id("[DONE]")
                    .data("[DONE]")
                    .reconnectTime(3000));
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        CompletionResponse completionResponse = mapper.readValue(data, CompletionResponse.class); // 读取Json
        sseEmitter.send(SseEmitter.event()
                .id(completionResponse.getId())
                .data(completionResponse.getChoices()[0])
                .reconnectTime(3000));
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if(Objects.isNull(response)){
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            String bodyString = body.string();
            log.error("OpenAI  sse连接异常data：{}，异常：{}", bodyString, t);
            sseEmitter.send(SseEmitter.event().id("cmpl-"+System.currentTimeMillis()).data("Error: " + bodyString).name("Error"));
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
            sseEmitter.send(SseEmitter.event().id("cmpl-"+System.currentTimeMillis()).data("Error: " + t.getMessage()).name("Error"));
        }
        eventSource.cancel();
    }
}
