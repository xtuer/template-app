<!-- 滚动相关例子 -->
<template>
    <div class="scroll-demo">
        <Affix :offset-top="20"><FileUpload/></Affix>

        <p>1</p>
        <p>2</p>
        <p>3</p>
    </div>
</template>

<script>
import FileUpload from '@/components/FileUpload.vue';

export default {
    components: { FileUpload },
    mounted() {
        window.addEventListener('scroll', this.fixAffix, true);
    },
    beforeDestroy() {
        window.removeEventListener('scroll', this.fixAffix, true);
    },
    methods: {
        // 解决 Affix 不生效
        fixAffix() {
            if (document.createEvent) {
                let event = document.createEvent('HTMLEvents');
                event.initEvent('resize', true, true);
                window.dispatchEvent(event);
            } else if (document.createEventObject) {
                window.fireEvent('onresize');
            }
        },
    }
};
</script>

<style lang="scss">
.scroll-demo {
    p {
        @include alignCenter;
        font-size: 60px;
        height: 500px;
        margin-top: 20px;
        background-image: linear-gradient(to top, #cfd9df 0%, #e2ebf0 100%);
    }
}
</style>
