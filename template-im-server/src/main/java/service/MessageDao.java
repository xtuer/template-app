package service;

import bean.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import util.PageUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 消息持久化 Dao
 */
@Service
@Slf4j
public class MessageDao {
    private static final String MESSAGE_IM  = "message_im"; // 消息在 MongoDB 中的集合名

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 保存消息
     *
     * @param message 消息
     */
    public void saveMessage(Message message) {
        mongoTemplate.insert(message, MESSAGE_IM); // 保存消息发送记录
    }

    /**
     * 查询小组消息: 按时间倒序
     *
     * @param groupName 小组名
     * @param page 页码
     * @param size 数量
     * @return 返回消息列表
     */
    public List<Message> findGroupMessages(String groupName, int page, int size) {
        // 等级 SQL: SELECT * FROM message_im WHERE to=#{groupName} AND type='GROUP_MESSAGE'
        //           ORDER BY createdTime DESC LIMIT #{offset}, #{size}

        PageRequest pageable = PageUtils.pageRequestByCreatedTimeDesc(page, size);
        Criteria criteria = Criteria.where("to").is(groupName).and("type").is(Message.Type.GROUP_MESSAGE);
        List<Message> messages = mongoTemplate.find(Query.query(criteria).with(pageable), Message.class, MESSAGE_IM);

        return messages;
    }

    /**
     * 查询用户 senderId 和 receiverId 之间发送过的消息: 按时间倒序
     *
     * @param senderId   发送者 ID
     * @param receiverId 接受者 ID
     * @param page 页码
     * @param size 数量
     * @return 返回消息列表
     */
    public List<Message> findUserMessages(String senderId, String receiverId, int page, int size) {
        // 等级 SQL: SELECT * FROM message_im WHERE type='PRIVATE_MESSAGE' AND
        //           ((`from`=#{senderId} AND to=#{receiverId}) OR (`from`=#{receiverId} AND to=#{senderId}))
        //           ORDER BY createdTime DESC LIMIT #{offset}, #{size}

        PageRequest pageable = PageUtils.pageRequestByCreatedTimeDesc(page, size);
        Criteria orCriteria = new Criteria().orOperator(
                Criteria.where("from").is(senderId).and("to").is(receiverId),
                Criteria.where("from").is(receiverId).and("to").is(senderId)
        );
        Criteria criteria = Criteria.where("type").is(Message.Type.PRIVATE_MESSAGE).andOperator(orCriteria);
        List<Message> messages = mongoTemplate.find(Query.query(criteria).with(pageable), Message.class, MESSAGE_IM);

        return messages;
    }
}
