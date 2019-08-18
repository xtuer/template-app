<template>
<div class="about">
    <h1>This is an about page</h1>
    <FileUploader ref="fileUploader" image upload-button @on-success="fileUploaded" />
</div>
</template>

<script>
import FileUploader from '@/components/FileUploader';
import UserDao from '@/../public/static/js/dao/UserDao.js';

export default {
    components: {
        FileUploader
    },
    data() {
        return {

        };
    },
    mounted() {
        // UserDao.findCurrentUser(user => {
        //     console.log(user);
        // });
    },
    methods: {
        fileUploaded(file) {
            console.log(JSON.stringify(file));
        }
    }
};
</script>

<style lang="scss">
.about {
    margin: 20px;
    padding: 20px;
    border: 1px dashed $primaryColor; // 使用 variables.css 中定义的变量
}
</style>
