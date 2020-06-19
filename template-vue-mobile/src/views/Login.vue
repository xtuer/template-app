<!-- 登录 -->
<template>
    <div class="login">
        <van-form @submit="login">
            <van-field
                    v-model="username"
                    label="账号:"
                    placeholder="账号"
                    :rules="[{ required: true, message: '请填写账号' }]">
            </van-field>
            <van-field
                    v-model="password"
                    type="password"
                    label="密码:"
                    placeholder="密码"
                    :rules="[{ required: true, message: '请填写密码' }]">
            </van-field>

            <div style="padding: 16px;">
                <van-button round block type="info" native-type="submit" :loading="loading" loading-text="登录">登录</van-button>
            </div>
        </van-form>
    </div>
</template>

<script>
import UserDao from '@/../public/static-m/js/dao/UserDao';

export default {
    data() {
        return {
            username: '',
            password: '',
            loading : false,
        };
    },
    methods: {
        // 登录
        login() {
            // 1. 登录
            // 2. 获取成功登录的用户信息
            // 3. 跳转到首页
            this.loading = true;
            UserDao.loginToken(this.username, this.password).then(() => {
                return UserDao.findCurrentUser();
            }).then(user => {
                this.$store.commit('setUser', user);
                this.$router.push('/');
                this.loading = false;
            }).catch(() => {
                this.loading = false;
            });
        },
    },
};
</script>

<style lang="scss">
.login {
    padding: 40px 0;

    .org {
        display: grid;
        justify-items: center;
        justify-content: center;
        align-items: center;
        grid-gap: 20px;
        margin-bottom: 40px;

        .org-name {
            font-size: 24px;
            font-weight: bold;
            text-shadow: 0px 0px 2px rgba(0, 0, 0, .5); // rgba(146, 150, 198, 0.88);
        }

        .van-image {
            background-color: rgba(0, 0, 0, 0.38);
            box-shadow: 0px 0px 20px 0px rgba(146, 150, 198, 0.8);
        }
    }
}
</style>
