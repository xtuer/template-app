package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.intf.Packet;
import org.tio.core.stat.IpStat;

/**
 * IP 监听器
 * 记录每一个 IP 的流量, 访问时间, 解码次数等, 具体请参考类 IpStat
 */
@Component
public class IpStatListener implements org.tio.core.stat.IpStatListener {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(IpStatListener.class);

	/**
	 *
	 */
	private IpStatListener() {
	}

	@Override
	public void onExpired(GroupContext groupContext, IpStat ipStat) {
		// 在这里把统计数据入库中或日志
		// if (log.isInfoEnabled()) {
		// 	log.info("可以把统计数据入库\n{}", Json.toFormatedJson(ipStat));
		// }
	}

	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect, IpStat ipStat) throws Exception {
		// if (log.isInfoEnabled()) {
		// 	log.info("onAfterConnected\n{}", Json.toFormatedJson(ipStat));
		// }
	}

	@Override
	public void onDecodeError(ChannelContext channelContext, IpStat ipStat) {
		// if (log.isInfoEnabled()) {
		// 	log.info("onDecodeError\n{}", Json.toFormatedJson(ipStat));
		// }
	}

	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess, IpStat ipStat) throws Exception {
		// if (log.isInfoEnabled()) {
		// 	log.info("onAfterSent\n{}\n{}", packet.logstr(), Json.toFormatedJson(ipStat));
		// }
	}

	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize, IpStat ipStat) throws Exception {
		// if (log.isInfoEnabled()) {
		// 	log.info("onAfterDecoded\n{}\n{}", packet.logstr(), Json.toFormatedJson(ipStat));
		// }
	}

	@Override
	public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes, IpStat ipStat) throws Exception {
		// if (log.isInfoEnabled()) {
		// 	log.info("onAfterReceivedBytes\n{}", Json.toFormatedJson(ipStat));
		// }
	}

	@Override
	public void onAfterHandled(ChannelContext channelContext, Packet packet, IpStat ipStat, long cost) throws Exception {
		// if (log.isInfoEnabled()) {
		// 	log.info("onAfterHandled\n{}\n{}", packet.logstr(), Json.toFormatedJson(ipStat));
		// }
	}

}
