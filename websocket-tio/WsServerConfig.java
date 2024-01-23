package newdt.dsc.ws.tio;

import org.tio.utils.time.Time;

public abstract class WsServerConfig {
    /**
     * 监听端口
     */
    public static final int SERVER_PORT = 9321;

    /**
     * 协议名字 (可以随便取，主要用于开发人员辨识)
     */
    public static final String PROTOCOL_NAME = "dsc";

    /**
     * 字符集
     */
    public static final String CHARSET = "UTF-8";

    /**
     * 监听的 IP
     */
    public static final String SERVER_IP = null; // null 表示监听所有，并不指定 IP

    /**
     * 心跳超时时间，单位: 毫秒
     */
    public static final int HEARTBEAT_TIMEOUT = 60 * 1000;

    /**
     * IP 数据监控统计，时间段
     */
    public static interface IpStatDuration {
        Long DURATION_1 = Time.MINUTE_1 * 5;
        Long[] IP_STAT_DURATIONS = new Long[] { DURATION_1 };
    }
}
