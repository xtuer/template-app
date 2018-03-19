## 什么时候请求数据

页面加载时，向服务器请求数据，一般会在函数 `created` 或者 `mounted` 中进行:

* created: 请求数据后，不需要用 JS 手动的修改 DOM，数据保存到 data 中即可
* mounted: 请求数据后，需要用 JS 修改 DOM，因为这个时候 el 才被新创建的 vm.$el 替换 (修改 DOM 需要在 nextTick 中进行)

常使用 AJAX 向服务器异步请求数据，所以在可能的情况下，越早越好，故能够在 created 中请求的话就尽量它里面进行。

## REF 的作用