package com.chatgpt.steam.server.config;

import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

//    @Value("${socketio.host}")
    private String host = "XXX.XXX.XXX.XXX";

//    @Value("${socketio.port}")
    private Integer port = 9092;

    //socket连接数大小（如只监听一个端口boss线程组为1即可）
//    @Value("${socketio.bossCount}")
    private int bossCount = 1;

//    @Value("${socketio.workCount}")
    private int workCount = 100;

//    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests=true;

    //协议升级超时时间（毫秒），默认10秒。HTTP握手升级为ws协议超时时间
//    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout = 10000;

    //Ping消息超时时间（毫秒），默认60秒，这个时间间隔内没有接收到心跳消息就会发送超时事件
//    @Value("${socketio.pingTimeout}")
    private int pingTimeout = 60000;

    // Ping消息间隔（毫秒），默认25秒。客户端向服务器发送一条心跳消息间隔
//    @Value("${socketio.pingInterval}")
    private int pingInterval = 60000;

    /**
     * 以下配置在上面的application.properties中已经注明
     * @return
     */
    @Bean
    public SocketIOServer socketIOServer() {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setSocketConfig(socketConfig);
        config.setHostname(host);
        config.setPort(port);
        config.setBossThreads(bossCount);
        config.setWorkerThreads(workCount);
        config.setAllowCustomRequests(allowCustomRequests);
        config.setUpgradeTimeout(upgradeTimeout);
        config.setPingTimeout(pingTimeout);
        config.setPingInterval(pingInterval);
        return new SocketIOServer(config);
    }

    /**
     * 扫描注解
     * @param socketIOServer
     * @return
     */
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer){
        return new SpringAnnotationScanner(socketIOServer);
    }
}
