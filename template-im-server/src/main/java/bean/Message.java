package bean;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 每种消息都有自己的类型 type，如果此类型不够用，那么需要根据业务场景，使用 JSON 格式的消息内容 content，在 content 中再定义需要的类型。
 */
@Getter
@Setter
@Accessors(chain = true)
public class Message {
    private String from;         // 消息发送者, 一般为消息发送者 ID
    private String fromUsername; // 消息发送者的名字
    private String to;           // 消息接收者, 例如私聊信息的目标用户 ID, 小组消息时为小组名字
    private String content;      // 消息内容
    private Type   type;         // 消息类型
    private Date   createdAt = new Date(); // 消息创建时间

    // 消息类型
    public enum Type {
        GROUP_JOIN,       // 加入小组
        GROUP_LEAVE,      // 离开小组
        GROUP_MESSAGE,    // 发送小组消息
        GROUP_USERS,      // 获取小组成员
        GROUP_HISTORY,    // 小组历史消息
        PRIVATE_MESSAGE,  // 发送私有消息
        PRIVATE_HISTORY,  // 私有历史消息
        HEARTBEAT,        // 心跳消息
        ERROR,            // 错误消息
        CONNECTION_COUNT, // 查询连接数
        KICK_OUT,         // 被踢掉的消息
    }

    /**
     * 创建不支持的消息
     *
     * @return 返回消息对象
     */
    public static Message createUnsupportedMessage() {
        return createErrorMessage("不支持的消息格式");
    }

    /**
     * 创建错误消息
     *
     * @param  content 消息内容
     * @return 返回消息对象
     */
    public static Message createErrorMessage(String content) {
        Message msg = new Message();
        msg.setContent(content);
        msg.setType(Type.ERROR);

        return msg;
    }

    /**
     * 创建离开小组的消息
     *
     * @param userId    用户 ID
     * @param groupName 小组名字
     * @return 返回消息对象
     */
    public static Message createLeaveGroupMessage(String userId, String groupName) {
        Message message = new Message();
        message.setFrom(userId);
        message.setTo(groupName);
        message.setType(Message.Type.GROUP_LEAVE);

        return message;
    }

    /**
     * 创建连接数的消息, content 为连接数量
     *
     * @param count 连接数量
     * @return 返回消息对象
     */
    public static Message createConnectionCountMessage(int count) {
        Message message = new Message();
        message.setType(Message.Type.CONNECTION_COUNT);
        message.setContent(count + "");

        return message;
    }

    /**
     * 踢掉用户的消息
     *
     * @return 返回消息对象
     */
    public static Message createKickOutMessage() {
        Message message = new Message();
        message.setType(Type.KICK_OUT);

        return message;
    }

    /**
     * 创建历史消息
     *
     * @param messages 消息的数组
     * @return 返回消息对象
     */
    public static Message createHistoryMessage(List<Message> messages) {
        Message message = new Message();
        message.setType(Type.GROUP_HISTORY);
        message.setContent(JSON.toJSONString(messages));

        return message;
    }
}
