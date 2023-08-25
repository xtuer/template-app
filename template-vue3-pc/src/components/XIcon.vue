<!--
自定义图标组件。

参数:
name: svg filename。
gray: 为 true 则灰度化显示，为 false 则不变。

示例:
<XIcon name="<svg-filename>" />
<XIcon name="dashboard_big-sql-upload" />
<XIcon name="dashboard_big-sql-upload" gray />
-->
<template>
    <component :is="icon" class="x-icon" :class="{ gray: gray }"/>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

const props = defineProps<{
    name: string,
    gray?: boolean,
}>();

const icon = defineAsyncComponent(() => import(`@/assets/icons/${props.name}.svg`));
</script>

<style lang="scss">
.x-icon {
    &.gray {
        filter: grayscale(100%);
        opacity: 0.8;
    }
}
</style>
