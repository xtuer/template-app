<template>
    <Scroll id="app">
        <router-view/>
    </Scroll>
</template>

<style lang="scss">
html, body, #app {
    min-width : 960px;
    width     : 100%;
    height    : 100%;
    padding   : 0;
    margin    : 0;
    background: white;
    font-family: Helvetica, "Microsoft YaHei", Arial, sans-serif;
    -webkit-font-smoothing : antialiased;
    -moz-osx-font-smoothing: grayscale;
}

/* 解决垂直滚动条出现时占据空间显示水平滚动条 */
:root {
    overflow-y: auto;
    overflow-x: hidden;
}
</style>
