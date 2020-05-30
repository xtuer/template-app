<template>
    <div class="about">
        <Richtext v-for="sn in 1" :key="sn" :min-height="200" inline/>
    </div>
</template>

<script>
import Richtext from '@/components/Richtext.vue';

export default {
    components: { Richtext },
    data() {
        return {};
    },
    mounted() {
        console.log(JSON.stringify(new Date()));

        Utils.loadJs('/static-p/lib/jquery.min.js?t=123').then(() => {
            console.log('jquery-1');
            Utils.loadJs('/static-p/lib/jquery.min.js?t=123').then(() => {
                console.log('jquery-2');
            });
        });

        const ids = [1, 2, 3];
        Rest.get({ url: '/api/demo/array', data: { ids } }).then(result => {
            console.log(result);
        });
    },
    methods: {}
};
</script>

<style lang="scss">

</style>
