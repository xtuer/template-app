/**
 * 对 Axios 进行 Restful 规范进行封装，使用 fluent 流式风格的链式调用，所有类型的请求都使用统一的格式进行调用，请求的可配置项有:
 *     url     [String]: 请求的 URL，可以使用 url() 函数设置，也可以在调用 get(url), update(url) 等时传入。
 *     params  [JSON]  : 替换 URL 路径中的变量 (参考 vue router 的命名习惯，例如 /users/{userId})。
 *     data    [JSON]  : 不管是 GET，还是 POST, PUT, PATCH 等请求都统一使用 JSON 对象传递参数，调用者不需要判断参数是否需要序列化。
 *     headers [JSON]  : 请求头信息。
 *     json    [Bool]  : 为 true 时使用 application/json 在请求体中传递 data 参数。
 *                       为 false 使用普通表单 application/x-www-form-urlencoded 传递参数 (即键值对)。
 *                       默认为 false。
 *
 * 只有 url 是必要的参数，其他几个参数是可选的，根据需求传入，支持下面 4 种请求:
 *     获取资源: Rest.get<T>()    -> GET    请求。
 *     创建资源: Rest.create<T>() -> POST   请求。
 *     全量更新: Rest.update<T>() -> PUT    请求。
 *     部分更新: Rest.patch<T>()  -> PATCH  请求。
 *     删除资源: Rest.delete<T>() -> DELETE 请求 (别名 Rest.del(), Rest.remove()，有的浏览器可能不允许使用 delete 作为方法名)。
 * 这些请求的函数的返回值都是泛型的 Promise<Response<T>>，then 的参数为请求成功的响应 Response<T>，catch 的参数为失败的错误, 调用示例:
 *
 * [1] 普通 GET 请求
 * 提示: rsp 会自动推导出类型 Response<string>，不需要明确的写。
 * Rest.get<string>('/api/rest').then(rsp => {});
 *
 * Rest.data({ pageNumber: 3 }).get<string>('/api/rest').then((rsp: Response<string>) => {
 *     console.log(rsp);
 * });
 *
 * 其他类型的请求只需要把 get<T>() 替换为对应的函数即可，参数配置部分一样。
 *
 * [2] 替换 url 中的变量: 下面的 URL 中 {bookId} 会被替换为 params 的参数 bookId 的值 23，得到请求的 url '/rest/books/23'
 * Rest.url('/rest/books/{bookId}').params({ bookId: 23 }).data({ name: 'C&S' }).update<boolean>().then(rsp => {
 *     console.log(rsp);
 * });
 *
 * [3] 设置 json 为 true 使用 request body 传输复杂的 data 对象 (可以有多级属性)
 * Rest.url('/api/uid').data({
 *     user: { username: 'Bob', password: '123456' },
 *     company: 'App'
 * }).json(true).create<User>().then(({ data: user, success, message }) => {
 *     console.log(user);
 * });
 *
 * json 默认为 false，使用 application/x-www-form-urlencoded 的方式，即普通表单的方式。
 *
 * [4] Axios 不支持同步请求，但可以在同一个函数里使用 async await 进行同步操作:
 * async function syncFunc() {
 *     const r1 = await Rest.get('/api/rest1'); // r1 为 resolve 的参数。
 *     const r2 = await Rest.data({ name: 'Goo' }).create<Foo>(/api/rest2');
 *
 *     console.log(r1, r2);
 * }
 * 注: jQuery 的 Ajax 支持同步请求，但是新版本中也不推荐使用了，浏览器中会有警告。
 *
 * [5] 请求成功表示与服务器通信成功，不代码业务处理成功。
 *     使用 Rest.normalize() 根据 success 对响应统一的进行业务逻辑判断，success 为 true 表示业务处理成功，为 false 表示失败。
 *     代码中逻辑更关注成功业务处理，大多数时候都可以使用 Rest.normalize() 简化开发，除非 success 的值不足以判断，
 *     需要使用响应的 code 进行更多情况处理。
 *
 * [5.1] Api 接口文件中处理请求。
 * async function findTime(): Promise<number> {
 *     return Rest.get<number>(url).then(({ data: time, success, message }) => {
 *         // 提示: 参数里进行了一次解构是为了让调用者知道 data 的业务名称，方便代码的维护。
 *         return Rest.normalize({ data: time, success, message });
 *     });
 * }
 *
 * [5.2] Vue 文件中调用请求直接获取结果，忽略请求的细节，而且即使切换了 Api 的实现不需要修改 vue 中的代码 (如把 Axios 换为 fetch 实现)。
 * findTime().then((time: number) => {
 *     console.log(time);
 * })
 * const time: number = await findTime();
 *
 * 提示:
 *     错误处理: 绝大多数时候不需要在 catch 中进行错误处理，已经默认提供了 401，403，404，服务器抛异常时的 500，服务不可达的 502 等错误处理: 弹窗提示和控制台打印错误信息。
 *     合并参数: data 综合了 axios 的 params 和 data 参数，不管是啥类型的请求，参数只管使用 JSON 对象即可。
 */

import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';

/**
 * 显示错误信息，可以根据使用的 Ui 框架进行修改。
 *
 * @param error 错误信息。
 */
function showError(error: any): void {
    alert(error)
}

/**
 * 请求响应的数据结构，业务数据 data 使用了泛型。
 */
interface Response<T> {
    success: boolean, // 表示请求的业务逻辑成功与否。
    data   : T,       // 请求的业务数据，成功时数据应该放 data 里。
    code   : number,  // 响应的业务 code，一般情况下不需要使用，比较特殊的是服务器发生异常时值为 500。
    message: string,  // 请求的 message，大多是错误时使用。
    stack  : string,  // 服务器发生异常时的堆栈信息。
}

/**
 * Rest 请求发起的类。
 */
class Rest {
    // 执行请求
    static get<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().get(url);
    }

    static create<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().create(url);
    }

    static update<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().update(url);
    }

    static patch<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().patch(url);
    }

    static delete<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().remove(url);
    }
    static remove<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().remove(url);
    }
    static del<T>(url: string = ''): Promise<Response<T>> {
        return RestBuilder.newBuilder().remove(url);
    }

    // 请求配置
    static url(url: string): RestBuilder {
        return RestBuilder.newBuilder().url(url);
    }

    static data(data: {[key: string | symbol]: any}): RestBuilder {
        return RestBuilder.newBuilder().data(data);
    }

    static params(params: {[key: string | symbol]: any}): RestBuilder {
        return RestBuilder.newBuilder().params(params);
    }

    static json(yes: boolean): RestBuilder {
        return RestBuilder.newBuilder().json(yes);
    }

    static headers(headers: {[key: string | symbol]: any}): RestBuilder {
        return RestBuilder.newBuilder().headers(headers);
    }

    /**
     * 根据 success 统一判断响应的业务逻辑是否成功，失败时弹窗显示错误信息:
     * - 当 success 为 true 时, 表示请求业务逻辑成功，执行 resolve(), 参数为 data。
     * - 当 success 为 false 时，表示请求的业务逻辑失败，执行 reject(), 参数为 message。
     *
     * @param rsp 请求响应 Response 的对象。
     * @returns
     */
    static normalize<T>(rsp: { data: T, success: boolean, message: string }): Promise<T> {
        if (rsp.success) {
            return Promise.resolve(rsp.data);
        } else {
            // TODO: 根据使用的 Ui 框架处理弹窗。
            // Message.error({
            //     content : message,
            //     duration: 30,
            //     closable: true
            // });
            showError(rsp.message);

            return Promise.reject(rsp.message);
        }
    }
}

/**
 * Rest 请求的构建器，使用 Builder 模式创建。
 * 类型 {[key: string]: any} 表示普通的 JSON 对象，例如 { name: 'Alice', id: 12 }。
 */
class RestBuilder {
    m_url    : string               = '';
    m_method : string               = '';
    m_data   : {[key: string]: any} = {};
    m_params : {[key: string]: any} = {};
    m_headers: {[key: string]: any} = {};
    m_json   : boolean              = false;

    static newBuilder(): RestBuilder {
        return new RestBuilder();
    }

    url(url: string): RestBuilder {
        this.m_url = url || this.m_url;
        return this;
    }

    method(method: string): RestBuilder {
        this.m_method = method;
        return this;
    }

    data(data: {[key: string]: any}): RestBuilder {
        this.m_data = data;
        return this;
    }

    params(params: {[key: string]: any}): RestBuilder {
        this.m_params = params;
        return this;
    }

    headers(headers: {[key: string]: any}): RestBuilder {
        this.m_headers = headers;
        return this;
    }

    json(json: boolean): RestBuilder {
        this.m_json = json;
        return this;
    }

    build(): RequestOptions {
        return {
            data   : this.m_data    || {},
            params : this.m_params  || {},
            headers: this.m_headers || {},
            json   : this.m_json    || false,
            url    : this.m_url     || '',
            method : this.m_method  || 'UNKNOWN',
        }
    }

    get<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('GET').url(optUrl).build();

        // return Promise.resolve({ code: 0, success: true, message: '', data: 10 as T });
        return RestExecutor.executeRequest<T>(config);
    }

    create<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('POST').url(optUrl).build();
        return RestExecutor.executeRequest<T>(config);
    }

    update<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('PUT').url(optUrl).build();
        return RestExecutor.executeRequest<T>(config);
    }

    patch<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('PATCH').url(optUrl).build();
        return RestExecutor.executeRequest<T>(config);
    }

    delete<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('DELETE').url(optUrl).build();
        return RestExecutor.executeRequest<T>(config);
    }
    remove<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('DELETE').url(optUrl).build();
        return RestExecutor.executeRequest<T>(config);
    }
    del<T>(optUrl: string = ''): Promise<Response<T>> {
        const config: RequestOptions = this.method('DELETE').url(optUrl).build();
        return RestExecutor.executeRequest<T>(config);
    }
}

/**
 * 执行请求的参数。
 */
interface RequestOptions {
    url    : string,
    params : {[key: string | symbol]: any},
    data   : {[key: string | symbol]: any} | string | any,
    headers: {[key: string | symbol]: any},
    json   : boolean,
    method : string,
}

/**
 * Rest 请求执行类
 */
class RestExecutor {
    /**
     * 执行请求
     */
    static executeRequest<T>({ url, params, data, json, method, headers } : RequestOptions): Promise<Response<T>> {
        // 保证把 Date 对象转为字符串正确的传给服务器端，其格式由 Date.prototype.toJSON() 确定，推荐使用 yyyy-MM-dd HH:mm:ss。
        data = JSON.parse(JSON.stringify(data || {}));

        return new Promise((resolve, reject) => {
            // url 变量不存在，立即返回
            if (!url) {
                console.error(`请求的 URL 无效: ${url}`);
                reject(new Error('URL undefined'));
                return;
            }

            // 如果是 GET，把数组变为字符串: [1, 2, 3] 转换为字符串 '1,2,3'。
            // 在 URL 里变为 '/foo?ids=1,2,3'。
            if (method === 'GET') {
                for (const key in data) {
                    if (Array.isArray(data[key])) {
                        data[key] += '';
                    }
                }
            }

            // json 为 false，构建 POST, PUT, DELETE, PATCH 请求的参数，参数对象 data 需要序列化为字符串,
            // json 为 true，则 data 仍然使用 JSON 对象格式，放在 request body 里即可。
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

            // content type 为 application/json 时 data 为 json 对象，使用 request body 传输
            // content type 为 application/x-www-form-urlencoded 时 data 为 key=value 字符串 (FormData)
            const options = {
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
            axios(options as AxiosRequestConfig).then(response => {
                const rsp = response.data as Response<T>;

                if (rsp.code === 500) {
                    // 响应中 code 为 500 则表示服务器端抛出异常。
                    console.error(rsp.stack);
                    showError(rsp.message);
                    reject(rsp.message);
                } else {
                    // 响应正常。
                    resolve(rsp);
                }
            }).catch(response => {
                // Axios 的错误响应。
                const error = response.response;
                RestExecutor.handleError(error);
                reject(error);
            });
        });
    }

    /**
     * 错误处理。
     */
    static handleError(error: any): void {
        const status = error.status;

        if (401 === status) {
            showError('401: Token 无效');
        } else if (403 === status) {
            showError('403: 权限不够');
        } else if (404 === status) {
            showError('404: URL 不存在');
        } else if (500 === status) {
            if (error.data && error.data.message) {
                // 发生 500 错误时服务器抛出异常，在控制台打印出异常信息
                console.error(error.data.message);
                console.error(error.data.data);
                showError(`500: 发生异常，${error.data.message}\n\n详细错误信息请查看控制台输出 (Chrome 按下快捷键 F12)`);
            }
        } else if (502 === status) {
            // 发生 502 错误时，Tomcat Web 服务器不可访问，一般有 2 个原因
            // 1. Nginx 配置出错
            // 2. Tomcat 的 Web 服务没启动或者不接收请求
            showError('502: 服务不可访问');
        } else if (504 === status) {
            showError('504: Gateway Timeout\n\n' + error.statusText);
        }
    }

    /**
     * 替换 URL 路径中的变量，例如 /rest/users/{id}，其中 {id} 替换为 params.id 的值。
     *
     * @param url    URL 例如 /rest/users/12, /rest/users/{id}。
     * @param params 路径参数对象。
     * @return 返回替换变量后的 URL。
     */
    static formatUrl(url: string, params: any): string {
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
     * @param data 请求参数对象。
     * @return 返回 key value 的字符串。
     */
    static serializeData(data: any): string {
        return [...Object.keys(data)]
            .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
            .join('&');
    }
}

export { Rest };
export type { Response };
