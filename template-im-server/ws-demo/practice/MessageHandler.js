var MessageHandler = function(showMessage) {
    this.showMessage = showMessage;

    /**
     * Web Socket 已连接上，使用 send() 方法发送数据: 如发送自己的所有信息群里其他人
     *
     * @param  {Object} event
     * @param  {WebSocket} ws
     */
    this.onopen = function(event, ws) {
        this.showMessage('连接成功: ' + new Date());
    };

    /**
     * 收到服务器发来的消息
     *
     * @param {Object} event 事件对象
     * @param {WebSocket} ws
     */
    this.onmessage = function(event, ws) {
        var message = JSON.parse(event.data);
        delete message.createdTime;

        if (message.type == Message.types.GROUP_JOIN) {
            // 加入小组成功后获取小组成员
            if (message.from == userId) {
                this.showMessage(`加入小组 ${message.to}`);
                // ws.send(Message.createGetUsersMessage(userId, groupName).toJson());
            } else {
                this.showMessage(JSON.stringify(message));
            }
        } else if (message.type == Message.types.GROUP_USERS) {
            // 获取到小组成员
            var users = JSON.parse(message.content);
            this.showMessage(JSON.stringify(users));
        } else if (message.type == Message.types.HEARTBEAT) {
            // TODO
        } else {
            this.showMessage(JSON.stringify(message));
        }
    };

    /**
     * @param  {Object} event
     * @param  {WebSocket} ws
     */
    this.onclose = function(event, ws) {
        this.showMessage('连接关闭: ' + new Date());
    };

    /**
     *
     * @param  {[type]} event
     * @param  {WebSocket} ws
     */
    this.onerror = function(event, ws) {
        // error(e, ws)
    };

    /**
     * 发送心跳，tio-ws 框架会自动定时调用该方法，请在该方法中发送心跳
     *
     * @param {WebSocket} ws
     */
    this.ping = function(ws) {
        ws.send(Message.createHeartbeatMessage().toJson());
    };
};
