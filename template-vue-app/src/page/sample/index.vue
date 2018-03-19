<template>
    <div id="app" class="app">
        <router-view/>
    </div>
</template>

<script>
    export default {
        created() {
            Utils.Notice = this.$Notice; // 给 Utils 注入 iView 的 Notice
        }
    };
</script>

<style lang="scss">
.app{
    width: 100%;
    height: 100%;
}
</style>
