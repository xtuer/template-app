/**
 * 使用 Restful 风格对 Axios 进行封装，使用流式风格链式调用，所有类型的请求都使用统一的格式进行调用，请求的可配置项有:
 *     url     [String]: 请求的 URL，可以使用 url() 函数设置，也可以在调用 get(), update() 等时传入
 *     params  [JSON]  : 替换 URL 路径中的变量 (参考 vue router 的命名习惯)
 *     data    [JSON]  : 不管是 GET，还是 POST, PUT, PATCH 等都统一使用 JSON 对象传递参数，调用者不需要判断是否需要序列化
 *     headers [JSON]  : 请求头信息
 *     json    [Bool]  : 为 true 时使用 applicatoin/json 在请求体中传递 data 参数，
 *                       为 false 使用普通表单 application/x-www-form-urlencoded 传递参数 (即键值对)，
 *                       默认为 false
 *
 * 只有 url 是必要的参数，其他几个参数是可选的，根据需求传入，支持下面 4 种请求:
 *     获取资源: Rest.get()    -> GET    请求
 *     创建资源: Rest.create() -> POST   请求
 *     全量更新: Rest.update() -> PUT    请求
 *     部分更新: Rest.patch()  -> PATCH  请求
 *     删除资源: Rest.remove() -> DELETE 请求 (别名 Rest.del())
 *
 * 这几个函数都返回 Promise 对象，then 的参数为请求成功的响应，catch 的参数为失败的响应, 调用示例:
 *
 * [1] 普通 GET 请求
 * Rest.get('/api/rest').then(result => {});
 *
 * Rest.data({ pageNumber: 3 }).get('/api/rest').then(result => {
 *     console.log(result);
 * });
 *
 * 其他类型的请求只需要把 get() 替换为对应的函数即可，参数配置部分一样。
 *
 * [2] 替换 url 中的变量: 下面的 URL 中 {bookId} 会被替换为 params 的参数 bookId 的值 23，得到请求的 url '/rest/books/23'
 * Rest.url('/rest/books/{bookId}').params({ bookId: 23 }).data({ name: 'C&S' }).update().then(result => {
 *     console.log(result);
 * });
 *
 * [3] 设置 json 为 true 使用 request body 传输复杂的 data 对象 (有多级属性)
 * Rest.url('/api/uid').data({
 *     user: { username: 'Bob', password: '123456' },
 *     company: 'Appo'
 * }).json(true).get().then(result => {
 *     console.log(result);
 * });
 *
 * json 默认为 false，使用 application/x-www-form-urlencoded 的方式，即普通表单的方式。
 *
 * [4] Axios 不支持同步请求，但可以在同一个函数里使用 async await 进行同步操作:
 * async function syncFunc() {
 *     const r1 = await Rest.get('/api/rest1'); // r1 为 then 或者 catch 的参数
 *     const r2 = await Rest.data({ name: 'Goqu' }).create(/api/rest2');
 *
 *     console.log(r1, r2);
 * }
 * 注: jQuery 的 Ajax 支持同步请求，但是新版本中也不推荐使用了，浏览器中会有警告
 *
 * 提示:
 *     错误处理: 绝大多数时候不需要在 catch 中进行错误处理，已经默认提供了 401，403，404，服务器抛异常时的 500，服务不可达的 502 等错误处理: 弹窗提示和控制台打印错误信息。
 *     合并参数: data 综合了 axios 的 params 和 data 参数
 */
const Rest = {
    // 执行请求
    get: url => {
        return RestBuilder.newBuilder().get(url);
    },
    create: url => {
        return RestBuilder.newBuilder().create(url);
    },
    update: url => {
        return RestBuilder.newBuilder().update(url);
    },
    patch: url => {
        return RestBuilder.newBuilder().patch(url);
    },
    remove: url => {
        return RestBuilder.newBuilder().remove(url);
    },
    del: url => {
        return RestBuilder.newBuilder().remove(url);
    },

    // 请求配置
    url: url => {
        return RestBuilder.newBuilder().url(url);
    },
    data: data => {
        return RestBuilder.newBuilder().data(data);
    },
    params: params => {
        return RestBuilder.newBuilder().params(params);
    },
    json: yes => {
        return RestBuilder.newBuilder().json(yes);
    },
    headers: headers => {
        return RestBuilder.newBuilder().headers(headers);
    },
    /**
     * 上传文件
     *
     * @param {String} url 上传地址
     * @param {JSON} formData 表单数据
     */
    upload: (url, formData) => {
        return new Promise((resolve, reject) => {
            axios.post(url, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                onUploadProgress: progressEvent => {
                    let complete = (progressEvent.loaded / progressEvent.total * 100 || 0) + '%';
                    console.log(complete);
                }
            }).then(response => {
                if (response.data.success) {
                    const uplaodedFile = response.data.data;
                    resolve(uplaodedFile);
                } else {
                    reject(response.data.message);
                }
            }).catch(response => {
                const error = response.response;
                console.error(error);
                reject(error);
            });
        });
    }
};

/**
 * Rest 请求的配置类，使用 Builder 模式创建
 */
function RestBuilder() {

}

RestBuilder.newBuilder = function() {
    return new RestBuilder();
};

RestBuilder.prototype.url = function(url) {
    this.m_url = url || this.m_url; // 后设置的 URL 优先
    return this;
};

RestBuilder.prototype.method = function(method) {
    this.m_method = method;
    return this;
};

RestBuilder.prototype.data = function(data) {
    this.m_data = data;
    return this;
};

RestBuilder.prototype.params = function(params) {
    this.m_params = params;
    return this;
};

RestBuilder.prototype.headers = function(headers) {
    this.m_headers = headers;
    return this;
};

RestBuilder.prototype.json = function(yes) {
    this.m_json = yes;
    return this;
};

RestBuilder.prototype.build = function() {
    return {
        data   : this.m_data    || {},
        params : this.m_params  || {},
        headers: this.m_headers || {},
        json   : this.m_json    || false,
        url    : this.m_url     || 'UNKNOWN',
        method : this.m_method  || 'UNKNOWN',
    };
};

RestBuilder.prototype.get = function(optUrl) {
    const config = this.method('GET').url(optUrl).build();
    return RestExecutor.executeRequest(config);
};

RestBuilder.prototype.create = function(optUrl) {
    const config = this.method('POST').url(optUrl).build();
    return RestExecutor.executeRequest(config);
};

RestBuilder.prototype.update = function(optUrl) {
    const config = this.method('PUT').url(optUrl).build();
    return RestExecutor.executeRequest(config);
};

RestBuilder.prototype.patch = function(optUrl) {
    const config = this.method('PATCH').url(optUrl).build();
    return RestExecutor.executeRequest(config);
};

RestBuilder.prototype.remove = function(optUrl) {
    const config = this.method('DELETE').url(optUrl).build();
    return RestExecutor.executeRequest(config);
};

/**
 * Rest 请求执行类
 */
class RestExecutor {
    /**
     * 执行请求
     */
    static executeRequest({ url, params, data, json, method, headers }) {
        // 保证把 Date 对象转为字符串正确的传给服务器端，其格式由 Date.prototype.toJSON() 确定，推荐使用 yyyy-MM-dd HH:mm:ss
        data = JSON.parse(JSON.stringify(data || {}));

        return new Promise((resolve, reject) => {
            // url 变量不存在，立即返回
            if (!url) {
                reject(new Error('URL undefined'));
                return;
            }

            // 如果是 GET，把数组变为字符串: [1, 2, 3] 转换为字符串 '1,2,3'
            if (method === 'GET') {
                for (let key in data) {
                    if (Array.isArray(data[key])) {
                        data[key] += '';
                    }
                }
            }

            // json 为 false，构建 POST, PUT, DELETE, PATCH 请求的参数，对象需要序列化为字符串,
            // json 为 true，则 data 仍然使用 JSON 格式，放在 request body 里即可。
            //
            // 注: 此处结合 Spring MVC 的 HiddenHttpMethodFilter 拦截器，使用 POST 执行 PUT, PATCH, DELETE, PATCH 请求时的额外参数 _method
            //     PUT   : _method=PUT
            //     PATCH : _method=PATCH
            //     DELETE: _method=DELETE
            // 服务器端是其他框架的话，根据具体情况进行修改
            if (!json && method !== 'GET') {
                data = RestExecutor.serializeData({ ...data, _method: method });
                method = 'POST';
            }

            // content type 为 applicatoin/json 时 data 为 json 对象，使用 request body 传输
            // content type 为 application/x-www-form-urlencoded 时 data 为 key=value 字符串 (FormData)
            let options = {
                url: RestExecutor.formatUrl(url, params), // 替换 URL 上的占位变量
                data,                                     // 非 GET 请求时使用
                params: (method === 'GET') ? data: {},    // GET 请求时 URL 后面的 query parameters
                method,
                headers,
                responseType: 'json',
            };

            // 服务器抛异常时，有时 Windows 的 Tomcat 环境下竟然取不到 header X-Requested-With, Mac 下没问题，
            // 正常请求时都是好的，手动添加 X-Requested-With 为 XMLHttpRequest 后所有环境下正常和异常时都能取到了
            options.headers['X-Requested-With'] = 'XMLHttpRequest';
            options.headers['Content-Type'] = json ? 'application/json;charset=UTF-8' : 'application/x-www-form-urlencoded';

            // 执行请求
            axios(options).then(response => {
                resolve(response.data);
            }).catch(response => {
                const error = response.response;
                RestExecutor.handleError(error);
                reject(error);
            });
        });
    }

    /**
     * 错误处理
     */
    static handleError(error) {
        const status = error.status;

        if (401 === status) {
            alert('401: Token 无效');
        } else if (403 === status) {
            alert('403: 权限不够');
        } else if (404 === status) {
            alert('404: URL 不存在');
        } else if (500 === status) {
            if (error.data && error.data.message) {
                // 发生 500 错误时服务器抛出异常，在控制台打印出异常信息
                console.error(error.data.message);
                console.error(error.data.data);
                alert(`500: 发生异常，${error.data.message}\n\n详细错误信息请查看控制台输出 (Chrome 按下快捷键 F12)`);
            }
        } else if (502 === status) {
            // 发生 502 错误时，Tomcat Web 服务器不可访问，一般有 2 个原因
            // 1. Nginx 配置出错
            // 2. Tomcat 的 Web 服务没启动或者不接收请求
            alert('502: 服务不可访问');
        } else if (504 === status) {
            alert('504: Gateway Timeout\n\n' + error.statusText);
        }
    }

    /**
     * 替换 URL 路径中的变量，例如 /rest/users/{id}，其中 {id} 替换为 params.id 的值
     *
     * @param {String} url    URL 例如 /rest/users/{id}，如果没有占位的变量，则原样返回
     * @param {Json}   params Json 对象
     * @return {String} 返回替换变量后的 URL
     */
    static formatUrl(url, params) {
        if (!params) {
            return url;
        }

        // 查找 {{、}}、或者 {name}，然后进行替换
        // m 是正则中捕获的组 $0，即匹配的整个子串
        // n 是正则中捕获的组 $1，即 () 中的内容
        // function($0, $1, $2, ...)
        return url.replace(/\{\{|\}\}|\{(\w+)\}/g, function(m, n) {
            if (m === '{{') { return '{'; }
            if (m === '}}') { return '}'; }

            return params[n];
        });
    }

    /**
     * 序列化 data 为 key value 的字符串 key1=value1&key2=value2
     *
     * @param {Json} data Json 对象
     * @return 返回 key value 的字符串
     */
    static serializeData(data) {
        return [...Object.keys(data)]
            .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
            .join('&');
    }
}

window.Rest = Rest;
