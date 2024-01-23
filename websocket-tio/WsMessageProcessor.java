package newdt.dsc.ws.tio;

import lombok.extern.slf4j.Slf4j;
import newdt.dsc.bean.exec.Task;
import newdt.dsc.bean.exec.TaskSource;
import newdt.dsc.service.exec.TaskExecuteService;
import newdt.dsc.util.TaskUtils;
import newdt.dsc.util.Utils;
import newdt.dsc.ws.msg.Message;
import newdt.dsc.ws.msg.MessageType;
import newdt.dsc.ws.msg.MessageUtils;
import newdt.dsc.ws.msg.exec.TaskCancelMessage;
import newdt.dsc.ws.msg.exec.TaskExecuteNthSqlMessage;
import newdt.dsc.ws.msg.exec.TaskSubmitMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消息处理器
 */
@Component
@Slf4j
public class WsMessageProcessor {
    @Autowired
    private WsMessageService msgService;

    @Autowired
    private TaskExecuteService execService;

    /**
     * 保存数据到数据库的线程池: 保存设备大量上报的实时数据时特别需要
     */
    private final ExecutorService dbThreadPool = Executors.newFixedThreadPool(3);

    /**
     * 处理消息.
     *
     * @param json 消息的原始字符串 (JSON 格式)。
     * @param channelContext 通道 context，为 null 表示通过业务代码调用，非 null 为 WS 直接调用.
     * @return 返回消息处理结果.
     * @throws RuntimeException channelContext 为 null 时继续抛出底层的异常，非 null 发生异常时返回 null.
     */
    public Object processMessage(String json, ChannelContext channelContext) {
        // 逻辑:
        // 1. 转换消息为 Message 对象，
        // 2. 如果转换出错则消息格式不对，return 错误信息告知发送者
        // 3. 处理心跳消息
        // 4. 根据消息的类型分别进行处理:
        //    获取设备网关
        //    获取服务器当前的连接数
        //    不支持的消息
        //    原样返回的 Echo 消息

        // [1] 转换消息为 Message 对象
        Message message = Utils.fromJson(json, Message.class);

        // [2] 如果转换出错则消息格式不对，return 错误信息告知发送者
        if (message == null || message.getType() == null) {
            if (log.isDebugEnabled()) {
                log.debug("[错误] 消息不支持: {}", json);
            }

            if (channelContext == null) {
                throw new RuntimeException("消息不支持");
            } else {
                return MessageUtils.createUnsupportedMessage().toJson();
            }
        }

        // [3] 提前处理心跳消息, 提高效率
        if (MessageType.HEARTBEAT.equals(message.getType())) {
            return null; // 不需要处理, 心跳消息只是为了告知服务器客户端连接仍然是活跃的
        }

        // 打印收到的消息
        if (log.isDebugEnabled()) {
            log.debug("[消息] 收到消息:\n{}", json);
        }

        // [4] 根据消息的类型分别进行处理
        try {
            switch (message.getType()) {
                case ECHO:
                    return json;
                case CONNECTION_COUNT:
                    return MessageUtils.createConnectionCountMessage().toJson();
                case TASK_SUBMIT:
                    // 提交 SQL 任务。
                    submitTask(json);
                    return null;
                case TASK_CANCEL:
                    cancelTask(json);
                    return null;
                case TASK_EXECUTE_NTH_SQL:
                    executeTaskNthSql(json);
                    return null;
                default:
                    return null;
            }
        } catch (Throwable ex) {
            if (channelContext == null) {
                // 业务调用 processMessage 发生的异常，再次抛出给调用者
                throw ex;
            } else {
                log.warn(Utils.getStackTrace(ex));

                // Websocket 客户端返回错误消息 (直接跑出异常客户端接收不到任何错误)
                return MessageUtils.createErrorMessage(Utils.truncateExceptionMessage(ex), ex).toJson();
            }
        }
    }

    /**
     * 提交 SQL 任务。
     */
    private void submitTask(String taskJson) {
        TaskSubmitMessage msg = Utils.fromJson(taskJson, TaskSubmitMessage.class);

        if (msg == null) {
            return;
        }

        // 把 WS 的任务消息转换为 SQL 任务对象。
        Task task = TaskUtils.convertTask(msg);
        task.setTaskSource(TaskSource.WEB_SOCKET);

        // 提交任务。
        execService.submitTask(task);
    }

    /**
     * SQL 任务取消。
     */
    private void cancelTask(String taskJson) {
        TaskCancelMessage msg = Utils.fromJson(taskJson, TaskCancelMessage.class);

        if (msg == null) {
            return;
        }

        execService.cancelTask(msg.getTaskId());
    }

    /**
     * 执行任务的第 Nth 条 SQL 语句。
     */
    private void executeTaskNthSql(String taskJson) {
        TaskExecuteNthSqlMessage msg = Utils.fromJson(taskJson, TaskExecuteNthSqlMessage.class);

        if (msg == null) {
            return;
        }

        execService.executeTaskNthSql(msg.getTaskId(), msg.getSqlSn(), msg.getPageNumber(), msg.getConditionOfTable());
    }
}
