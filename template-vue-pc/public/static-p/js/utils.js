/*-----------------------------------------------------------------------------|
 |                                 字符串格式化                                  |
 |----------------------------------------------------------------------------*/
/**
 * 替换字符串中 {placeholder} 或者 {0}, {1} 等模式部分为参数中传入的字符串
 * 使用方法:
 *     formatString('I can speak {language} since I was {age}', {language: 'Javascript', age: 10})
 *     formatString('I can speak {0} since I was {1}', 'Javascript', 10)
 * 输出都为:
 *     I can speak Javascript since I was 10
 *
 * @param  {String} str 带有 placeholder 的字符串
 * @param  {JSON}   replacements 用来替换 placeholder 的 JSON 对象 (或者数组)
 * @return {String} 返回格式化后的字符串
 */
var formatString = function(str, replacements) {
    replacements = (typeof replacements === 'object') ? replacements : Array.prototype.slice.call(arguments, 1);
    return str.replace(/\{\{|\}\}|\{(\w+)\}/g, function(m, n) {
        if (m === '{{') { return '{'; }
        if (m === '}}') { return '}'; }
        return replacements[n];
    });
};

/**
 * 扩展了 String 类型，给其添加格式化的功能，替换字符串中 {placeholder} 或者 {0}, {1} 等模式部分为参数中传入的字符串
 * 使用方法:
 *     'I can speak {language} since I was {age}'.format({language: 'Javascript', age: 10})
 *     'I can speak {0} since I was {1}'.format('Javascript', 10)
 * 输出都为:
 *     I can speak Javascript since I was 10
 *
 * @param  {JSON} replacements 用来替换 placeholder 的 JSON 对象 (或者数组)
 * @return {String} 返回格式化后的字符串
 * @
 */
String.prototype.format = function(replacements) {
    replacements = (typeof replacements === 'object') ? replacements : Array.prototype.slice.call(arguments, 0);
    return formatString(this, replacements);
};

/**
 * 字符串转换为日期
 * 例子:
 *     '2016-03-29 18:30:00'.toDate('yyyy-MM-dd hh:mm:ss');
 *     '22/03/2016 14:03:01'.toDate('dd/MM/yyyy hh:mm:ss');
 *
 * @param  {String} format 日期格式，例如 yyyy-MM-dd hh:mm:ss
 * @return {Date} 返回字符串对应的日期
 */
String.prototype.toDate = function(format) {
    // var normalized       = this.replace(/[^a-zA-Z0-9]/g, '-');
    // var normalizedFormat = format.replace(/[^a-zA-Z0-9]/g, '-');
    // var formatItems      = normalizedFormat.split('-');
    // var dateItems        = normalized.split('-');

    // var monthIndex   = formatItems.indexOf('MM');
    // var dayIndex     = formatItems.indexOf('dd');
    // var yearIndex    = formatItems.indexOf('yyyy');
    // var hourIndex    = formatItems.indexOf('hh');
    // var minutesIndex = formatItems.indexOf('mm');
    // var secondsIndex = formatItems.indexOf('ss');

    // var today = new Date();

    // var year  = yearIndex>-1  ? dateItems[yearIndex]    : today.getFullYear();
    // var month = monthIndex>-1 ? dateItems[monthIndex]-1 : today.getMonth()-1;
    // var day   = dayIndex>-1   ? dateItems[dayIndex]     : today.getDate();

    // var hour    = hourIndex>-1    ? dateItems[hourIndex]    : today.getHours();
    // var minute  = minutesIndex>-1 ? dateItems[minutesIndex] : today.getMinutes();
    // var second  = secondsIndex>-1 ? dateItems[secondsIndex] : today.getSeconds();

    // return new Date(year, month, day, hour, minute, second);
    return dayjs(this).toDate();
};

/**
 * 解析 JSON 字符串为数组
 *
 * @return {Array} 返回字符串对应的数组，如果字符串不是数组格式，返回空数组
 */
String.prototype.toArray = function() {
    try {
        var array = JSON.parse(this);
        return Array.isArray(array) ? array : [];
    } catch (error) {
        console.log(`字符串解析为数组出错: ${this}`);
    }

    return [];
};

/*-----------------------------------------------------------------------------|
 |                                    Array                                    |
 |----------------------------------------------------------------------------*/
/**
 * 从数组中删除下标为 index 的元素
 *
 * @param  {Integer} index 要删除的元素的下标
 * @return 无返回值
 */
Array.prototype.remove = function(index) {
    this.splice(index, 1);
};

/**
 * 在下标为 index 处向数组中插入一个元素，例如 arr.remove(2)
 *
 * @param  {Integer}     index 要插入元素的位置
 * @param  {ElementType} elem  要插入的元素
 * @return 无返回值
 */
Array.prototype.insert = function(index, elem) {
    this.splice(index, 0, elem);
};

/**
 * 替换数组中下标为 index 处的元素
 *
 * @param  {Integer}     index 要替换的元素的位置
 * @param  {ElementType} elem  用于替换的元素
 * @return 无返回值
 */
Array.prototype.replace = function(index, elem) {
    if (index>=0 && index<this.length) {
        this.splice(index, 1, elem);
    }
};

/**
 * 将日期格式化成指定格式的字符串
 *
 * @param  {String} fmt 目标字符串格式，支持的字符有：y,M,d,q,w,H,h,m,S，默认：yyyy-MM-dd HH:mm:ss
 * @return {String} 返回格式化后的日期字符串
 */
Date.prototype.format = function(fmt) {
    fmt = fmt || 'yyyy-MM-dd HH:mm:ss';
    var obj = {
        y: this.getFullYear(),    // 年份，注意必须用getFullYear
        M: this.getMonth() + 1,   // 月份，注意是从0-11
        d: this.getDate(),        // 日期
        q: Math.floor((this.getMonth() + 3) / 3), // 季度
        w: this.getDay(),         // 星期，注意是0-6
        H: this.getHours(),       // 24小时制
        h: this.getHours() % 12 === 0 ? 12 : this.getHours() % 12, // 12小时制
        m: this.getMinutes(),     // 分钟
        s: this.getSeconds(),     // 秒
        S: this.getMilliseconds() // 毫秒
    };
    var week = ['天', '一', '二', '三', '四', '五', '六'];
    for (var i in obj) {
        fmt = fmt.replace(new RegExp(i + '+', 'g'), function (m) {
            var val = obj[i] + '';
            if (i === 'w') return (m.length > 2 ? '星期' : '周') + week[val];
            for (var j = 0, len = val.length; j < m.length - len; j += 1) val = '0' + val;
            return m.length === 1 ? val : val.substring(val.length - m.length);
        });
    }
    return fmt;
};

/*-----------------------------------------------------------------------------|
 |                                   Utils                                     |
 |----------------------------------------------------------------------------*/
function Utils() {}

/**
 * 文件类型和对应的 CSS 样式名
 */
Utils.types = {
    png : 'file-type-png',
    jpg : 'file-type-jpg',
    jpeg: 'file-type-jpg',
    gif : 'file-type-gif',
    pdf : 'file-type-pdf',
    doc : 'file-type-word',
    docx: 'file-type-word',
    ppt : 'file-type-ppt',
    pptx: 'file-type-ppt',
    xls : 'file-type-excel',
    xlsx: 'file-type-excel',
    zip : 'file-type-zip',
    rar : 'file-type-zip',
    '7z': 'file-type-zip',
    swf : 'file-type-swf',
    mp4 : 'file-type-mp4',
    mp3 : 'file-type-mp3',
};

/**
 * 获取 URL 中的文件名，
 * 例如 https://www.qtdebug.com/vue-array.html 输出 vue-array.html
 *
 * @param  {String} URL 链接
 * @return {String} 返回 URL 的文件名
 */
Utils.getFilename = function(url) {
    var parser  = document.createElement('a');
    parser.href = url;
    const token = parser.pathname.split('/'); // 去掉参数等和文件名无关的部分
    const filename = token[token.length - 1];

    return filename;
};

/**
 * 获取文件名的后缀
 *
 * @param  {String} url 文件名或者 URI、URL
 * @return {String} 返回文件名的后缀
 */
Utils.getFilenameExtension = function(url) {
    const filename = Utils.getFilename(url);
    const dotPos   = filename.lastIndexOf('.');
    const ext      = filename.substring(dotPos + 1);

    return ext;
};

/**
 * 根据文件名返回对应的 CSS class 名
 *
 * @param  {String} filename 文件名或者 URI、URL
 * @return {String} 返回文件名对应的 CSS class 名
 */
Utils.getFileClass = function(filename) {
    const type = Utils.types[Utils.getFilenameExtension(filename)];

    return type || 'file-type-other';
};

/**
 * 判断文件是否图片
 *
 * @param  {String} filename 文件名或者 URI、URL
 * @return {Boolean} 文件是图片时返回 true，否则返回 false
 */
Utils.isImage = function(filename) {
    const ext = Utils.getFilenameExtension(filename);

    return ('png' === ext || 'jpg' === ext || 'jpeg' === ext || 'gif' === ext);
};

/**
 * 判断文件是否 MP3
 *
 * @param  {String} filename 文件名或者 URI、URL
 * @return {Boolean} 文件是 MP3 时返回 true，否则返回 false
 */
Utils.isMp3 = function(filename) {
    const ext = Utils.getFilenameExtension(filename);

    return ext === 'mp3';
};

/**
 * 判断文件是否 MP4
 *
 * @param  {String} filename 文件名或者 URI、URL
 * @return {Boolean} 文件是 MP4 时返回 true，否则返回 false
 */
Utils.isMp4 = function(filename) {
    const ext = Utils.getFilenameExtension(filename);

    return ext === 'mp4';
};

/**
 * 从 100 开始生成递增的整数
 *
 * @return {Integer} 返回整数
 */
Utils.nextSn = function() {
    if (window.nextSequentialInt) {
        window.nextSequentialInt += 1;
    } else {
        window.nextSequentialInt = 100;
    }

    return window.nextSequentialInt;
};

/**
 * 从 100 开始生成新的 ID，类型为字符串，因为服务器端使用 long 为 ID，而 JS 不支持 64 位 long，
 * 所以服务器返回的 ID 在 JS 中使用字符串存储
 *
 * @return {String} 返回页面打开后按顺序生成的 ID
 */
Utils.nextId = function() {
    return Utils.nextSn() + '';
};

/**
 * 判断 id 是否有效，不为 0 或者 '0' 则有效
 *
 * @param  {String} 进行校验的 ID
 * @return {Boolean} ID 有效返回 true，否则返回 false
 */
Utils.isValidId = function(id) {
    return id && (id + '' !== '0');
};

Utils.BASE_CN_NUMBERS = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十'];

/**
 * 阿拉伯数字转为中文数字，支持 [0, 99]
 *
 * @param  {Integer} n 阿拉伯数字
 * @return {String} 返回 [0, 99] 之间的中文数字字符串，超出范围的返回 ''
 */
Utils.toCnNumber = function(n) {
    n = parseInt(n);

    if (0 <= n && n <= 10) {
        return Utils.BASE_CN_NUMBERS[n];
    } else if (11 <= n && n <= 99) {
        const ge  = parseInt(n%10);
        const shi = parseInt(n/10);

        if (1 === shi) {
            // 11-19: 十一、十二、...
            return '十' + (ge!==0?Utils.BASE_CN_NUMBERS[ge]:'');
        } else {
            // 20-99: 二十一、二十二、...
            return Utils.BASE_CN_NUMBERS[shi] + '十' + (ge===0?'':Utils.BASE_CN_NUMBERS[ge]);
        }
    } else {
        return '';
    }
};

/**
 * 生成一序列间隔为 step 整数，asc 为 true 时生成 [start, end]，asc 为 false 时生成 [end, start]
 *
 * @param  {Integer} start 开始的整数
 * @param  {Integer} end   结束的整数
 * @param  {Boolean} asc   为 true 则使用升序，为 false 使用降序
 * @return {Array} 返回整数的数组
 */
Utils.numbersInRange = function(start, end, step = 1, asc = true) {
    const ns = [];

    for (let i = start; i <= end; i += step) {
        ns.push(i);
    }

    return asc ? ns : ns.reverse();
};

Utils.CAN_NOT_PREVIEW  = 0; // 不可预览
Utils.CAN_PREVIEW      = 1; // 可以预览
Utils.CONVERTING       = 2; // 转换中
Utils.CONVERTING_ERROR = 3; // 转换错误

/**
 * 请求预览信息, 参数为 JSON 对象, 只有 uri 为必要参数, 其他的都为可选参数, 格式为
 * {
 *     uri     : '/file/data/2018-10-21/238639489453391872.docx', // 要预览文件的 URI
 *     ready   : function(previewUrl) {},    // 可以预览的回调函数, 参数为预览的 URL
 *     progress: function(progressValue) {}, // 文件正在转换中的回调函数, 参数 progressValue 为转换的进度, 值为 [0, 100]
 *     complete: function() {},              // 请求结束的回调函数, 例如结束加载动画
 *     timeout : 120,                        // 请求的超时时间, 单位为秒, 默认超时时间为 60 秒
 * }
 *
 * 算法: 定时轮询请求文件是否可预览了, 当文件可以预览或者请求超时则结束轮询请求.
 *
 * @param  {String}   uri      [必要] 要预览文件的 URI
 * @param  {Function} ready    [可选] 可以预览的回调函数, 参数为预览的 URL
 * @param  {Function} progress [可选] 文件正在转换中的回调函数, 参数 progressValue 为转换的进度, 值为 [0, 100]
 * @param  {Function} complete [可选] 请求结束的回调函数, 例如结束加载动画
 * @param  {Integer}  timeout  [可选] 请求的超时时间, 单位为秒
 * @return 无返回值
 */
Utils.canPreview = function({ uri, ready, progress, complete, timeout }) {
    const url     = Urls.API_CAN_PREVIEW_FILE_PREFIX + uri;
    let stopped   = false;
    let timerId   = 0;
    let expiredAt = new Date().getTime() + (timeout ? timeout*1000 : 120*1000); // 默认超时时间为 120 秒

    // 结束请求预览信息
    function stopRequestPreviewInfo() {
        window.clearInterval(timerId);
        stopped = true;
        complete && complete();
    }

    // 请求预览信息
    function requestPreviewInfo() {
        if (stopped) { return; }

        $.rest.get({ url, success: (result) => {
            if (result.success) {
                const previewResult   = result.data;
                const type            = previewResult.type; // 类型
                const previewUrl      = previewResult.url; // 预览的 URL
                const convertProgress = previewResult.progress; // 转换进度

                if (type === Utils.CAN_NOT_PREVIEW) {
                    // 不可预览
                    stopRequestPreviewInfo();
                    Utils.warning(`文件不可预览: ${uri}`);
                } else if (type === Utils.CAN_PREVIEW) {
                    // 可以预览
                    stopRequestPreviewInfo();
                    ready && ready(previewUrl);
                } else if (type === Utils.CONVERTING) {
                    // 转换中
                    progress && progress(convertProgress);

                    // 超时还在转换则结束请求
                    if (new Date().getTime() > expiredAt) {
                        stopRequestPreviewInfo();
                    }
                } else if (type === Utils.CONVERTING_ERROR) {
                    // 转换错误
                    stopRequestPreviewInfo();

                    Utils.warning('文件转换错误', '详细错误信息请查看控制台输出 (Chrome 按下快捷键 F12)');
                    console.error(previewResult.error);
                }
            } else {
                stopRequestPreviewInfo();
                Utils.warning(result.message);
            }
        }, fail: () => {
            stopRequestPreviewInfo();
        } });
    }

    requestPreviewInfo(); // 立即执行请求
    timerId = window.setInterval(requestPreviewInfo, 3000); // 每 3 秒执行请求一次
};

/**
 * 日期转为 JSON 字符串
 *
 * @return {String} 返回日期的字符串表示
 */
Date.prototype.toJSON = function() {
    return dayjs(this).format('YYYY-MM-DD HH:mm:ss'); // 使用 dayjs，输出 2019-09-30 11:10:53
};

/**
 * 异步加载 JS
 *
 * @param {String} url JS 的 URL
 * @param {String} id  JS <style> 的 ID，可选值，默认为 url 对应的文件名
 * @returns 返回 Promise
 */
Utils.loadJs = function(url, id) {
    return LoadTask.start(url, id, 'js');
};

/**
 * 异步加载 CSS
 *
 * @param {String} url CSS 的 URL
 * @param {String} id  CSS <link> 的 ID，可选值，默认为 url 对应的文件名
 * @returns 返回 Promise
 */
Utils.loadCss = function(url, id) {
    return LoadTask.start(url, id, 'css');
};

/**
 * 异步动态加载 JS 和 CSS 的任务类，如果同一个 ID 指定的资源已经加载过则不进行重复加载。
 */
class LoadTask {
    static STATE_INIT    = 0; // 初始化
    static STATE_LOADING = 1; // 加载中
    static STATE_SUCCESS = 2; // 加载成功
    static STATE_ERROR   = 3; // 加载失败

    /**
     * @param {String} url    JS 或者 CSS 的路径
     * @param {String} taskId 任务的 id
     */
    constructor(url, taskId) {
        this.id    = taskId;
        this.url   = url;
        this.state = LoadTask.STATE_INIT;
        this.queue = []; // 任务加载请求者队列，实际保存的是 Promise 的回调函数 { resolve, reject }
    }

    // 请求者入队
    add(requester) {
        this.queue.push(requester);
    }

    // 加载成功的回调函数
    loadSuccess() {
        this.state = LoadTask.STATE_SUCCESS;
        this.queue.forEach(p => p.resolve());
        this.queue = [];
    }

    // 加载失败的回调函数
    loadError() {
        this.state = LoadTask.STATE_ERROR;
        this.queue.forEach(p => p.reject());
        this.queue = [];
    }

    // 任务状态为初始化返回 true，否则返回 false
    isInit() {
        return this.state === LoadTask.STATE_INIT;
    }

    // 任务状态为加载中返回 true，否则返回 false
    isLoading() {
        return this.state === LoadTask.STATE_LOADING;
    }

    // 任务状态为加载成功返回 true，否则返回 false
    isSuccess() {
        return this.state === LoadTask.STATE_SUCCESS;
    }

    // 任务状态为加载失败返回 true，否则返回 false
    isError() {
        return this.state === LoadTask.STATE_ERROR;
    }

    /**
     * 开始加载
     *
     * @param {String} url  JS 或者 CSS 的 URL
     * @param {String} id   任务的 ID，同一个资源建议使用相同的 ID，避免重复加载
     * @param {String} type 加载类型，值为 'js' 或者 'css'
     * @returns 返回 Promise 对象，加载成功时回调它的 resolve 函数，失败时回调它的 reject 函数
     */
    static start(url, id, type) {
        // 1. 从全局的任务队列中获取 id 对应的任务，如果不存在则创建
        // 2. 开始加载，把任务的 Promise 加入任务的 queue，当加载完成后进行回调

        // [1] 从全局的任务队列中获取 id 对应的任务，如果不存在则创建
        window.loadingTasksMap = window.loadingTasksMap || new Map();
        const taskId = type + '-' + (id || Utils.getFilename(url)).replace(/\./g, '-'); // 替换名字中的 . 为 -，如: jquery.js 输出 jquery-js
        const task   = window.loadingTasksMap.get(taskId) || new LoadTask(url, taskId);
        window.loadingTasksMap.set(taskId, task);

        // [2] 开始加载，把任务的 Promise 加入任务的 queue，当加载完成后进行回调
        return new Promise((resolve, reject) => {
            task.add({ resolve, reject });
            task.doLoad(type);
        });
    }

    // 执行加载
    doLoad(type) {
        // 如果已经加载成功或者失败，调用相应的回调行数，加载中直接返回，避免重复加载
        if (this.isSuccess()) {
            this.loadSuccess();
            return;
        } else if (this.isError()) {
            this.loadError();
            return;
        } else if (this.isLoading()) {
            return;
        }

        // 初始化状态时开始加载
        this.state   = LoadTask.STATE_LOADING;
        const id     = this.id;
        const url    = this.url;
        const src    = url.includes('?') ? `${url}&_t=${id}` : `${url}?_t=${id}`;
        const source = document.createElement(type === 'js' ? 'script' : 'link');

        console.log('开始加载: ' + src);

        // 加载成功
        if (source.readyState) {
            // IE
            source.onreadystatechange = () => {
                if (source.readyState === 'loaded' || source.readyState === 'complete') {
                    source.onreadystatechange = null;
                    this.loadSuccess();
                    console.log('加载完成: ' + src);
                }
            };
        } else {
            // Other Browsers
            source.onload = () => {
                this.loadSuccess();
                console.log('加载完成: ' + src);
            };
        }

        // 加载失败
        source.onerror = () => {
            this.loadError();
            console.error('加载失败: ' + src);
        };

        if (type === 'js') {
            source.type = 'text/javascript';
            source.id   = id;
            source.src  = src;
            document.getElementsByTagName('head').item(0).appendChild(source);
        } else {
            source.rel  = 'stylesheet';
            source.id   = id;
            source.href = src;
            document.getElementsByTagName('head').item(0).appendChild(source);
        }
    }
}

/**
 * 深拷贝
 *
 * @param {Any} item 要拷贝的对象
 * @return 返回深拷贝的对象
 */
Utils.clone = function(item) {
    if (!item) { return item; } // null, undefined values check

    var types = [Number, String, Boolean];
    var result;

    // normalizing primitives if someone did new String('aaa'), or new Number('444');
    types.forEach(function(type) {
        if (item instanceof type) {
            result = type(item);
        }
    });

    if (typeof result === 'undefined') {
        if (Object.prototype.toString.call(item) === '[object Array]') {
            result = [];
            item.forEach(function (child, index, array) {
                result[index] = Utils.clone(child);
            });
        } else if (typeof item === 'object') {
            // testing that this is DOM
            if (item.nodeType && typeof item.cloneNode === 'function') {
                result = item.cloneNode(true);
            } else if (!item.prototype) { // check that this is a literal
                if (item instanceof Date) {
                    result = new Date(item);
                } else {
                    // it is an object literal
                    result = {};
                    for (var i in item) {
                        result[i] = Utils.clone(item[i]);
                    }
                }
            } else {
                // depending what you would like here, just keep the reference, or create new object
                if (false && item.constructor) {
                    // would not advice to do that, reason? Read below
                    result = new item.constructor();
                } else {
                    result = item;
                }
            }
        } else {
            result = item;
        }
    }

    return result;
};

/**
 * 生成唯一 ID，例如 1599699790761-5085301350
 *
 * @return{String} 返回唯一 ID
 */
Utils.uid = function() {
    return Date.now() + '-' + Math.floor(Math.random() * 10000000000);
};

/**
 * 处理 Rest 请求的响应
 *
 * @param {JSON} data 数据
 * @param {Bool} success 是否成功
 * @param {String} message 信息
 * @return {Promise} 返回处理结果的 Promise 对象
 */
Utils.response = function(data, success, message) {
    if (success) {
        return Promise.resolve(data);
    } else {
        Message.error({
            content : message,
            duration: 30,
            closable: true
        });

        return Promise.reject(message);
    }
};

/**
 * 在 duration 秒内每隔 interval 秒执行函数 processFunc 一次，当 repeat 结束时执行函数 finishFunc。
 *
 * 使用示例:
 * const cancelToken = Utils.retry(100, 3, () => { console.log(Date.now()) }, () => { console.log('Finished')});
 * cancelToken.cancel();
 *
 * @param {Integer}  duration    执行时间范围
 * @param {Integer}  interval    执行间隔
 * @param {Function} processFunc 重复执行的函数
 * @param {Function} finishFunc  结束的回调函数
 * @returns 返回取消操作对象，调用其 cancel() 函数可取消执行
 */
Utils.repeat = function(duration, interval, processFunc, finishFunc) {
    /* 逻辑:
     1. 定义取消操作的 Token，调用其 cancel() 函数则立即取消后续执行
     2. 定义递归循环执行的函数 doWork
     3. 如果已经完成或者取消了执行，doWork 就不在执行
     4. 执行用户指定的具体操作 processFunc
     5. 如果仍然在 duration 的时间范围内，使用递归计划下一次执行，否则结束递归
     */

    const startAt = Date.now();
    let finished  = false;
    let canceled  = false;
    duration     *= 1000;
    interval     *= 1000;

    // [1] 定义取消操作的 Token，调用其 cancel() 函数则立即取消后续执行
    const cancelToken = {
        cancel: function() {
            if (finished || canceled) {
                return;
            }

            canceled = true;
            finished = true;
            finishFunc && finishFunc();
        }
    };

    // [2] 定义递归循环执行的函数 doWork
    const doWork = () => {
        // [3] 如果已经完成或者取消了执行，doWork 就不在执行
        if (finished || canceled) {
            return;
        }

        // [4] 执行用户指定的具体操作 processFunc
        processFunc && processFunc();

        // [5] 如果仍然在 duration 的时间范围内，使用递归计划下一次执行，否则结束递归
        if (Date.now() - startAt + interval <= duration) {
            setTimeout(doWork, interval);
        } else {
            finished = true;
            finishFunc && finishFunc();
        }
    };

    // 开始执行
    doWork();

    return cancelToken;
};

// 定义为全局变量
window.Utils = Utils;
window.formatString = formatString;
