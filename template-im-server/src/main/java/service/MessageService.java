package service;

import bean.Constants;
import bean.Message;
import bean.User;
import com.alibaba.fastjson.JSON;
import config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsResponse;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 群消服务:
 *     加入小组
 *     离开小组
 *     离开所有小组
 *     获取小组成员
 *     获取小组历史消息
 *     获取私有聊天历史消息
 *     发送小组消息调用 sendToGroup()
 *     发送私人消息调用 sendToUser()
 *
 * 提示: IP:Port 的连接对应一个 ChannelContext 对象
 */
@Service
@Slf4j
public class MessageService {
    @Autowired
    private MessageDao messageDao;

    /**
     * 处理消息
     *
     * @param text 消息的原始字符串
     * @param channelContext ChannelContext 对象
     * @return 返回消息处理结果
     */
    public Object processMessage(String text, ChannelContext channelContext) {
        // 逻辑:
        // 1. 转换消息为 Message 对象，如果转换出错则消息格式不对，return 错误信息告知发送者
        // 2. 根据消息的类型分别进行处理
        //    2.1 加入小组消息:
        //    2.2 离开小组消息
        //    2.3 发送小组消息
        //    2.4 获取小组成员
        //    2.5 发送私聊消息
        //    2.6 获取小组历史信息: to 为 groupName, content 为页码
        //    2.7 获取私有历史信息: from 和 to 为用户 ID, content 为页码
        //    2.8 获取服务器当前的连接数
        //    2.9 不支持的消息
        try {
            // [1] 转换消息为 Message 对象，如果转换出错则消息格式不对，return 错误信息告知发送者
            Message message = JSON.parseObject(text, Message.class);
            String  userId  = message.getFrom();
            String  to      = message.getTo(); // 可能是小组名, 也可能是目标用户 ID, 也可能是空
            message.setFromUsername(getUser(channelContext).getUsername()); // 设置发送者名字

            // 不支持没有类型的消息
            if (message.getType() == null) {
                return JSON.toJSONString(Message.createUnsupportedMessage());
            }

            // 提前处理心跳消息, 提高效率
            if (Message.Type.HEARTBEAT.equals(message.getType())) {
                return null; // 不需要处理, 心跳消息只是为了告知服务器客户端连接仍然是活跃的
            }

            if (log.isDebugEnabled()) {
                log.debug("[消息] 收到消息:\n{}", JSON.toJSONString(message, true));
            }

            // [2] 根据消息的类型分别进行处理
            switch (message.getType()) {
                case GROUP_JOIN:
                    // [2.1] 加入小组消息: to 为 groupName
                    joinGroup(message, channelContext);
                    break;
                case GROUP_LEAVE:
                    // [2.2] 离开小组消息: to 为 groupName
                    leaveGroup(userId, to, channelContext);
                    break;
                case GROUP_MESSAGE:
                    // [2.3] 发送小组消息: to 为 groupName
                    sendToGroup(message, to, channelContext);
                    break;
                case GROUP_USERS:
                    // [2.4] 获取小组成员: to 为 groupName
                    List<User> users = groupUsers(to, channelContext);
                    message.setContent(JSON.toJSONString(users));
                    return JSON.toJSONString(message);
                case PRIVATE_MESSAGE:
                    // [2.5] 发送私聊消息: to 为接收者的 ID
                    sendToUser(message, to, channelContext);
                    return null;
                case GROUP_HISTORY:
                    // [2.6] 获取小组历史信息: to 为 groupName, content 为页码
                    return JSON.toJSONString(messageDao.findGroupMessages(to, NumberUtils.toInt(message.getContent(), 0), 100));
                case PRIVATE_HISTORY:
                    // [2.7] 获取私有历史信息: from 和 to 为用户 ID, content 为页码
                    return JSON.toJSONString(messageDao.findUserMessages(userId, to, NumberUtils.toInt(message.getContent(), 0), 100));
                case CONNECTION_COUNT:
                    // [2.8] 获取服务器当前的连接数
                    return JSON.toJSONString(connectionCountMessage(channelContext));
                default:
                    // [2.9] 不支持的消息
                    return JSON.toJSONString(Message.createUnsupportedMessage());
            }

            return null; // 返回值是要发送给客户端的内容，一般都是返回 null
        } catch (Exception ex) {
            // 转换出错则消息格式不对
            log.warn(ExceptionUtils.getStackTrace(ex));
            return JSON.toJSONString(Message.createErrorMessage(ex.getMessage()));
        }
    }

    /**
     * 加入小组
     *
     * @param groupMessage 小组消息:
     *        from    为用户 ID
     *        to      为小组名字
     *        content 为用户名
     * @param channelContext ChannelContext 对象
     */
    public synchronized void joinGroup(Message groupMessage, ChannelContext channelContext) {
        // 1. 获取信息
        // 2. 加入小组
        // 3. 通知小组成员

        // [1] 获取信息
        String groupName = StringUtils.trim(groupMessage.getTo()); // 要加入的小组名字, 不能为空

        if (StringUtils.isBlank(groupName)) {
            return;
        }

        // [2] 加入小组
        User user = getUser(channelContext);
        user.joinGroup(groupName);
        Tio.bindGroup(channelContext, groupName);

        // [3] 通知小组成员
        sendToGroup(groupMessage, groupName, channelContext);

        log.info("[加入小组] 用户: {}({}), 小组: {}", user.getUsername(), user.getId(), groupName);
    }

    /**
     * 离开小组
     *
     * @param userId    用户 ID
     * @param groupName 小组名字
     * @param channelContext ChannelContext 对象
     */
    public synchronized void leaveGroup(String userId, String groupName, ChannelContext channelContext) {
        // 1. 给小组成员发送用户离开的消息
        // 2. 把 groupName 从用户的信息中删除
        // 3. 从 Tio 小组中解绑用户

        // [1] 给小组成员发送用户离开的消息
        Message groupMessage = Message.createLeaveGroupMessage(userId, groupName);
        sendToGroup(groupMessage, groupName, channelContext);

        // [2] 把 groupName 从用户的信息中删除
        User user = getUser(channelContext);
        user.leaveGroup(groupName);

        // [3] 从 Tio 小组中解绑用户
        Tio.unbindGroup(groupName, channelContext);

        log.info("[离开小组] 用户: {}({}), 小组: {}", user.getUsername(), user.getId(), groupName);
    }

    /**
     * 用户连接进来, [验证]绑定用户
     * 网址: ws://im.xtuer.com:3721?userId=1&username=bob
     *
     * @param request 请求 HttpRequest 对象
     * @param channelContext ChannelContext 对象
     * @return 连接成功返回 true, 否则返回 false
     */
    public boolean login(HttpRequest request, ChannelContext channelContext) {
        // 1. 获取用户信息
        // 2. 如果 userId 或者 username 为空则返回 false, 不允许建立连接
        // 3. 如果 userId 已经绑定过其他的 channelContext, 则踢掉前一个, 同一个 userId 不允许重复重复登录
        // 4. 绑定用户和 channelContext: 在 channelContext 中存储用户对象

        // [1] 获取用户信息
        String userId   = StringUtils.trim(request.getParam("userId"));
        String username = StringUtils.trim(request.getParam("username"));
        User   user     = new User(userId, username);

        // [2] 如果 userId 或者 username 为空则返回 false, 不允许建立连接
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(userId)) {
            log.warn("userId 或 username 不能为空 - {}", channelContext.getClientNode());
            return false;
        }

        // [3] 如果 userId 已经绑定过其他的 channelContext, 则踢掉前一个, 同一个 userId 不允许重复重复登录
        ChannelContext previousChannelContext = Tio.getChannelContextByBsId(channelContext.groupContext, userId);
        if (!channelContext.equals(previousChannelContext) && previousChannelContext != null) {
            user = getUser(previousChannelContext);

            // 给用户发送一条他被踢掉的消息: 阻塞消息
            sendToUser(Message.createKickOutMessage(), user.getId(), previousChannelContext, true);

            // 踢掉用户
            previousChannelContext.setAttribute(Constants.KEY_KICK_OUT, true); // 踢掉的标志
            Tio.unbindBsId(previousChannelContext);
            Tio.remove(previousChannelContext, "服务器断开客户端连接");
            log.info("踢掉 {}({}) 已经登录的连接 {}", user.getUsername(), userId, previousChannelContext.getClientNode());

            // 绑定此用户前一个连接的小组
            user.getGroups().forEach(groupName -> {
                Tio.bindGroup(channelContext, groupName);
            });
        }

        // [4] 绑定用户和 channelContext: 在 channelContext 中存储用户对象,
        //     以便使用 Tio.sendToBsId(groupContext, userId, response) 给指定 ID 的用户发送信息
        //     Tio.bindBsId() 内部会调用 channelContext.setBsId(userid), 其他地方可以使用 channelContext.getBsId() 获取用户 ID
        bindUser(user, channelContext);
        Tio.bindBsId(channelContext, userId); // BS ID 和 ChannelContext 是一对一的

        return true;
    }

    /**
     * 用户断开连接离开
     *
     * @param channelContext ChannelContext 对象
     */
    public void logout(ChannelContext channelContext) {
        // 1. 如果 isKickOut() 为 true, 则说明是被重复登录踢掉的, 不需要发送离开消息, 也不需要重复解绑
        // 2. 离开所有小组
        // 3. 与 channelContext 解绑

        // [1] 如果 isKickOut() 为 true, 则说明是被重复登录踢掉的, 不需要发送离开消息, 也不需要重复解绑
        if (isKickOut(channelContext)) {
            return;
        }

        // [2] 离开所有小组
        User user = getUser(channelContext);
        user.getGroups().forEach(groupName -> {
            this.leaveGroup(user.getId(), groupName, channelContext);
        });

        // [3] 与 channelContext 解绑
        Tio.unbindBsId(channelContext);
    }

    /**
     * 获取小组成员
     *
     * @param groupName 小组名字
     * @param channelContext ChannelContext 对象
     * @return 返回小组成员列表
     */
    public List<User> groupUsers(String groupName, ChannelContext channelContext) {
        // 1. 获取小组的所有 channelContext
        // 2. 得到每个 channelContext 的用户
        // 3. 返回小组的所有用户

        // 如果小组中没有成员, 返回 null
        SetWithLock<ChannelContext> temp = Tio.getChannelContextsByGroup(channelContext.groupContext, groupName);
        if (temp == null) {
            return Collections.emptyList();
        }

        Set<ChannelContext> channels = temp.getObj();
        return channels.stream().map(this::getUser).collect(Collectors.toList());
    }

    /**
     * 发送小组消息
     *
     * @param groupName 小组名字
     */
    public void sendToGroup(Message message, String groupName, ChannelContext channelContext) {
        // 1. 只有在小组中才能给发送此小组的消息
        // 2. 发送小组消息
        // 3. 只保存小组成员之间发送的消息, 加入, 离开消息不保存

        // [1] 只有在小组中才能给发送此小组的消息
        if (!Tio.isInGroup(groupName, channelContext)) { return; }

        // [2] 发送小组消息
        WsResponse response = WsResponse.fromText(JSON.toJSONString(message), ServerConfig.CHARSET);
        Tio.sendToGroup(channelContext.groupContext, groupName, response);

        // [3] 只保存小组成员之间发送的消息, 加入, 离开消息不保存
        if (message.getType().equals(Message.Type.GROUP_MESSAGE)) {
            messageDao.saveMessage(message);
        }
    }

    /**
     * 给指定用户发送消息，以异步的方式发送
     *
     * @param userId 用户 ID
     */
    public void sendToUser(Message message, String userId, ChannelContext channelContext) {
        sendToUser(message, userId, channelContext, false);
    }

    /**
     * 给指定用户发送消息，isBlock 为 true 时以阻塞的方式发送，为 false 以异步的方式发送
     *
     * @param userId  用户 ID
     * @param isBlock 是否阻塞
     */
    public void sendToUser(Message message, String userId, ChannelContext channelContext, boolean isBlock) {
        WsResponse response = WsResponse.fromText(JSON.toJSONString(message), ServerConfig.CHARSET);

        if (isBlock) {
            Tio.bSendToBsId(channelContext.groupContext, userId, response);
        } else {
            Tio.sendToBsId(channelContext.groupContext, userId, response);
        }

        // 保存私聊消息
        messageDao.saveMessage(message);
    }

    /**
     * 绑定用户和 channelContext, channelContext 存储用户对象
     *
     * @param user 用户
     * @param channelContext ChannelContext 对象
     */
    private void bindUser(User user, ChannelContext channelContext) {
        channelContext.setAttribute(Constants.KEY_USER, user);
    }

    /**
     * 获取 channelContext 绑定的用户
     *
     * @param channelContext ChannelContext 对象
     * @return 返回channelContext 绑定的用户
     */
    public User getUser(ChannelContext channelContext) {
        return ((User) channelContext.getAttribute(Constants.KEY_USER));
    }

    /**
     * 判断 channelContext 是否被踢掉线的
     *
     * @param channelContext ChannelContext 对象
     * @return 被踢掉线返回 true, 否则返回 false
     */
    public boolean isKickOut(ChannelContext channelContext) {
        return BooleanUtils.toBoolean((Boolean) channelContext.getAttribute(Constants.KEY_KICK_OUT));
    }

    /**
     * 创建总共有多少连接数量的消息
     *
     * @param channelContext ChannelContext 对象
     * @return 返回连接数的消息对象
     */
    public Message connectionCountMessage(ChannelContext channelContext) {
        int count = Tio.getAllChannelContexts(channelContext.groupContext).getObj().size();
        return Message.createConnectionCountMessage(count);
    }
}
