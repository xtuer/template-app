<template>
    <div id="app">
        <router-view/>
    </div>
</template>

<style lang="scss">
body, html, #app {
    min-width : 960px;
    width     : 100%;
    height    : 100%;
    padding   : 0;
    margin    : 0;
    background: white;
    font-family: Helvetica, "Microsoft YaHei", Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
}
</style>
