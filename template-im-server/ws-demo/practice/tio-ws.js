var tio = {};
tio.ws = {};

/**
 * 创建 Web Socket，实现了心跳、自动重连的功能。
 *
 * @param {*} ws_protocol  WebSocket 的协议：wss or ws
 * @param {*} ip           服务器的 IP 或者域名
 * @param {*} port         服务器的端口
 * @param {*} requestQuery 加在 ws url 后面的请求参数，例如：name=张三&id=12，auth_token=1EB7EA47-0B1D-43B3-B7A4-8AD8CFCA12E4
 * @param {*} messageHandler    消息处理器，需要实现 5 个函数: onopen, onmessage, onclose, onerror, ping
 * @param {*} heartbeatTimeout  心跳时间，单位：毫秒
 * @param {*} reconnectInterval 重连间隔时间，单位：毫秒
 * @param {*} binaryType        'blob' or 'arraybuffer'; // blob 表示字符串，arraybuffer 表示字节
 */
tio.ws = function(ws_protocol, ip, port, requestQuery, messageHandler, heartbeatTimeout, reconnectInterval, binaryType) {
    this.ip   = ip;
    this.port = port;
    this.url  = ws_protocol + '://' + ip + ':' + port;
    this.binaryType = binaryType || 'arraybuffer';

    if (requestQuery) {
        this.url += '?' + requestQuery;
    }

    this.handler = messageHandler;
    this.heartbeatTimeout = heartbeatTimeout;
    this.reconnectInterval = reconnectInterval;
    this.heartbeatSendInterval = heartbeatTimeout / 2;

    // 连接到服务器
    this.connect = function() {
        var ws = new WebSocket(this.url);
        this.ws = ws;

        ws.binaryType = this.binaryType; // 'blob' or 'arraybuffer'
        var self = this;

        // 连接成功的事件，调用 handler 的 onopen 函数
        ws.onopen = function(event) {
            self.handler.onopen.call(self.handler, event, ws);
            self.lastInteractionTime(new Date().getTime());

            // 定时发送心跳
            self.pingIntervalId = setInterval(function() {
                self.ping(self);
            }, self.heartbeatSendInterval);
        };

        // 消息到达事件，调用 handler 的 onmessage 函数
        ws.onmessage = function(event) {
            // 方法内部 this 对象不同, 使用 call 调用其他对象方法，方法中内部 this 指的是掉用的对象而不再是原对象
            self.handler.onmessage.call(self.handler, event, ws);
            self.lastInteractionTime(new Date().getTime());
        };

        // 连接关闭事件，调用 handler 的 onclose 函数，并且关闭的时候自动重连
        ws.onclose = function(event) {
            clearInterval(self.pingIntervalId); // clear send heartbeat task

            try {
                self.handler.onclose.call(self.handler, event, ws);
            } catch (error) { /**/ }

            self.reconnect(event);
        };

        ws.onerror = function(event) {
            self.handler.onerror.call(self.handler, event, ws);
        };

        return ws;
    };

    // 发送消息
    this.send = function(data) {
        this.ws.send(data);
    };

    // 重新连接到服务器
    this.reconnect = function(event) {
        var self = this;
        setTimeout(function() {
            var ws = self.connect();
            self.ws = ws;
        }, self.reconnectInterval);
    };

    // 发送心跳
    this.ping = function() {
        var iv = new Date().getTime() - this.lastInteractionTime(); // 已经多久没发消息了

        if ((this.heartbeatSendInterval + iv) >= this.heartbeatTimeout) {
            this.handler.ping(this.ws);
        }
    };

    // 获取或者设置最后和服务器交互时间
    this.lastInteractionTime = function() {
        if (arguments.length == 1) {
            this.lastInteractionTimeValue = arguments[0];
        }

        return this.lastInteractionTimeValue;
    };
};
