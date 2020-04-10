/**
 * 消息
 *
 * @param from 发送者，一般为 userId
 * @param to   接收者
 *             群发消息时为小组名 groupName
 *             私人消息时为接收者的 userId
 * @param type 消息类型
 * @param content 消息的内容
 */
function Message(from, to, type, content) {
    this.from = from;
    this.to   = to;
    this.type = type;
    this.content = content;
}

// 消息转为 JSON 字符串
Message.prototype.toJson = function() {
    var temp = {
        from   : this.from,
        to     : this.to,
        type   : this.type,
        content: this.content
    };

    return JSON.stringify(temp);
};

// 消息类型
Message.types = {
    GROUP_JOIN     : 'GROUP_JOIN',      // 加入小组
    GROUP_LEAVE    : 'GROUP_LEAVE',     // 离开小组
    GROUP_MESSAGE  : 'GROUP_MESSAGE',   // 发送小组消息
    GROUP_USERS    : 'GROUP_USERS',     // 获取小组成员
    GROUP_HISTORY  : 'GROUP_HISTORY',   // 小组历史消息
    PRIVATE_MESSAGE: 'PRIVATE_MESSAGE', // 发送私有消息
    PRIVATE_HISTORY: 'PRIVATE_HISTORY', // 私有历史消息
    HEARTBEAT      : 'HEARTBEAT',       // 心跳消息
    ERROR          : 'ERROR',           // 错误消息
};

// 创建加入小组消息: 消息的 content 为用户名字
Message.createJoinToGroupMessage = function(userId, groupName) {
    return new Message(userId, groupName, Message.types.GROUP_JOIN, '');
};

// 创建小组消息
Message.createGroupMessage = function(userId, groupName, content) {
    return new Message(userId, groupName, Message.types.GROUP_MESSAGE, content);
};

// 创建获取小组成员列表消息
Message.createGetGroupUsersMessage = function(userId, groupName) {
    return new Message(userId, groupName, Message.types.GROUP_USERS, '');
};

// 创建获取小组历史消息的消息
Message.createGroupHistoryMessage = function(userId, groupName, page = 1) {
    return new Message(userId, groupName, Message.types.GROUP_HISTORY, page); // content 为 page
};

// 创建私人消息
Message.createPrivateMessage = function(userId, receiverId, content) {
    return new Message(userId, receiverId, Message.types.PRIVATE_MESSAGE, content);
};

// 创建关闭消息，WebSocket 中关闭的消息为空的内容
Message.createCloseMessage = function() {
    return '';
};

// 创建心跳消息
Message.createHeartbeatMessage = function() {
    return new Message('', '', Message.types.HEARTBEAT, '');
};
