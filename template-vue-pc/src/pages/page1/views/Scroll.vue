<!-- 滚动相关例子 -->
<template>
    <div class="scroll-demo">
        <Affix :offset-top="20"><FileUpload/></Affix>

        <!-- Leader Lines -->
        <div class="connections">
            <div v-for="item in items" :id="item" :key="item" class="item">{{ item }}</div>
        </div>

        <p>1</p>
        <p>2</p>
        <p>3</p>
    </div>
</template>

<script>
import FileUpload from '@/components/FileUpload.vue';

export default {
    components: { FileUpload },
    data() {
        return {
            items: ['One', 'Two', 'Three', 'Four', 'Five', 'Six'],
            lines: [],
        };
    },
    mounted() {
        window.addEventListener('scroll', this.fixAffix, true);

        Utils.loadJs('/static-p/lib/leader-line.min.js').then(() => {
            this.buildLines();
        });
    },
    beforeDestroy() {
        window.removeEventListener('scroll', this.fixAffix, true);

        // 销毁连线
        this.lines.forEach(line => {
            line.remove();
        });
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
        // 创建连线
        buildLines() {
            for (let i = 0; i < this.items.length-1; i++) {
                var startElement = document.getElementById(this.items[i]);
                var endElement   = document.getElementById(this.items[i+1]);

                this.lines.push(new LeaderLine(startElement, endElement, { color: '#aaa', size: 2 }));
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
        // width: 2000px;
    }

    .connections {
        padding: 20px;
        border: 1px solid #eee;
        margin-top: 20px;

        .item {
            display: inline-block;
            border: 1px solid #aaa;
            padding: 20px;
            margin: 20px;
            width: 150px;
            text-align: center;
        }
    }
}
</style>
