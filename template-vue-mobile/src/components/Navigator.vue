<!--
功能: 导航组件

属性:
title : 标题
height: 高度

案例:
<Navigator title="在线课程"/>
<Navigator title="在线课程" :height="100"/>
<Navigator title="在线课程" :route="{ name: 'home' }"/>
-->
<template>
    <div class="navigator" :style="style">
        <van-nav-bar :title="title" left-arrow @click-left="to"/>
    </div>
</template>

<script>
export default {
    props: {
        title : { type: String, required: true }, // 标题
        height: { type: Number, default: -1    }, // 高度
        route : { type: String, default: null  }, // 路由
    },
    computed: {
        style() {
            return {
                height: this.custom ? this.height + 'px' : 'auto',
                backgroundImage : this.custom ? 'url(/static-m/img/nav-bg.png)' : '',
                backgroundRepeat: 'no-repeat',
                backgroundSize  : 'cover',
                backgroundColor : this.custom ? '' : '#469afa',
                backgroundPosition: this.custom ? 'bottom' : '',
            };
        },
        // 高度不为 -1 表示自定义
        custom() {
            return this.height !== -1;
        },
    },
    methods: {
        to() {
            if (this.route) {
                this.$router.push(this.route);
            } else {
                this.goBack();
            }
        }
    }
};
</script>

<style lang="scss">
.navigator {
    .van-nav-bar {
        background: transparent;

        .van-nav-bar__left {
            top: 50%;
            transform: translateY(-50%);
            // margin-top: -2px;

            display: flex;
            justify-content: center;
            align-items: center;
        }

        .van-icon-arrow-left, .van-nav-bar__title {
            color: white;
            font-size: 19px;
            font-weight: bold;
        }

        .van-nav-bar__title {
            max-width: 80%;
        }

        &.van-hairline--bottom::after {
            display: none;
        }
    }
}
</style>
