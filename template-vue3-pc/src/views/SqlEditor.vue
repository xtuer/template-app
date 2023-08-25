<template>
    <div ref="editorDom" class="sql-editor"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as monaco from 'monaco-editor';
import { useDatabaseObjectStore } from '@/stores/DatabaseObjectStore';
import { SqlCompletionProvider } from './SqlCompletionProvider';

const editorDom = ref<any>();
let editor: monaco.editor.IStandaloneCodeEditor;
let completionProvider: monaco.IDisposable;
let sqlCompletionProvider: SqlCompletionProvider;
const dbStore = useDatabaseObjectStore();

onMounted(() => {
    initializeEditor();
});

onUnmounted(() => {
    editor.dispose();
    completionProvider.dispose()
});

/**
 * 初始化编辑器。
 */
async function initializeEditor(): Promise<void> {
    // FIXME: 只是为了测试时获取配置。
    await dbStore.findDatabaseMetadataConfigs();

    (self as any).MonacoEnvironment = {
        getWorker: function (workerId: string, label: string) {
            const getWorkerModule = (moduleUrl: string, label: string) => {
                return new Worker((self as any).MonacoEnvironment.getWorkerUrl(moduleUrl), {
                    name: label,
                    type: 'module'
                });
            };

            return getWorkerModule('/monaco-editor/esm/vs/editor/editor.worker?worker', label);
        }
    };

    // 创建编辑器。
    editor = monaco.editor.create(editorDom.value, {
        value: "select * from user ",
        language: 'sql',
        scrollBeyondLastLine: false,
        minimap: { enabled: false },
        // quickSuggestions: true,
    });

    // 注册自动补全工具。
    sqlCompletionProvider = new SqlCompletionProvider(editor);
    sqlCompletionProvider.setup('MYSQL', 1, 'test', null);
    // sqlCompletionProvider.setup('POSTGRES', 3, 'postgres', 'biao');
    completionProvider = monaco.languages.registerCompletionItemProvider("sql", sqlCompletionProvider);

    // 内容变化时回调。
    editor.onDidChangeModelContent(() => {
        // console.log(editor.getValue());
    });
}
</script>

<style>
.sql-editor {
    width: 100vw;
    height: 100vh;
}
</style>
