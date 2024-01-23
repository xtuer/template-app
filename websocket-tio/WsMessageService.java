package newdt.dsc.ws.tio;

import lombok.extern.slf4j.Slf4j;
import newdt.dsc.util.Utils;
import newdt.dsc.ws.Const;
import newdt.dsc.ws.msg.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

/**
 * 提示: IP:Port 的连接对应一个 ChannelContext 对象
 */
@Service
@Slf4j
public class WsMessageService {
    /**
     * 登录。
     *
     * 提示: 每个用户可能在多个页面打开 SQL 窗口，每个 SQL 窗口会建立一个 Socket 连接，
     *      所以一个用户 ID 可能会同时有多个 socket clients。
     *      而消息只会发送到对应的 socket client，所以使用 clientId 和 channelContext 进行绑定，
     *      而不是 userId 和 channelContext 绑定。
     *
     * @param channelContext ChannelContext 对象
     * @param params Websocket 连接参数
     * @return 连接成功返回 true, 否则返回 false
     */
    public boolean login(ChannelContext channelContext, WsParams params) {
        /*
         逻辑:
         1. 获取客户端信息。
         2. 绑定客户端对象。
         */
        channelContext.set(Const.KEY_USER_ID, params.getUserId());
        Tio.bindBsId(channelContext, params.getClientId());
        return true;
    }

    /**
     * 注销。
     *
     * @param channelContext ChannelContext 对象
     */
    public void logout(ChannelContext channelContext) {
        // 与 channelContext 解绑
        channelContext.remove(Const.KEY_USER_ID);
        Tio.unbindBsId(channelContext);
    }

    /**
     * 异步给客户端发送消息。
     */
    public static void sendToClient(String clientId, Message message) {
        sendToClient(clientId, Utils.toJson(message), false);
    }

    /**
     * 异步给客户端发送消息。
     */
    public static void sendToClient(String clientId, String jsonMessage) {
        sendToClient(clientId, jsonMessage, false);
    }

    /**
     * 给客户端发送消息。
     *
     * @param clientId 客户端 ID。
     * @param message 要发送的消息。
     * @param blocked 是否阻塞发送。
     */
    public static void sendToClient(String clientId, Message message, boolean blocked) {
        sendToClient(clientId, Utils.toJson(message), blocked);
    }

    /**
     * 给客户端发送消息。
     *
     * @param clientId 客户端 ID。
     * @param jsonMessage 要发送的消息 (JSON 格式的字符串)。
     * @param blocked 是否阻塞发送。
     */
    public static void sendToClient(String clientId, String jsonMessage, boolean blocked) {
        if (!StringUtils.hasText(clientId)) {
            return;
        }

        ChannelContext ctx = Tio.getByBsId(WsServer.tioConfig, clientId);

        if (ctx != null) {
            WsResponse response = WsResponse.fromText(jsonMessage, WsServerConfig.CHARSET);

            if (blocked) {
                Tio.sendToBsId(WsServer.tioConfig, clientId, response);
            } else {
                Tio.bSendToBsId(WsServer.tioConfig, clientId, response);
            }
        } else {
            log.debug("客户端不存在: {}", clientId);
        }
    }
}
