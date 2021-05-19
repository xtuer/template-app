!function () {
    function doneEach() {
        // 0. 删除存在的 doc
        // 1. 查询所有 h2
        // 2. 计算页面的 baseUrl (去掉 ? 后面的部分)
        // 3. 生成 doc 的 innerHTML
        // 4. 创建 doc element 添加到 body 中

        // [1] 删除存在的 doc
        const prevDoc = document.querySelector('.docx');
        if (prevDoc) {
            prevDoc.remove();
        }

        // [1] 查询所有 h2
        const hs = document.querySelectorAll('h2');
        if (hs.length === 0) {
            return;
        }

        // [2] 计算页面的 baseUrl (去掉 ? 后面的部分)
        let baseUrl = window.location.href;
        let pos = baseUrl.indexOf('?');
        if (pos === -1) {
            pos = baseUrl.length
        }
        baseUrl = baseUrl.substring(0, pos);

        // [3] 生成 doc 的 innerHTML
        let doc = '<ul>';
        for (let h of hs) {
            let target = encodeURIComponent(h.id);
            let href = `${baseUrl}?id=${target}`;
            doc += `<li><a href="${href}">${h.innerText}</a></li>`;
        }
        doc += '</ul>';

        // [4] 创建 doc element 添加到 #main 中
        var div = document.createElement("div");
        div.classList.add("docx");
        div.innerHTML = doc;
        const body = document.querySelector('body');
        body.appendChild(div);
    }

    window.$docsify.plugins = [].concat(function (o) {
        o.doneEach(doneEach);
    }, window.$docsify.plugins);
}();
