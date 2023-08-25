<template>
    <div class="sql-parser">
        <!-- 输入 SQL -->
        <div class="left-column">
            <textarea v-model="sql" ref="textarea"></textarea>

            <div class="toolbar">
                <label>
                    <input v-model="showLineNumber" type="checkbox" id="line-number"/>行号
                </label>
                <button @click="insertCursorPlaceholder">插入标记</button>
                <button @click="generateAst">生成语法树</button>
            </div>

            <div><span class="label">光标标记:</span> <span>__cursor__</span></div>
            <div class="node-types"><span class="label">节点类型:</span><br/><code class="language-js">{{ types }}</code></div>

            <button @click="loadTestForSqlAdvisor">性能测试</button>
        </div>

        <!-- AST JSON -->
        <div class="right-column">
            <VueJsonPretty :data="astRoots" :showLineNumber="showLineNumber" showLength :showDoubleQuotes1="false" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import Prism from 'prismjs';
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';

import { SqlAst } from './SqlAst';
import { SqlAdvisor } from './SqlAdvisor';

// 测试的 SQL 语句: select * from user，不管 '\n;' 的特殊情况。
const SQL = `
SELECT schema.user.id_id__cursor__,
       u.name AS user_name,
       COUNT(DISTINCT o.id) AS total_orders,
       SUM(oi.quantity * p.price) AS total_spent

FROM users u
     INNER JOIN orders o ON u.id = o.user_id
     INNER JOIN order_items oi ON o.id = oi.order_id
     INNER JOIN products p ON oi.product_id = p.id

WHERE o.id IN (
      SELECT DISTINCT o.id
      FROM orders o
           INNER JOIN order_items oi ON o.id = oi.order
           INNER JOIN products p ON oi.product_id = p.id
      WHERE p.name = '特定产品名称'
            AND o.order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
)
GROUP BY u.id, u.name
ORDER BY total_spent DESC;
`;

// const sql = ref<string>(SQL);
// const sql = ref<string>('select * from user where u.id__cursor__ = 3');
// const sql = ref<string>('select * from user u, student__cursor__ as s, person where uid = 3');
// const sql = ref<string>('select * from schema.user__cursor__ u left join student s on s.id = u.id right join class on class.id = u.classid');
// const sql = ref<string>('select * from schema.user u left join student s on s.id = u.id right join class on class.id__cursor__ = u.classid where id = 3');
const sql = ref<string>('select * from user where id in (select id from student where id__cursor__>1)');
const types = ref<Set<string>>(new Set())
const astRoots = ref<any>([]);
const textarea = ref<HTMLTextAreaElement>();
const showLineNumber = ref<boolean>(false);

onMounted(() => {
    generateAst();
});

// 生成语法树。
async function generateAst(): Promise<void> {
    /*
     逻辑:
     1. 重置环境。
     2. 生成 SQL 的语法树。
     3. 给语法树节点生成 id，构建节点之间的父子关系: 这个语法树对象用于变成查找。
     4. 把语法树对象序列化为 JSON 字符串，并且去掉 parent 信息。
     5. 使用 VueJsonPretty 显示 JSON。
     6. 获取语法树节点的类型，用于分析。
     7. Prism 语法高亮生效。
     8. 查找光标所在节点。
     */

    // [1] 重置环境。
    reset();

    // 抽象语法树。
    // [2] 生成 SQL 的语法树。
    // [3] 给语法树节点生成 id，构建节点之间的父子关系: 这个语法树对象用于变成查找。
    const astHelper = new SqlAst(sql.value, 'postgresql'); // mysql, postgresql

    // [4] 把语法树对象序列化为 JSON 字符串，并且去掉 parent 信息。
    const astString = astHelper.toString();

    // [5] 使用 VueJsonPretty 显示 JSON。
    // 不能直接传 ast 对象，因为 children 持有 parent 引用，会导致递归栈溢出，需要把 ast 转为 JSON 字符串去掉 parent 信息，然后再把字符串转为 JSON 对象。
    astRoots.value = JSON.parse(astString);

    // [6] 获取语法树节点的类型，用于分析。
    const result = astString.matchAll(/"type": "(\w+)"/g); // "type": "keyword",
    for (let t of result) {
        types.value.add(t[1]);
    }

    // [7] Prism 语法高亮生效。
    await nextTick();
    Prism.highlightAll();

    // [8] 查找光标所在节点。
    // const cursorNode = astHelper.findCursorNode();
    // if (cursorNode) {
    //     console.log(`ID: ${cursorNode.id}, TEXT: ${cursorNode.text}, PARENT_ID:  ${cursorNode.parent!.id}`);

    //     const clause = astHelper.findNearestClause(cursorNode);
    //     console.log(clause.nameKw);
    //     console.log(astHelper.isDirectChildOfClause(cursorNode, 'FROM'));
    //     console.log(astHelper.findTablesInClause(clause));
    // }
    let advisor: SqlAdvisor = new SqlAdvisor({ wholeSql: sql.value, databaseType: 'postgresql' });
    console.log(advisor.advise());
}

/**
 * 重置环境。
 */
function reset(): void {
    astRoots.value = {};
    types.value.clear();
}

/**
 * 在 textarea 里光标处插入 __cursor__ 占位符。
 */
function insertCursorPlaceholder() {
    const [start, end] = [textarea.value?.selectionStart, textarea.value?.selectionEnd];

    if (start && end) {
        textarea.value?.setRangeText('__cursor__', start, end, 'select');
        sql.value = textarea.value?.value || '';
    }
}

/**
 * 性能测试。
 */
async function loadTestForSqlAdvisor(): Promise<void> {
    // nextTick() 后面的逻辑异步处理。
    await nextTick();

    console.time('解析 1000 条 SQL'); // 开始计时

    const sql = 'select * from user where id in (select id from student where id__cursor__>1)';
    for (let i = 0; i < 1000; i++) {
        new SqlAdvisor({ wholeSql: sql, databaseType: 'postgresql' }).advise();
    }

    console.timeEnd('解析 1000 条 SQL'); // 结束计时并在控制台输出执行时间
}
</script>

<style lang="scss">
.sql-parser {
    display: grid;
    grid-template-columns: 400px 1fr;
    row-gap: 10px;
    height: 100%;

    .left-column {
        padding: 10px;
        display: grid;
        grid-template-rows: 400px max-content max-content 1fr;
        row-gap: 10px;

        textarea {
            font-family: Monaco, monospace;
            font-size: 12px;
        }

        .toolbar {
            display: grid;
            grid-template-columns: max-content max-content 1fr;
            column-gap: 10px;
            align-items: center;

            label {
                display: flex;
                align-items: center;
                input {
                    margin-right: 4px;
                }
            }
        }

        .node-types {
            margin-top: 40px;
        }
    }

    .right-column {
        border-left: 1px solid #aaa;
        overflow: auto;

        pre[class*="language-"] {
            margin: 0;
            border-radius: 0;
        }

        .vjs-tree-node .vjs-indent-unit {
            width: 2em;
        }
        .vjs-value-string {
            color: #0e984b;
        }
    }

    .label {
        color: #888;
    }
}
</style>
./SqlAst./SqlAst
