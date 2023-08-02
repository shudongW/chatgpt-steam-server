package com.tech.chatgpt.utils;

import com.baidu.aip.contentcensor.AipContentCensor;
import org.springframework.beans.factory.annotation.Value;

/**
 * 初始化敏感词
 */
public class BaiDuAiConfig {
    private static AipContentCensor instance;
    //设置APPID/AK/SK
    @Value("${filter.baidu.appId}")
    public static String APP_ID;
    @Value("${filter.baidu.apiKey}")
    public static String API_KEY;
    @Value("${filter.baidu.secretKey}")
    public static String SECRET_KEY;


    public static synchronized AipContentCensor getInstance() {
        if (instance == null) {
            instance = new AipContentCensor(APP_ID, API_KEY, SECRET_KEY);
            // 可选：设置网络连接参数
            instance.setConnectionTimeoutInMillis(2000);
            instance.setSocketTimeoutInMillis(60000);
        }
        return instance;
    }




//    public boolean containsSensitiveWord(String text) {
//        for (String word : sensitiveWords) {
//            if (text.contains(word)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public String filterSensitiveWord(String text) {
//        for (String word : sensitiveWords) {
//            text = text.replaceAll(word, "***");
//        }
//        return text;
//    }


}
