package newdt.dsc.ws.tio;

import lombok.extern.slf4j.Slf4j;
import newdt.dsc.ws.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.WsServerAioListener;

/**
 * 用户根据情况来完成该类的实现，可能需要实现的是 onBeforeClose 处理离开前通知组里其他成员
 */
@Component
@Slf4j
public class WsAioListener extends WsServerAioListener {
    @Autowired
    private WsMessageService msgService;

    /**
     * 连接断开前调用
     */
    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
        super.onBeforeClose(channelContext, throwable, remark, isRemove);

        if (log.isTraceEnabled()) {
            log.trace("onBeforeClose\r\n{}", channelContext);
        }

        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.get();

        if (wsSessionContext != null && wsSessionContext.isHandshaked()) {
            // 提示: channelContext.toString() 为 server:0.0.0.0:3721, client:127.0.0.1:60610
            String ipPort = channelContext.getClientNode().toString();
            int count = Tio.getAll(channelContext.tioConfig).getObj().size() - 1;
            WsParams params = (WsParams) channelContext.get(Const.KEY_WS_PARAMS);

            msgService.logout(channelContext);

            log.info("[断开连接] 用户 [{}] 的客户端 [{}] 断开了连接，共有 [{}] 人在线: {}", params.getUserId(), params.getClientId(), count, ipPort);
        }
    }

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        super.onAfterConnected(channelContext, isConnected, isReconnect);

        if (log.isTraceEnabled()) {
            log.trace("onAfterConnected\r\n{}", channelContext);
        }
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        super.onAfterSent(channelContext, packet, isSentSuccess);

        if (log.isTraceEnabled()) {
            log.trace("onAfterSent\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        super.onAfterDecoded(channelContext, packet, packetSize);

        if (log.isTraceEnabled()) {
            log.trace("onAfterDecoded\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        super.onAfterReceivedBytes(channelContext, receivedBytes);

        if (log.isTraceEnabled()) {
            log.trace("onAfterReceivedBytes\r\n{}", channelContext);
        }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        super.onAfterHandled(channelContext, packet, cost);

        if (log.isTraceEnabled()) {
            log.trace("onAfterHandled\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }
}
