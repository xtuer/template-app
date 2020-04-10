package handler;

import bean.Constants;
import bean.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.WsServerAioListener;
import service.MessageService;

/**
 * 断开连接前调用 onBeforeClose()
 */
@Component
@Slf4j
public class ServerAioListener extends WsServerAioListener {
    @Autowired
    private MessageService messageService;

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        super.onAfterConnected(channelContext, isConnected, isReconnect);

        if (log.isDebugEnabled()) {
            log.debug("onAfterConnected\n{}", channelContext);
        }
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        super.onAfterSent(channelContext, packet, isSentSuccess);

        if (log.isDebugEnabled()) {
            log.debug("onAfterSent\n{}\n{}", packet.logstr(), channelContext);
        }
    }

    /**
     * 断开连接前调用
     */
    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
        super.onBeforeClose(channelContext, throwable, remark, isRemove);

        if (log.isDebugEnabled()) {
            log.debug("onBeforeClose\n{}", channelContext);
        }

        // 提示: channelContext.toString() 为 server:0.0.0.0:3721, client:127.0.0.1:60610
        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute(Constants.KEY_DEFAULT_ATTRIBUTE);

        if (wsSessionContext != null && wsSessionContext.isHandshaked()) {
            int    count  = Tio.getAllChannelContexts(channelContext.groupContext).getObj().size() - 1;
            User   user   = messageService.getUser(channelContext);
            String ipPort = channelContext.getClientNode().toString();

            messageService.logout(channelContext);

            log.info("[离开] {} - {}({}) 离开了，共 {} 人在线", ipPort, user.getUsername(), user.getId(), count);
        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        super.onAfterDecoded(channelContext, packet, packetSize);

        if (log.isDebugEnabled()) {
            log.debug("onAfterDecoded\n{}\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        super.onAfterReceivedBytes(channelContext, receivedBytes);

        if (log.isDebugEnabled()) {
            log.debug("onAfterReceivedBytes\n{}", channelContext);
        }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        super.onAfterHandled(channelContext, packet, cost);

        if (log.isDebugEnabled()) {
            log.debug("onAfterHandled\n{}\n{}", packet.logstr(), channelContext);
        }
    }
}
