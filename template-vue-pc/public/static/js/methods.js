/**
 * 下载 url 指定的文件
 *
 * @param  {String} url 要下载的 URL
 * @return 无返回值
 */
const download = function(url) {
    // 1. 如果 url 是文件仓库的 url，则替换为下载使用的 url
    // 2. 创建 form 表单
    // 3. 提交表单下载文件

    if (!url) {
        return;
    }

    // [1] 如果 url 是文件仓库的 url，则替换为下载使用的 url
    const finalUrl = url.replace('/file/repo', '/file/download');

    // [2] 创建 form 表单
    const form = document.createElement('form');
    form.method = 'GET';
    form.action = finalUrl;
    document.body.appendChild(form);

    // [3] 提交表单下载文件
    form.submit();
};

/**
 * 返回上一路由
 *
 * 案例: <Button @click="goBack()">返回</Button>
 *
 * @return 无返回值
 */
const goBack = function() {
    window.history.length > 1 ? this.$router.go(-1) : this.$router.push('/');
};

export default {
    download,
    goBack,
};
