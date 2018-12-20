(function($) {
    /**
     * 执行 REST 请求的 jQuery 插件，不以 sync 开头的为异步请求，以 sync 开头的为同步请求:
     *      Get    请求调用 $.rest.get(),    $.rest.syncGet()
     *      Create 请求调用 $.rest.create(), $.rest.syncCreate()
     *      Update 请求调用 $.rest.update(), $.rest.syncUpdate()
     *      Delete 请求调用 $.rest.remove(), $.rest.syncRemove()
     *
     * 默认使用 contentType 为 application/x-www-form-urlencoded 的方式提交请求，只能传递简单的 key/value，
     * 就是普通的 form 表单提交，如果想要向服务器传递复杂的 json 对象，可以使用 contentType 为 application/json 的格式，
     * 此时只要设置请求的参数 jsonRequestBody 为 true 即可，例如
     *      $.rest.update({url: '/rest', data: {name: 'Alice'}, jsonRequestBody: true, success: function(result) {
     *          console.log(result);
     *      }});
     *
     * 调用示例:
     *      // 异步请求
     *      $.rest.get({url: '/rest', data: {name: 'Alice'}, success: function(result) {
     *          console.log(result);
     *      }});
     *
     *      // 同步请求
     *      $.rest.syncGet({url: '/rest', data: {name: 'Alice'}, success: function(result) {
     *          console.log(result);
     *      }});
     *
     *      // url 中的 bookId 会被替换为 pathVariables 中的 bookId
     *      $.rest.update({url: '/rest/books/{bookId}', pathVariables: {bookId: 23}, data: {name: 'C&S'}, success: function(result) {
     *          console.log(result);
     *      }}, fail: function(failResponse) {});
     * 提示:
     *     绝大多数时候不需要传入 fail 的回调函数，已经默认提供了 401，403，404，服务器抛异常时的 500，服务不可达的 502 等错误处理: 弹窗提示和打印错误信息。
     */
    $.rest = {
        /**
         * 使用 Ajax 的方式执行 REST 的 GET 请求(服务器响应的数据根据 REST 的规范，必须是 Json 对象，否则浏览器端会解析出错)。
         * 如果没有设置 fail 的回调函数，则默认会把错误信息打印到控制台，可自定义 $.rest.defaultFail 函数例如使用弹窗显示错误信息。
         *
         * 以下几个 REST 的函数 $.rest.create(), $.rest.update(), $.rest.remove() 只是请求的 HTTP 方法和 data 处理不一样，
         * 其他的都是相同的，所以就不再重复注释说明了。
         *
         * @param {Json} options 有以下几个选项:
         *               {String}   url       请求的 URL        (必选)
         *               {Json}     pathVariables URL 中的变量，例如 /rest/users/{id}，其中 {id} 为要被 pathVariables.id 替换的部分(可选)
         *               {Json}     data      请求的参数         (可选)
         *               {Boolean}  jsonRequestBody 是否使用 application/json 的方式进行请求，默认为 false 不使用(可选)
         *               {Function} success   请求成功时的回调函数(可选)
         *               {Function} fail      请求失败时的回调函数(可选)
         *               {Function} complete  请求完成后的回调函数(可选)
         * @return 没有返回值
         */
        get: function(options) {
            options.httpMethod = 'GET';
            this.sendRequest(options);
        },
        create: function(options) {
            options.data = options.data || {};
            options.httpMethod = 'POST';
            this.sendRequest(options);
        },
        update: function(options) {
            options.data = options.data || {};
            options.httpMethod   = 'POST';
            options.data._method = 'PUT'; // SpringMvc HiddenHttpMethodFilter 的 PUT 请求
            this.sendRequest(options);
        },
        remove: function(options) {
            options.data = options.data || {};
            options.httpMethod   = 'POST';
            options.data._method = 'DELETE'; // SpringMvc HiddenHttpMethodFilter 的 DELETE 请求
            this.sendRequest(options);
        },
        // 阻塞请求
        syncGet: function(options) {
            options.async = false;
            this.get(options);
        },
        syncCreate: function(options) {
            options.async = false;
            this.create(options);
        },
        syncUpdate: function(options) {
            options.async = false;
            this.update(options);
        },
        syncRemove: function(options) {
            options.async = false;
            this.remove(options);
        },

        /**
         * 执行 Ajax 请求，不推荐直接调用这个方法.
         *
         * @param {Json} options 有以下几个选项:
         *               {String}   url        请求的 URL        (必选)
         *               {String}   httpMethod 请求的方式，有 GET, PUT, POST, DELETE (必选)
         *               {Json}     pathVariables URL 中的变量      (可选)
         *               {Json}     data       请求的参数        (可选)
         *               {Boolean}  async      默认为异步方式     (可选)
         *               {Boolean}  jsonRequestBody 是否使用 application/json 的方式进行请求，默认为 false 不使用(可选)
         *               {Function} success    请求成功时的回调函数(可选)
         *               {Function} fail       请求失败时的回调函数(可选)
         *               {Function} complete   请求完成后的回调函数(可选)
         */
        sendRequest: function(options) {
            var self = this;

            // 默认设置
            var defaults = {
                data           : {},
                async          : true,
                jsonRequestBody: false,
                contentType    : 'application/x-www-form-urlencoded;charset=UTF-8',
                success        : function() {},
                fail           : function() {},
                complete       : function() {}
            };

            // 使用 jQuery.extend 合并用户传递的 options 和 defaults
            var settings = $.extend(true, {}, defaults, options);

            // 使用 application/json 的方式进行请求时，需要处理相关参数
            if (settings.jsonRequestBody) {
                if (settings.data._method === 'PUT') {
                    settings.httpMethod = 'PUT';
                } else if (settings.data._method === 'DELETE') {
                    settings.httpMethod = 'DELETE';
                }

                delete settings.data._method; // 没必要传递一个无用的参数
                settings.contentType = 'application/json;charset=UTF-8';

                // 非 GET 时 json 对象需要序列化
                if (settings.data.httpMethod !== 'GET') {
                    settings.data = JSON.stringify(settings.data);
                }
            }

            // 替换 url 中的变量，例如 /rest/users/{id}, 其中 {id} 为要被 settings.pathVariables.id 替换的部分
            if (settings.pathVariables) {
                settings.url = settings.url.replace(/\{\{|\}\}|\{(\w+)\}/g, function(m, n) {
                    // m 是正则中捕捉的组 $0，n 是 $1，function($0, $1, $2, ...)
                    if (m == '{{') { return '{'; }
                    if (m == '}}') { return '}'; }
                    return settings.pathVariables[n];
                });
            }

            // 执行 AJAX 请求
            $.ajax({
                url        : settings.url,
                data       : settings.data,
                async      : settings.async,
                type       : settings.httpMethod,
                dataType   : 'json', // 服务器的响应使用 JSON 格式
                contentType: settings.contentType,
                // 服务器抛异常时，有时 Windows 的 Tomcat 环境下竟然取不到 header X-Requested-With, Mac 下没问题，
                // 正常请求时都是好的，手动添加 X-Requested-With 为 XMLHttpRequest 后所有环境下正常和异常时都能取到了
                headers: {'X-Requested-With': 'XMLHttpRequest'}
            })
            .done(function(data, textStatus, jqXHR) {
                settings.success(data, textStatus, jqXHR);
            })
            .fail(function(jqXHR, textStatus, failThrown) {
                // data|jqXHR, textStatus, jqXHR|failThrown
                const status = jqXHR.status;

                if (401 == status) {
                    alert('401: Token 无效');
                } else if (403 == status) {
                    alert('403: 权限不够');
                } else if (404 == status) {
                    alert('404: URL 不存在');
                } else if (500 == status) {
                    // 发生 500 错误时服务器抛出异常，在控制台打印出异常信息
                    console.error(jqXHR.responseJSON.data);
                    alert(`500: 发生异常，${jqXHR.responseJSON.message}\n\n详细错误信息请查看控制台输出 (Chrome 按下快捷键 F12)`);
                } else if (502 == status) {
                    // 发生 502 错误时，Tomcat Web 服务器不可访问，一般有 2 个原因
                    // 1. Nginx 配置出错
                    // 2. Tomcat 的 Web 服务没启动或者不接收请求
                    alert('502: 服务不可访问');
                } else if (504 == status) {
                    alert('504: Gateway Timeout\n\n' + jqXHR.responseText);
                }

                settings.fail(jqXHR, textStatus, failThrown);
            })
            .always(function() {
                settings.complete();
            });
        }
    };

    /**
     * 执行 Jsonp 请求，服务器端访问回调函数名使用 key 为 'callback'
     *
     * @param  {String}   url      请求的 URL
     * @param  {Function} callback 请求成功的回调函数，参数为服务器端返回的结果
     * @return 无返回值
     */
    $.jsonp = function(url, callback) {
        $.ajax({
            url     : url,
            type    : 'GET',
            dataType: 'jsonp',
            jsonp   : 'callback',
            success : function(data) {
                callback && callback(data);
            }
        });
    };
})(jQuery);
