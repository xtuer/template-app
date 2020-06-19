<template>
    <div class="demo">
        <van-cell-group>
            <van-field v-model="username" required clearable label="用户名" placeholder="请输入用户名" />
            <van-field v-model="password" type="password" label="密码" placeholder="请输入密码" required />
            <ul>
                <li v-for="file in files" :key="file">{{ file }}</li>
            </ul>
            <van-uploader :after-read="readyToUpload" accept="*/*">
                <van-button icon="photo" type="primary">上传文件</van-button>
            </van-uploader>
            <van-button type="primary" block @click="save">提交</van-button>
        </van-cell-group>
    </div>
</template>

<script>
export default {
    data() {
        return {
            username: '',
            password: '',
            files: ['介绍.doc']
        };
    },
    methods: {
        save() {
            this.$toast.success('保存成功');
        },
        readyToUpload(file) {
            let url = '/form/upload/temp/file';
            let fd  = new FormData();
            fd.append('file', file.file);

            Rest.upload(url, fd).then(result => {
                this.files.push(result.data.filename);
            });
        }
    }
};
</script>

<style lang="scss">
.demo {
    padding: 20px;

    button {
        margin-top: 10px;
    }
}
</style>
