<template>
    <div class="about">
        <FileUploader button/>
    </div>
</template>

<script>
import FileUploader from '@/components/FileUploader.vue';

export default {
    components: { FileUploader },
    data() {
        return {};
    },
    mounted() {
        this.$UserDao.patchUser({ id: 1, nickname: 'Bob' });
    },
};
</script>

<style lang="scss">

</style>
