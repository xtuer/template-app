<template>
    <main>
        <VTree ref="tree"/>
    </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import '@wsfe/vue-tree/style.css';
import VTree from '@wsfe/vue-tree';

const tree = ref<any>(null);

const treeData = [
    { id: 1, title: '1' },
    { id: 2, title: '2' },
]

onMounted(() => {
    tree.value.setData(treeData);
})

</script>
