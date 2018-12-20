/*-----------------------------------------------------------------------------|
 |                                 字符串格式化                                  |
 |----------------------------------------------------------------------------*/
/**
 * 扩展了 String 类型，给其添加格式化的功能，替换字符串中 {placeholder} 或者 {0}, {1} 等模式部分为参数中传入的字符串
 * 使用方法:
 *     'I can speak {language} since I was {age}'.format({language: 'Javascript', age: 10})
 *     'I can speak {0} since I was {1}'.format('Javascript', 10)
 * 输出都为:
 *     I can speak Javascript since I was 10
 *
 * @param replacements 用来替换 placeholder 的 JSON 对象或者数组
 */
String.prototype.format = function(replacements) {
    replacements = (typeof replacements === 'object') ? replacements : Array.prototype.slice.call(arguments, 0);
    return formatString(this, replacements);
};

/**
 * 替换字符串中 {placeholder} 或者 {0}, {1} 等模式部分为参数中传入的字符串
 * 使用方法:
 *     formatString('I can speak {language} since I was {age}', {language: 'Javascript', age: 10})
 *     formatString('I can speak {0} since I was {1}', 'Javascript', 10)
 * 输出都为:
 *     I can speak Javascript since I was 10
 *
 * @param str 带有 placeholder 的字符串
 * @param replacements 用来替换 placeholder 的 JSON 对象或者数组
 */
var formatString = function (str, replacements) {
    replacements = (typeof replacements === 'object') ? replacements : Array.prototype.slice.call(arguments, 1);
    return str.replace(/\{\{|\}\}|\{(\w+)\}/g, function(m, n) {
        if (m == '{{') { return '{'; }
        if (m == '}}') { return '}'; }
        return replacements[n];
    });
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
    var normalized       = this.replace(/[^a-zA-Z0-9]/g, '-');
    var normalizedFormat = format.replace(/[^a-zA-Z0-9]/g, '-');
    var formatItems      = normalizedFormat.split('-');
    var dateItems        = normalized.split('-');

    var monthIndex   = formatItems.indexOf('MM');
    var dayIndex     = formatItems.indexOf('dd');
    var yearIndex    = formatItems.indexOf('yyyy');
    var hourIndex    = formatItems.indexOf('hh');
    var minutesIndex = formatItems.indexOf('mm');
    var secondsIndex = formatItems.indexOf('ss');

    var today = new Date();

    var year  = yearIndex>-1  ? dateItems[yearIndex]    : today.getFullYear();
    var month = monthIndex>-1 ? dateItems[monthIndex]-1 : today.getMonth()-1;
    var day   = dayIndex>-1   ? dateItems[dayIndex]     : today.getDate();

    var hour    = hourIndex>-1    ? dateItems[hourIndex]    : today.getHours();
    var minute  = minutesIndex>-1 ? dateItems[minutesIndex] : today.getMinutes();
    var second  = secondsIndex>-1 ? dateItems[secondsIndex] : today.getSeconds();

    return new Date(year, month, day, hour, minute, second);
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
 * @param fmt 目标字符串格式，支持的字符有：y,M,d,q,w,H,h,m,S，默认：yyyy-MM-dd HH:mm:ss
 * @returns 返回格式化后的日期字符串
 */
Date.prototype.format = function (fmt) {
    fmt = fmt || 'yyyy-MM-dd HH:mm:ss';
    var obj = {
        'y': this.getFullYear(),    // 年份，注意必须用getFullYear
        'M': this.getMonth() + 1,   // 月份，注意是从0-11
        'd': this.getDate(),        // 日期
        'q': Math.floor((this.getMonth() + 3) / 3), // 季度
        'w': this.getDay(),         // 星期，注意是0-6
        'H': this.getHours(),       // 24小时制
        'h': this.getHours() % 12 == 0 ? 12 : this.getHours() % 12, // 12小时制
        'm': this.getMinutes(),     // 分钟
        's': this.getSeconds(),     // 秒
        'S': this.getMilliseconds() // 毫秒
    };
    var week = ['天', '一', '二', '三', '四', '五', '六'];
    for (var i in obj) {
        fmt = fmt.replace(new RegExp(i + '+', 'g'), function (m) {
            var val = obj[i] + '';
            if (i == 'w') return (m.length > 2 ? '星期' : '周') + week[val];
            for (var j = 0, len = val.length; j < m.length - len; j++) val = '0' + val;
            return m.length == 1 ? val : val.substring(val.length - m.length);
        });
    }
    return fmt;
};

/*-----------------------------------------------------------------------------|
 |                                   Utils                                     |
 |----------------------------------------------------------------------------*/
function Utils() {

}

// iView 的 Notice，需要注入，例如在 vue 的 created() 中注入: Utils.Notice = this.$Notice
Utils.Notice = null;

/**
 * 显示警告信息，默认使用 iView 的 Notice，如果没有设置则使用 alert()
 *
 * @param  {String}  title 标题
 * @param  {String}  desc  描述
 * @param  {Integer} duration  关闭时间，单位为秒
 * @return 无返回值
 */
Utils.warning = function(title, desc = '', duration = 30) {
    if (Utils.Notice) {
        Utils.Notice.warning({ title, desc, duration });
    } else {
        alert(`${title}\n${desc}`);
    }
};

// 成功
Utils.success = function(title, desc = '', duration = 4.5) {
    if (Utils.Notice) {
        Utils.Notice.success({ title, desc, duration });
    } else {
        alert(`${title}\n${desc}`);
    }
};

// 通知
Utils.info = function(title, desc = '', duration = 4.5) {
    if (Utils.Notice) {
        Utils.Notice.info({ title, desc, duration });
    } else {
        alert(`${title}\n${desc}`);
    }
};

Utils.notice = function(title, desc = '', duration = 4.5) {
    if (Utils.Notice) {
        Utils.Notice.info({ title, desc, duration });
    } else {
        alert(`${title}\n${desc}`);
    }
};

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
 * 获取文件名的后缀
 *
 * @param  {String} filename 文件名或者 URI、URL
 * @return {String} 返回文件名的后缀
 */
Utils.getFilenameExtension = function(filename) {
    var parser = document.createElement('a');
    parser.href  = filename;

    filename     = parser.pathname.toLowerCase(); // 去掉参数等和文件名无关的部分
    const dotPos = filename.lastIndexOf('.');
    const ext    = filename.substring(dotPos+1);

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

    return type ? type : 'file-type-other';
};

/**
 * 判断文件是否图片
 *
 * @param  {String} filename 文件名或者 URI、URL
 * @return {Boolean} 文件是图片时返回 true，否则返回 false
 */
Utils.isImage = function(filename) {
    const ext = Utils.getFilenameExtension(filename);

    if ("png" === ext || "jpg" === ext || "jpeg" === ext || "gif" === ext) {
        return true;
    } else {
        return false;
    }
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
 * 从 100 开始生成新的 ID
 *
 * @return {Integer} 返回页面打开后按顺序生成的 ID
 */
Utils.nextId = function() {
    if (window.newGeneratedId) {
        window.newGeneratedId += 1;
    } else {
        window.newGeneratedId = 100;
    }

    return window.newGeneratedId;
};

Utils.BASE_CN_NUMBERS = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十'];

/**
 * 阿拉伯数字转为中文数字，支持 [0, 99]
 *
 * @param  {Integer} n 阿拉伯数字
 * @return {String}  返回 [0, 99] 之间的中文数字字符串，超出范围的返回 ''
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
            return '十' + (ge!=0?Utils.BASE_CN_NUMBERS[ge]:'');
        } else {
            // 20-99: 二十一、二十二、...
            return Utils.BASE_CN_NUMBERS[shi] + '十' + (ge==0?'':Utils.BASE_CN_NUMBERS[ge]);
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

/**
 * 判断 html 是否包裹在 tag 标签中，也就是最上级的标签是否传入的 tag,
 * 例如:
 *     <p>Hello</p> 的最上级标签是 p, 则 Utils.isWrappedIn(html, 'p') 返回 true
 *     <div>Hello</div> 的最上级标签是 div, 则 Utils.isWrappedIn(html, 'p') 返回 false
 *     ABC<p>Hello</p> 是多个平级节点, 则 Utils.isWrappedIn(html, 'p') 返回 false
 *
 * @param  {String} html 要判断是否被包裹的 html
 * @param  {String} tag  最上级的标签 tag 名字
 * @return {Boolean} 如果 html 的最上级标签是传入的 tag 则返回 true，否则返回 false
 */
Utils.isWrappedIn = function(html, tag) {
    try {
        var tagName = $(html).get(0).tagName;

        if (tagName === tag.toUpperCase()) {
            return true;
        }
    } catch (ex) {
        // No root
    }

    return false;
};

/**
 * 把 html 包裹在 p 标签中，如果已经被包裹在了 p 标签中就不再重复包裹
 * 特殊: 如果 html 为空则返回 <p><br></p>
 *
 * @param  {String} html 要被包裹的 html
 * @param  {String} tag  用于包裹的标签名
 * @return {String} 返回包裹后的 html
 */
Utils.wrapIn = function(html, tag) {
    html = html || '<br>'; // 如果为空则赋值为 <br>
    return Utils.isWrappedIn(html, tag) ? html : `<p>${html}</p>`;
};

/**
 * 使用百度地图 API 获取当前城市
 *
 * @param {String}   BMapAk   百度地图的 App Key (在 config.js 中有定义)
 * @param {Function} callback 请求成功的回调函数，参数为当前城市，格式为 (adcode 为城市编码):
 *     {
 *         "country"          : "中国",
 *         "country_code"     : 0,
 *         "country_code_iso" : "CHN",
 *         "country_code_iso2": "CN",
 *         "province"         : "北京市",
 *         "city"             : "北京市",
 *         "city_level"       : 2,
 *         "district"         : "东城区",
 *         "town"             : "",
 *         "adcode"           : "110101",
 *         "street"           : "中华路",
 *         "street_number"    : "甲10号",
 *         "direction"        : "西南",
 *         "distance"         : "85"
 *     }
 * @return 无返回值
 */
Utils.locateCurrentCity = function(BMapAk, callback) {
    new BMap.Geolocation().getCurrentPosition(function(r) {
        const position  = r.point;      // 位置
        const latitude  = position.lat; // 纬度
        const longitude = position.lng; // 经度
        const url = `https://api.map.baidu.com/geocoder/v2/?ak=${BMapAk}&location=${latitude},${longitude}&output=json&pois =1`;

        if (this.getStatus() == BMAP_STATUS_SUCCESS) {
            $.ajax({
                url     : url,
                dataType: 'jsonp',
                callback: 'BMap._rd._cbk43398',
                success : function(response) {
                    const city     = response.result.addressComponent;
                    city.longitude = longitude;
                    city.latitude  = latitude;

                    callback(city);
                }
            });
        } else {
            Utils.warning('定位失败');
        }
    }, { enableHighAccuracy: true} ); // 指示浏览器获取高精度的位置，默认false
};


/**
 * 获取指定经纬度地区的天气
 *
 * @param {String}   longitude  经度
 * @param {String}   latitude   纬度
 * @param {String}   aliAppCode 阿里墨迹天气的 App Code
 * @param {Function} callback   请求成功的回调函数，参数为天气对象:
 * @return 无返回值
 */
Utils.getWeather = function(longitude, latitude, aliAppCode, callback) {
    const briefforecast3daysUrl = 'http://apifreelat.market.alicloudapi.com/whapi/json/aliweather/briefforecast3days'; // 3 天的天气预报，有每天的最高最低温度
    const briefconditionUrl     = 'http://apifreelat.market.alicloudapi.com/whapi/json/aliweather/briefcondition';     // 实时天气预报，有当前的温度和天气状况
    const weather = {};

    // 请求 3 天的天气预报
    $.ajax({
        url     : briefforecast3daysUrl,
        type    : 'POST',
        dataType: 'json',
        headers : { 'Authorization': `APPCODE ${aliAppCode}` },
        data    : { lat: latitude, lon: longitude }
    }).done((forecast3DaysResult) => {
        const todayWeather     = forecast3DaysResult.data.forecast[0]; // forecast 里有 3 天的天气预报，第一个为今天的
        weather.minTemperature = Math.min(todayWeather.tempDay, todayWeather.tempNight); // 最低温度
        weather.maxTemperature = Math.max(todayWeather.tempDay, todayWeather.tempNight); // 最高温度

        // 请求实时天气
        $.ajax({
            url     : briefconditionUrl,
            type    : 'POST',
            dataType: 'json',
            headers : { 'Authorization': `APPCODE ${aliAppCode}` },
            data    : { lat: latitude, lon: longitude }
        }).done((briefConditionResult) => {
            const data          = briefConditionResult.data;
            weather.city        = data.city.pname + data.city.name;
            weather.condition   = data.condition.condition;
            weather.temperature = data.condition.temp;

            callback(weather);
        }).fail((error) => {
            Utils.warning('请求实时天气出错');
            console.error(error);
        });
    }).fail((error) => {
        Utils.warning('请求 3 天天气预报出错');
        console.error(error);
    });
};

// 天气状况
Utils.WEATHER_CONDITIONS = [
    '晴', '阴', '雾', '中雨', '中雪', '多云', '大雨', '大雪', '小雨', '小雪',
    '暴雨', '暴雪', '阵雨', '大暴雨', '雨加雪', '雷阵雨', '特大暴雨'
];

/**
 * 使用天气情况获取天气的图片
 *
 * @param  {String} condition 天气状况
 * @return {String} 返回天气状况对应的图片地址
 */
Utils.getWeatherConditionImage = function(condition) {
    if (Utils.WEATHER_CONDITIONS.indexOf(condition) >= 0) {
        return `/static/img/weather/${condition}.png`;
    } else {
        return '/static/img/weather/阴.png';
    }
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
        }});
    }

    requestPreviewInfo(); // 立即执行请求
    timerId = window.setInterval(requestPreviewInfo, 3000); // 每 3 秒执行请求一次
};
