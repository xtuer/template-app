package bean;

/**
 * 常量
 */
public interface Constants {
    String KEY_USER = "ws_user";    // 用户的 key，用于把用户绑定到 channelContext 上
    String KEY_KICK_OUT = "ws_kickout"; // 重复登录时被踢掉标志的 key
    String KEY_DEFAULT_ATTRIBUTE = "t-io-d-a-k"; // 和 ChannelContext.DEFAULT_ATTRIBUTE_KEY 一样
}
