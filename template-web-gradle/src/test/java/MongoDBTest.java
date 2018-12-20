import edu.bean.Message;
import edu.bean.lrs.LrsAction;
import edu.bean.lrs.LrsRecord;
import edu.service.LrsService;
import edu.service.MessageService;
import edu.util.Utils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 测试 MongoDB
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:application-test.xml"})
public class MongoDBTest {
    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    @Resource(name = "messageService")
    private MessageService messageService;

    @Autowired
    private LrsService lrsService;

    private long userId = 12; // 用户 ID

    /**
     * 准备消息
     */
    @Test
    public void setup() {
        // 所有消息: db.message_group.find().pretty()
        //          db.message_user.find().pretty()
        // 清空消息: db.message_group.deleteMany({})
        //          db.message_user.deleteMany({})

        // 1. 清空所有消息
        // 2. 发布几条消息
        mongoTemplate.remove(new Query(), MessageService.MESSAGE_GROUP);
        mongoTemplate.remove(new Query(), MessageService.MESSAGE_USER);

        // 发布 7 条群发消息
        messageService.sendMessage(Message.createGroupMessage(1, 1, 8, 1, "clazz-1",  Message.ContentType.QA_ANSWER, Message.Type.CLAZZ,  day(-5)));
        messageService.sendMessage(Message.createGroupMessage(2, 1, 8, 2, "clazz-2",  Message.ContentType.QA_ANSWER, Message.Type.CLAZZ,  day(-2)));
        messageService.sendMessage(Message.createGroupMessage(3, 1, 8, 1, "team-01",  Message.ContentType.QA_ANSWER, Message.Type.TEAM,   day(-4)));
        messageService.sendMessage(Message.createGroupMessage(4, 1, 8, 1, "team-02",  Message.ContentType.QA_ANSWER, Message.Type.TEAM,   day(-2)));
        messageService.sendMessage(Message.createGroupMessage(5, 1, 8, 2, "team-23",  Message.ContentType.QA_ANSWER, Message.Type.TEAM,   day(-1)));
        messageService.sendMessage(Message.createGroupMessage(6, 1, 8, 1, "school-1", Message.ContentType.QA_ANSWER, Message.Type.SCHOOL, day(-0)));
        messageService.sendMessage(Message.createGroupMessage(7, 2, 8, 2, "school-2", Message.ContentType.QA_ANSWER, Message.Type.SCHOOL, day(-0)));

        // 发布 9 条用户消息
        messageService.sendMessage(Message.createUserMessage(11, 1, 9, userId, "Alice",  Message.ContentType.QA_ANSWER, day(0)));
        messageService.sendMessage(Message.createUserMessage(12, 1, 9, userId, "Aloha",  Message.ContentType.QA_ANSWER, day(1)));
        messageService.sendMessage(Message.createUserMessage(13, 1, 9, userId, "David",  Message.ContentType.QA_ANSWER, day(2)));
        messageService.sendMessage(Message.createUserMessage(14, 1, 9, userId, "Scott",  Message.ContentType.QA_ANSWER, day(3)));
        messageService.sendMessage(Message.createUserMessage(15, 1, 9, userId, "Harris", Message.ContentType.QA_ANSWER, day(4)));
        messageService.sendMessage(Message.createUserMessage(16, 1, 9, userId, "Fergus", Message.ContentType.QA_ANSWER, day(5)));
        messageService.sendMessage(Message.createUserMessage(17, 1, 9, userId, "Garcia", Message.ContentType.QA_ANSWER, day(6)));
        messageService.sendMessage(Message.createUserMessage(18, 1, 9, userId, "Warren", Message.ContentType.QA_ANSWER, day(7)));
        messageService.sendMessage(Message.createUserMessage(19, 1, 9, userId, "Lester", Message.ContentType.QA_ANSWER, day(8)));
    }

    /**
     * 获取群发消息
     */
    @Test
    public void testFindGroupMessages() {
        long schoolId = 1L;
        List<Long> clazzIds = Arrays.asList(1L);
        List<Long> teamIds  = Arrays.asList(1L, 2L);

        // Utils.dump(messageService.findStudentReceivedMessages(1L, schoolId, 1, 10));
    }

    /**
     * 更新消息为已读，读取用户消息
     */
    @Test
    public void testUpdateAndFindMessages() {
        messageService.markMessageRead(userId, 12);
        messageService.markMessageRead(userId, 19);

        Utils.dump(messageService.findReadMessages(userId,   1, 10));
        Utils.dump(messageService.findUnreadMessages(userId, 1, 3));
        Utils.dump(messageService.findUnreadMessages(userId, 2, 3));
        Utils.dump(messageService.findUnreadMessages(userId, 3, 3));
    }

    /**
     * 查询是否还有未读消息
     */
    @Test
    public void testHasUnreadMessages() {
        // 全部标记为已读: db.message_private.updateMany({}, {$set: {read: true}})
        Utils.dump(messageService.countUnreadMessages(userId));
    }

    /**
     * 获取群发消息到用户的私有消息空间
     */
    @Test
    public void testFetchGroupMessages() {
        long schoolId = 1L;
        List<Long> clazzIds = Arrays.asList(1L);
        List<Long> teamIds  = Collections.emptyList();
        // Utils.dump(messageService.fetchGroupMessages(userId, schoolId, clazzIds, teamIds, day(-13)));
    }

    @Test
    public void updateFetchGroupMessageTime() {
        Utils.dump(messageService.findLastFetchGroupMessagesTime(userId));
        messageService.updateLastFetchGroupMessagesTime(userId, new Date());
    }

    /**
     * 测试创建 LRS 记录
     */
    @Test
    public void insertLrsRecord() {
        LrsRecord record = new LrsRecord();
        record.setActionCode(LrsAction.OpenPlan.getCode());
        record.setActionDetails(LrsAction.OpenPlan.getDetails());
        record.setLatitude(31.9992);
        record.setLongitude(234.123);
        record.setLocation("学校");

        lrsService.saveLrsRecord(record);
    }

    /**
     * 计算离今天 n 天的时间，
     */
    private Date day(int n) {
        return DateUtils.addDays(new Date(), n);
    }
}
