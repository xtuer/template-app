package newdt.dsc.ws.tio;

import lombok.extern.slf4j.Slf4j;
import newdt.dsc.ws.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

/**
 * WebSocket 核心类
 */
@Component
@Slf4j
public class WsMessageHandler implements IWsMsgHandler {
    @Autowired
    private WsMessageService msgService;

    @Autowired
    private WsMessageProcessor msgProcessor;

    /**
     * 握手时走这个方法，业务可以在这里获取 cookie，request 参数等，决定是否握手成功
     * 网址:
     *     网关的: ws://im.xtuer.com:9321?clientId=foo&token=xxx
     * 成功: 返回 httpResponse
     * 失败: 返回 null
     */
    @Override
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        WsParams params = WsParams.parseParams(httpRequest);

        if (!params.isValidated()) {
            return null;
        }

        log.info("[握手消息] 收到来自用户 [{}] 的客户端 ID [{}] 的 WS 握手包: {}\n{}",
                params.getUserId(), params.getClientId(),
                channelContext.getClientNode().toString(), httpRequest);

        channelContext.set(Const.KEY_WS_PARAMS, params);
        return msgService.login(channelContext, params) ? httpResponse : null;
    }

    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        int count = Tio.getAll(channelContext.tioConfig).getObj().size();
        String ipPort = channelContext.getClientNode().toString();
        WsParams params = (WsParams) channelContext.get(Const.KEY_WS_PARAMS);

        log.info("[建立连接] 用户 [{}] 的客户端 [{}] 建立了连接，共有 [{}] 人在线: {}", params.getUserId(), params.getClientId(), count, ipPort);
    }

    /**
     * 当客户端发 close flag 时，会走这个方法
     */
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        Tio.remove(channelContext, "receive close flag");
        return null;
    }

    /**
     * 字节消息（binaryType = arraybuffer）过来后会走这个方法
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        return null;
    }

    /*
     * 字符消息（binaryType = blob）过来后会走这个方法
     *
     * @return 返回给客户端的内容:
     * 1. 返回 null: 使用 Tio.sendToBsId(), Tio.sendToUser() 等发送 WsResponse 后，返回 null
     * 2. 返回字符串: 字符串内容为返回给前端的消息
     */
    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
        return msgProcessor.processMessage(text, channelContext);
    }
}
