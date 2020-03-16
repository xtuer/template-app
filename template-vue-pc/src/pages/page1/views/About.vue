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
    },
    methods: {}
};
</script>

<style lang="scss">

</style>
