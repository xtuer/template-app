import config.ServerConfig;
import handler.IpStatListener;
import handler.MessageHandler;
import handler.ServerAioListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.tio.server.ServerGroupContext;
import org.tio.websocket.server.WsServerStarter;

/**
 * IM 消息服务器
 */
@Component
public class ImServer {
	@Value("${port}")      private int  port;      // 程序端口
	@Value("${heartbeat}") private Long heartbeat; // 心跳时间

	@Autowired private ServerAioListener serverAioListener;
	@Autowired private IpStatListener    ipStatListener;
	@Autowired private MessageHandler    messageHandler;

	/**
	 * 启动消息服务
	 */
	public void start() throws Exception {
		WsServerStarter wsServerStarter = new WsServerStarter(port, messageHandler);
		ServerGroupContext serverGroupContext = wsServerStarter.getServerGroupContext();
		serverGroupContext.setName(ServerConfig.PROTOCOL_NAME);
		serverGroupContext.setServerAioListener(serverAioListener);

		serverGroupContext.setIpStatListener(ipStatListener); // 设置 IP 监控
		serverGroupContext.ipStats.addDurations(ServerConfig.IpStatDuration.IPSTAT_DURATIONS); // 设置 IP 统计时间段
		serverGroupContext.setHeartbeatTimeout(heartbeat); // 设置心跳超时时间: 如果 <= 0 则关闭心跳检测

		wsServerStarter.start();
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("config/application.xml");
		ImServer imServer = context.getBean("imServer", ImServer.class);
		imServer.start(); // 启动 websocket server
	}
}
