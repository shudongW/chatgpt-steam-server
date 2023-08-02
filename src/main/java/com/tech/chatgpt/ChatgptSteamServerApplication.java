package com.tech.chatgpt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

/**
 * 描述：ChatgptSteamServerApplication
 */
@SpringBootApplication
public class ChatgptSteamServerApplication {

    @Value("${chatgpt.apiKey}")
    private String apiKey;
    @Value("${chatgpt.apiHost}")
    private String apiHost;
    @Value("${chatgpt.apiHostProxy}")
    private String apiHostProxy;
    @Value("${azure.apiKey}")
    private String azureKey;
    @Value("${azure.apiHost}")
    private String azureHost;


    public static void main(String[] args) {
        SpringApplication.run(ChatgptSteamServerApplication.class, args);
    }


    @Bean
    public OpenAiStreamClient openAiStreamClient() {
        return OpenAiStreamClient.builder().apiHost(apiHost).apiKey(Collections.singletonList(apiKey)).build();
    }

    @Bean
    public OpenAiClient openAiClient() {
        return OpenAiClient.builder().apiHost(apiHost).apiKey(Collections.singletonList(apiKey)).build();
    }

    @Bean
    public AzureOpenAISteamClient azureOpenAISteamClient() {
        return AzureOpenAISteamClient.builder().apiHost(azureHost).apiKey(Collections.singletonList(azureKey)).build();
    }

    @Bean
    public AzureOpenAIClient azureOpenAIClient() {
        return AzureOpenAIClient.builder().apiHost(azureHost).apiKey(Collections.singletonList(azureKey)).build();
    }


//    @Bean
//    public OpenAiStreamClient openAiStreamClient() {
//        //本地开发需要配置代理地址
////        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
//        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
//        //!!!!!!测试或者发布到服务器千万不要配置Level == BODY!!!!
//        //!!!!!!测试或者发布到服务器千万不要配置Level == BODY!!!!
//        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
//        OkHttpClient okHttpClient = new OkHttpClient
//                .Builder()
////                .proxy(proxy)
//                .addInterceptor(httpLoggingInterceptor)
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(600, TimeUnit.SECONDS)
//                .readTimeout(600, TimeUnit.SECONDS)
//                .build();
//        return OpenAiStreamClient
//                .builder()
//                .apiHost(apiHost)
//                .apiKey(apiKey)
//                //自定义key使用策略 默认随机策略
//                .keyStrategy(new KeyRandomStrategy())
//                .okHttpClient(okHttpClient)
//                .build();
//    }

}
