package handler;

import bean.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;
import service.MessageService;

/**
 * Tio 的 WebSocket 消息处理器 (用户只有加入小组后才能给其他用户发送消息):
 *     建立连接前握手调用 handshake()，可以在里面验证客户端的权限，例如使用 JWT
 *     建立连接时调用   onAfterHandshaked()
 *     消息到达时调用   onText(): 消息处理的核心函数
 *     断开连接前会调用 ServerAioListener.onBeforeClose()
 */
@Component
public class MessageHandler implements IWsMsgHandler {
	private static Logger logger = LoggerFactory.getLogger(MessageHandler.class);

	@Autowired private MessageService messageService;

	/**
	 * 字符消息 (binaryType = blob) 到达后会调用这个方法
	 */
	@Override
	public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) {
		return messageService.processMessage(text, channelContext);
	}

	/**
	 * 握手时走这个方法，业务可以在这里获取 cookie，request 参数等
	 */
	@Override
	public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) {
		String userId   = StringUtils.trim(request.getParam("userId"));
		String username = StringUtils.trim(request.getParam("username"));

		logger.info("收到来自 {}({}) 的 WS 握手包: {}\n{}", username, userId, channelContext.getClientNode().toString(), request.toString());
		return messageService.login(request, channelContext) ? httpResponse : null;
	}

	@Override
	public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) {
		int    count  = Tio.getAllChannelContexts(channelContext.groupContext).getObj().size();
		String ipPort = channelContext.getClientNode().toString();
		User   user   = messageService.getUser(channelContext);

		logger.info("{} - {}({}) 进来了，共 {} 人在线", ipPort, user.getUsername(), user.getId(), count);
	}

	/**
	 * 字节消息 (binaryType = arraybuffer) 过来后会走这个方法
	 */
	@Override
	public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		return null;
	}

	/**
	 * 当客户端发 close flag 时，会走这个方法
	 */
	@Override
	public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		Tio.remove(channelContext, "receive close flag");
		return null;
	}
}
