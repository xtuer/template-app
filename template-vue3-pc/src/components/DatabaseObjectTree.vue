<!-- eslint-disable @typescript-eslint/no-unused-vars -->
<!--
数据库对象树。

参数: 无。
事件: 无。
示例: <DatabaseObjectTree />

实现提示:
- nodeRender: 渲染节点的样式。
- doLoadNodes: 根据节点的类型分类加载和创建节点，是核心逻辑的入口。
-->
<template>
    <!-- 对象树 -->
    <Vtree class="database-object-tree" ref="tree" :load="loadNodes" :render="nodeRender" :renderNodeAmount=30 @node-right-click="onNodeRightClick"/>

    <!-- 右键菜单 -->
    <ContextMenu ref="contextMenu">
        <ContextMenuItem v-for="item in contextMenuItems" :key="item.id" :disabled="!item.enabled" @click="onContextMenuItemClicked(item)">
            {{ item.label }}
        </ContextMenuItem>
    </ContextMenu>
</template>

<script setup lang="tsx">
import { ref, onMounted } from 'vue';
import Vtree from '@wsfe/vue-tree';
import '@wsfe/vue-tree/style.css';

import XIcon from '@/components/XIcon.vue';
import { NodeType } from '@/static/types/DatabaseObjectTreeTypes';
import type { NodeData, DatabaseConfig, DatabaseInfo } from '@/static/types/DatabaseObjectTreeTypes';
import type { DatabaseType, TableColumns, TableCoordinator } from '@/static/types';
import { NodeCreator, NodeHelper } from './DatabaseObjectTreeHelper';

import { Contextmenu as ContextMenu, ContextmenuItem as ContextMenuItem } from 'v-contextmenu';
import 'v-contextmenu/dist/themes/default.css';
import useDatabaseObjectTreeContextMenu from './DatabaseObjectTreeContextMenu';

import { useDatabaseObjectStore } from '@/stores/DatabaseObjectStore';

// 表示 Vue-Tree 树节点类型。
type TreeNode = NodeData;

// 创建树的 ref，用于调用其 API。
const tree = ref<any>(null);

// 右键菜单。
const contextMenu = ref<any>(null);
const { contextMenuItems, showContextMenu, onContextMenuItemClicked } = useDatabaseObjectTreeContextMenu({ tree, contextMenu });

// 数据库 Store。
const dbStore = useDatabaseObjectStore();

// DOM 挂载好后请求可使用的数据库类型。
onMounted(() => {
    dbStore.findDatabaseMetadataConfigs().then(configs => {
        // 第一层节点: 数据库类型。
        const treeData = [];
        for (const config of configs) {
            treeData.push(NodeCreator.createDatabaseTypeNode(config.type, config.label))
        }

        // 设置树的数据，生成第一层节点。
        tree.value.setData(treeData);
    });
})

// Node render: 使用 JSX 定义节点的样式。
function nodeRender(node: TreeNode): any {
    // 节点的图标
    const style = NodeHelper.findNodeStyle(node);

    // 节点的 CSS 类名。
    const connected = (NodeType.DATABASE_INSTANCE === node.type && node.connected); // 数据库实例的连接状态。
    const classes = ['db-node', style.className, connected ? 'node-db-instance-connected' : ''];

    if (NodeType.TABLE_COLUMN === node.type) {
        // 列需要显示类型。
        return (
            <div class={ classes }>
                <XIcon name={ style.icon } />
                { node.title }
                <span class="data-type">{ node.dataType }</span>
            </div>
        );
    } else {
        // 其他情况。
        return (
            <div class={ classes }>
                <XIcon name={ style.icon } />
                { node.title }
            </div>
        );
    }
}

// 右键点击节点时显示右键菜单。
function onNodeRightClick(node: TreeNode, event: MouseEvent) {
    const dbType: DatabaseType = NodeHelper.findDatabaseType(node);
    const config: DatabaseConfig = dbStore.findDatabaseConfig(dbType)!;
    showContextMenu(config, node, event)
}

// 对 doLoadNodes 包装了一下，因为 Vue-Tree 会把 loadNodes() toString 后绑定到 DOM 上，
// 如果不简单的包装一下，DOM 的内容就会变得很长。
function loadNodes(node: TreeNode, resolve: Function, reject: Function) {
    doLoadNodes(node, resolve, reject);
}

// 异步加载节点数据 (点击箭头展开或者单击节点)。
async function doLoadNodes(node: TreeNode, resolve: Function, reject: Function): Promise<void> {
    if (!node) {
        reject();
        return;
    }

    try {
        // 查询数据库的配置。
        const dbInfo: DatabaseInfo = NodeHelper.findDatabaseInfo(node);
        const dbType: DatabaseType = NodeHelper.findDatabaseType(node);
        const config: DatabaseConfig | null = dbStore.findDatabaseConfig(dbType);

        if (!config) {
            console.log(`数据库没有配置: ${node.databaseType}`);
            reject();
            return;
        }

        // 点击某个节点后，加载这个节点的下一级内容。
        if (NodeType.DATABASE_TYPE === node.type) {
            // 点击数据库类型节点，加载数据库实例。
            await loadDatabaseInstances(config, node, resolve, reject);
        } else if (NodeType.DATABASE_INSTANCE === node.type) {
            // 点击数据库实例:
            // - 如果使用 catalog 则只请求 catalog。
            // - 如果只使用 schema 则只请求 schema。
            if (NodeHelper.onlyUseCatalog(config) || NodeHelper.useBothCatalogAndSchema(config)) {
                await loadCatalogs(config, node, resolve, reject, dbInfo);
            }
            if (NodeHelper.onlyUseSchema(config)) {
                await loadSchemas(config, node, resolve, reject, dbInfo);
            }

            // 注意: 标记数据库连接成功。
            node.connected = true;
        } else if (NodeType.CATALOG === node.type && NodeHelper.onlyUseCatalog(config)) {
            // 创建 catalog 或 schema 下的分组目录：表目录、视图目录、存储过程和函数目录等。
            createFoldersUnderCatalogOrSchema(config, node, resolve);
        } else if (NodeType.CATALOG === node.type && NodeHelper.useBothCatalogAndSchema(config)) {
            // 加载 schema。
            await loadSchemas(config, node, resolve, reject, dbInfo);
        } else if (NodeType.SCHEMA === node.type) {
            // 创建 catalog 或 schema 下的分组目录：表目录、视图目录、存储过程和函数目录等。
            createFoldersUnderCatalogOrSchema(config, node, resolve);
        } else if (NodeType.TABLE_FOLDER === node.type) {
            // 加载表。
            await loadTables(config, node, resolve, reject, dbInfo);
        } else if (NodeType.VIEW_FOLDER === node.type) {
            // 加载视图。
            await loadViews(config, node, resolve, reject, dbInfo);
        } else if (NodeType.TABLE === node.type) {
            // 创建表下面的目录: 列、索引。
            createFoldersUnderTable(config, node, resolve);
        } else if (NodeType.VIEW === node.type) {
            // 创建视图下面的目录: 列。
            createFoldersUnderView(config, node, resolve);
        } else if (NodeType.TABLE_COLUMN_FOLDER === node.type) {
            // 加载表的列。
            await loadTableColumns(config, node, resolve, reject, dbInfo);
        } else if (NodeType.VIEW_COLUMN_FOLDER === node.type) {
            // 加载视图的列 (与表的列一样)。
            await loadViewColumns(config, node, resolve, reject, dbInfo);
        }
    } catch (err) {
        console.error(err);
        reject();
    }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                        创建节点                                                    //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 加载数据库实例。
async function loadDatabaseInstances(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function): Promise<void> {
    /*
    逻辑:
    1. 从服务器加载指定类型的数据库实例。
    2. 如果数据为空则 reject()，再次点击还会触发加载。
    3. 创建节点。
    4. 调用 resolve(nodes) 把节点添加到树中。

    提示: 其他节点的加载逻辑也一样，不再重复注释
    */

    // [1] 从服务器加载指定类型的数据库实例。
    const instances = await dbStore.findDatabaseInstances(config.type);

    // [2] 如果数据为空则 reject()，再次点击还会触发加载。
    // if (instances.length === 0) {
    //     reject();
    //     return;
    // }

    // [3] 创建节点。
    const dbNodes = [];
    for (const ins of instances) {
        dbNodes.push(NodeCreator.createDatabaseInstanceNode(config.type, ins.dbid, `${config.label}-${ins.dbid}`));
    }

    // [4] 调用 resolve(nodes) 把节点添加到树中。
    resolve(dbNodes)
}

// 加载 catalogs。
async function loadCatalogs(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function, dbInfo: DatabaseInfo): Promise<void> {
    const catalogNames: string[] = (await dbStore.findCatalogs(config.type, dbInfo.dbid)).map(c => c.name);

    const catalogNodes: NodeData[] = [];
    for (const name of catalogNames) {
        catalogNodes.push(NodeCreator.createCatalogNode(name));
    }
    resolve(catalogNodes);
}

// 加载 schemas。
async function loadSchemas(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function, dbInfo: DatabaseInfo): Promise<void> {
    const schemaNames: string[] = (await dbStore.findSchemas(config.type, dbInfo.dbid, dbInfo.catalog)).map(s => s.name);

    const schemaNodes: NodeData[] = [];
    for (const name of schemaNames) {
        schemaNodes.push(NodeCreator.createSchemaNode(name));
    }
    resolve(schemaNodes);
}

// 加载表。
async function loadTables(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function, dbInfo: DatabaseInfo): Promise<void> {
    const tables = await dbStore.findTables(config.type, dbInfo.dbid, dbInfo.catalog, dbInfo.schema);
    const tableNames: string[] = tables.map(table => table.name);

    const tableNodes: NodeData[] = [];
    for (const name of tableNames) {
        tableNodes.push(NodeCreator.createTableNode(name));
    }
    resolve(tableNodes);
}

// 加载视图。
async function loadViews(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function, dbInfo: DatabaseInfo): Promise<void> {
    const views = await dbStore.findViews(config.type, dbInfo.dbid, dbInfo.catalog, dbInfo.schema);
    const viewNames: string[] = views.map(table => table.name);

    const viewNodes: NodeData[] = [];
    for (const name of viewNames) {
        viewNodes.push(NodeCreator.createViewNode(name));
    }
    resolve(viewNodes);
}

// 加载表的列。
async function loadTableColumns(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function, dbInfo: DatabaseInfo): Promise<void> {
    const tableCoordinator: TableCoordinator = { catalog: dbInfo.catalog, schema: dbInfo.schema, table: dbInfo.table };
    const tableColumns: TableColumns | null = await dbStore.findTableColumns(config.type, dbInfo.dbid, tableCoordinator);

    if (!tableColumns || tableColumns.columns.length === 0) {
        return;
    }

    const columnNodes: NodeData[] = [];
    for (const col of tableColumns.columns) {
        columnNodes.push(NodeCreator.createTableColumnNode(col.name, col.typeName));
    }

    resolve(columnNodes);
}

// 加载视图的列。
async function loadViewColumns(config: DatabaseConfig, node: TreeNode, resolve: Function, reject: Function, dbInfo: DatabaseInfo): Promise<void> {
    const viewCoordinator: TableCoordinator = { catalog: dbInfo.catalog, schema: dbInfo.schema, table: dbInfo.view };
    const viewColumns: TableColumns | null = await dbStore.findTableColumns(config.type, dbInfo.dbid, viewCoordinator);

    if (!viewColumns || viewColumns.columns.length === 0) {
        return;
    }

    const columnNodes: NodeData[] = [];
    for (const col of viewColumns.columns) {
        columnNodes.push(NodeCreator.createTableColumnNode(col.name, col.typeName));
    }

    resolve(columnNodes);
}

// 创建 catalog 或 schema 下的分组目录：表目录、视图目录、存储过程和函数目录等。
function createFoldersUnderCatalogOrSchema(config: DatabaseConfig, node: TreeNode, resolve: Function): void {
    const folderNodes = [];

    folderNodes.push(NodeCreator.createTableFolderNode());
    folderNodes.push(NodeCreator.createViewFolderNode());

    if (config.useFunction) {
        folderNodes.push(NodeCreator.createFunctionFolderNode());
    }
    if (config.useProcedure) {
        folderNodes.push(NodeCreator.createProcedureFolderNode());
    }

    resolve(folderNodes);
}

// 创建表下面的目录: 列、索引。
function createFoldersUnderTable(config: DatabaseConfig, node: TreeNode, resolve: Function): void {
    const folderNodes = [];

    folderNodes.push(NodeCreator.createTableColumnFolderNode());

    resolve(folderNodes);
}

// 创建视图下面的目录: 列。
function createFoldersUnderView(config: DatabaseConfig, node: TreeNode, resolve: Function): void {
    const folderNodes = [];

    folderNodes.push(NodeCreator.createViewColumnFolderNode());

    resolve(folderNodes);
}
</script>

<style lang="scss">
.database-object-tree {
    /* 展开箭头 */
    .ctree-tree-node__expand:after {
        border-width: 4px;
    }
    /* 数据库实例 */
    .node-db-instance {
        color: #888;

        .x-icon {
            filter: grayscale(100%);
            opacity: 0.8;
        }
    }
    .node-db-instance-connected {
        color: #333;

        .x-icon {
            filter: none;
            opacity: 1.0;
        }
    }

    /* 节点的 title */
    .ctree-tree-node__title {
        margin-left: 0;
        padding-left: 0;
    }

    /* 节点 */
    .db-node {
        display: flex;
        align-items: center;

        svg {
            /* transform: scale(0.65);  图标大小，不能使用 width 和 height */
            margin-right: 4px;
        }
    }

    /* 表或者视图列的数据类型 */
    .data-type {
        color: #aaa;
        margin-left: 4px;
    }
}
</style>
./DatabaseObjectTreeHandler
./DatabaseObjectTreeHelper
