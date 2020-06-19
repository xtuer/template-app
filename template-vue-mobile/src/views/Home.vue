<template>
    <div class="home">
        <Navigator title="首页"/>
        {{ $store.state.user }}
    </div>
</template>

<script>
export default {
    data() {
        return {};
    },
    mounted() {},
    methods: {}
};
</script>

<style lang="scss">
.home {
    width: 100%;
    height: 100%;
}
</style>
