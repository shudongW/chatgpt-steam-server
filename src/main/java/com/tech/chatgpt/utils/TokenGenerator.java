package com.tech.chatgpt.utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public class TokenGenerator {

    private static final String SECRET_KEY = "alight_gpt_secret"; // 秘钥
    private static final long EXPIRE_TIME = 24 * 60 * 60; // 过期1天


    public static String generateToken() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 将当前时间转换成年月日时0分0秒
        LocalDateTime zeroTime = now.withMinute(0).withSecond(0).withNano(0);
        // 将年月日时0分0秒转换成时间戳
        long timestamp = zeroTime.toEpochSecond(ZoneOffset.of("+8"));
        String data = SECRET_KEY + timestamp; // 拼接秘钥和时间戳
        String signature = md5(data); // 对拼接后的字符串进行MD5加密
        String token = timestamp + ":" + signature; // 将时间戳和签名用冒号连接起来作为token
        return token;
    }

    public static boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String[] parts = token.split(":");
        if (parts.length != 2) {
            return false;
        }
        long timestamp = Long.parseLong(parts[0]);
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 将当前时间转换成年月日时0分0秒
        LocalDateTime zeroTime = now.withMinute(0).withSecond(0).withNano(0);
        // 将年月日时0分0秒转换成时间戳
        long currentTimestamp = zeroTime.toEpochSecond(ZoneOffset.of("+8"));
//        System.out.println(currentTimestamp + "-" + timestamp+ " = " + (currentTimestamp - timestamp) );
        if (currentTimestamp - timestamp > EXPIRE_TIME) { // 判断token是否过期
            return false;
        }
        String signature = parts[1];
        String data = SECRET_KEY + timestamp;
        String md5 = md5(data);
        return signature.equals(md5);
    }

    private static String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        System.out.println(TokenGenerator.generateToken());
//        String token = TokenGenerator.generateToken();
//        System.out.println(verifyToken(token));
//    }
}
