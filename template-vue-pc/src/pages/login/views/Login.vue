<!-- 登录页面 -->
<template>
    <div class="login">
        <div class="wrap-login100">
            <!-- 标题 -->
            <div class="login100-form-title" style="background-image: url(/static-p/img/login-title.jpg);">
                <span class="login100-form-title-1">
                    Sign In
                </span>
            </div>

            <div class="login100-form">
                <!-- 机构 -->
                <div class="wrap-input100 m-b-26">
                    <span class="label-input100">机构</span>
                    <select v-model="orgId">
                        <option v-for="org in orgs" :key="org.orgId" :value="org.orgId">
                            {{ org.name }}
                        </option>
                    </select>
                    <span class="focus-input100"></span>
                </div>

                <!-- 账号 -->
                <div class="wrap-input100 m-b-26">
                    <span class="label-input100">账号</span>
                    <input v-model="username" class="input100" type="text" placeholder="Enter Username">
                    <span class="focus-input100"></span>
                </div>

                <!-- 密码 -->
                <div class="wrap-input100 m-b-50">
                    <span class="label-input100">密码</span>
                    <input v-model="password" class="input100" type="password" placeholder="Enter password">
                    <span class="focus-input100"></span>
                </div>

                <!-- 登录按钮 -->
                <div class="container-login100-form-btn flex-c-m">
                    <button class="login100-form-btn" @click="login">登录</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import Login from './Login';

export default {
    mixins: [Login]
};
</script>

<style lang="scss">
.login {
    width: 100%;
    min-height: 100vh;
    display: -webkit-box;
    display: -webkit-flex;
    display: -moz-box;
    display: -ms-flexbox;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    padding: 15px;
    background: #ebeeef;

    select {
        width: 100%;
        height: 45px;
        font-size: 15px;
        color: #1b3815;
        line-height: 1.2;
        border: 1px solid transparent;
        outline: none;

        // 隐藏 select 右边的箭头
        -webkit-appearance: none;
        -moz-appearance: none;
        appearance: none;
        text-indent: 1px;
        text-overflow: '';
    }

    input {
        outline: none;
        border: none;
    }

    input[type="number"] {
        -moz-appearance: textfield;
        appearance: none;
        -webkit-appearance: none;
    }

    input[type="number"]::-webkit-outer-spin-button,
    input[type="number"]::-webkit-inner-spin-button {
        -webkit-appearance: none;
        appearance: none;
    }

    textarea {
        outline: none;
        border: none;
    }

    textarea:focus,
    input:focus {
        border-color: transparent !important;
    }

    input:focus::-webkit-input-placeholder {
        color: transparent;
    }

    input:focus:-moz-placeholder {
        color: transparent;
    }

    input:focus::-moz-placeholder {
        color: transparent;
    }

    input:focus:-ms-input-placeholder {
        color: transparent;
    }
    input:-webkit-autofill {
        background-color: white !important;
        box-shadow: 0 0 0px 1000px white inset;
    }

    textarea:focus::-webkit-input-placeholder {
        color: transparent;
    }

    textarea:focus:-moz-placeholder {
        color: transparent;
    }

    textarea:focus::-moz-placeholder {
        color: transparent;
    }

    textarea:focus:-ms-input-placeholder {
        color: transparent;
    }

    input::-webkit-input-placeholder {
        color: #999999;
    }

    input:-moz-placeholder {
        color: #999999;
    }

    input::-moz-placeholder {
        color: #999999;
    }

    input:-ms-input-placeholder {
        color: #999999;
    }

    textarea::-webkit-input-placeholder {
        color: #999999;
    }

    textarea:-moz-placeholder {
        color: #999999;
    }

    textarea::-moz-placeholder {
        color: #999999;
    }

    textarea:-ms-input-placeholder {
        color: #999999;
    }

    label {
        display: block;
        margin: 0;
    }

    button {
        outline: none !important;
        border: none;
        background: transparent;
    }

    button:hover {
        cursor: pointer;
    }

    .wrap-login100 {
        width: 670px;
        background: #fff;
        border-radius: 10px;
        overflow: hidden;
        position: relative;
    }

    .focus-input100 {
        position: absolute;
        display: block;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        pointer-events: none;
    }

    input.input100 {
        height: 45px;
    }

    .input100:focus+.focus-input100::before {
        width: 100%;
    }

    .has-val.input100+.focus-input100::before {
        width: 100%;
    }

    .focus-input100::before {
        content: "";
        display: block;
        position: absolute;
        bottom: -1px;
        left: 0;
        width: 0;
        height: 1px;

        -webkit-transition: all 0.6s;
        -o-transition: all 0.6s;
        -moz-transition: all 0.6s;
        transition: all 0.6s;

        background: #57b846;
    }

    .login100-form-title {
        width: 100%;
        position: relative;
        z-index: 1;
        display: -webkit-box;
        display: -webkit-flex;
        display: -moz-box;
        display: -ms-flexbox;
        display: flex;
        flex-wrap: wrap;
        flex-direction: column;
        align-items: center;

        background-repeat: no-repeat;
        background-size: cover;
        background-position: center;

        padding: 70px 15px 74px 15px;
    }

    .login100-form-title-1 {
        font-size: 30px;
        color: #fff;
        text-transform: uppercase;
        line-height: 1.2;
        text-align: center;
    }

    .login100-form-title::before {
        content: "";
        display: block;
        position: absolute;
        z-index: -1;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        background-color: rgba(54, 84, 99, 0.7);
    }


    .login100-form {
        width: 100%;
        display: -webkit-box;
        display: -webkit-flex;
        display: -moz-box;
        display: -ms-flexbox;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        padding: 43px 88px 50px 150px;
    }

    .wrap-input100 {
        width: 100%;
        position: relative;
        border-bottom: 1px solid #b2b2b2;
    }

    .label-input100 {
        font-size: 15px;
        color: #808080;
        line-height: 1.2;
        text-align: right;

        position: absolute;
        top: 14px;
        left: -105px;
        width: 80px;

    }

    .input100 {
        font-size: 15px;
        color: #555555;
        line-height: 1.2;

        display: block;
        width: 100%;
        background: transparent;
        padding: 0 5px;
    }

    .container-login100-form-btn {
        width: 100%;
        display: -webkit-box;
        display: -webkit-flex;
        display: -moz-box;
        display: -ms-flexbox;
        display: flex;
        flex-wrap: wrap;
    }

    .login100-form-btn {
        display: -webkit-box;
        display: -webkit-flex;
        display: -moz-box;
        display: -ms-flexbox;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 20px;
        min-width: 160px;
        height: 50px;
        background-color: #2d8cf0;
        border-radius: 25px;

        font-size: 16px;
        color: #fff;
        line-height: 1.2;

        -webkit-transition: all 0.4s;
        -o-transition: all 0.4s;
        -moz-transition: all 0.4s;
        transition: all 0.4s;
    }

    .login100-form-btn:hover {
        background-color: #333333;
    }
}
</style>
