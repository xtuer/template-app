package newdt.dsc.ws.tio;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.tio.server.ServerTioConfig;
import org.tio.websocket.server.WsServerStarter;

import java.util.concurrent.TimeUnit;

/**
 * Websocket 服务器，启动 Websocket 服务: new WsServer().start()
 */
// @Component
@Getter
@Setter
@Slf4j
public class WsServer implements ApplicationRunner {
    /**
     * 程序端口。
     */
    @Value("${ws.port:8501}")
    private int port;

    /**
     * 心跳时间，单位秒。
     */
    @Value("${ws.heartbeat:300}")
    private int heartbeat;

    @Autowired
    private WsAioListener aioListener;

    @Autowired
    private WsIpStatListener ipStatListener;

    @Autowired
    private WsMessageHandler msgHandler;

    /**
     * 服务器端的 TioConfig
     */
    public static ServerTioConfig tioConfig;

    /**
     * 启动 Websocket 服务
     */
    public void start() throws Exception {
        WsServerStarter wsServerStarter = new WsServerStarter(port, msgHandler);

        tioConfig = wsServerStarter.getServerTioConfig();
        tioConfig.setName(WsServerConfig.PROTOCOL_NAME);
        tioConfig.setServerAioListener(aioListener);
        tioConfig.setIpStatListener(ipStatListener); // 设置 IP 监控
        tioConfig.ipStats.addDurations(WsServerConfig.IpStatDuration.IP_STAT_DURATIONS); // 设置 IP 统计时间段
        tioConfig.setHeartbeatTimeout(TimeUnit.SECONDS.toMillis(heartbeat)); // 设置心跳超时时间: 如果 <= 0 则关闭心跳检测

        log.info("[操作] 启动 Websocket 服务, 端口: {}", port);
        wsServerStarter.start();
    }

    @Autowired
    private WsServer self;

    /**
     * 启动 Websocket 服务
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        self.start();
    }
}
