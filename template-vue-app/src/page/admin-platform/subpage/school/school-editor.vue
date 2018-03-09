<template>
    <div>
        <Button type="primary" @click="saveSchool">保存</Button>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                name: '学校管理'
            };
        },
        methods: {
            // 保存学校
            saveSchool() {
                this.$router.go(-1);
            }
        }
    };
</script>

<style>
.app-main{
    width: 100%;
    height: 100%;
}
</style>
