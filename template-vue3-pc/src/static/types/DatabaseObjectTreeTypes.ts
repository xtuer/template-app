/**
 * 数据库对象树类型。
 */
/**
 * 节点类型。
 */

import { DATABASE_TYPES } from '@/static/types';
import type { DatabaseType } from '@/static/types';

/**
 * 数据库对象，catalog schema table view 等。
 *
 * 注意: 因为 Oracle 获取表名有的时候很慢，这种情况下 loadState 标记为加载中避免重复加载尤其重要。
 */
interface DatabaseObject {
    type     : 'root' | 'catalog' | 'schema' | 'table' | 'view' | 'column' | 'procedure' | 'function'; // 对象类型。
    name     : string;           // 对象名称。
    loadState: 0 | 1 | 2;        // 下级子对象的加载状态: 0 (初始化)，1 (加载中), 2 (加载完成)。
    children : DatabaseObject[]; // 下级子对象数组，例如 schema 的 children 为 table, view 等。
    typeName?: string;           // 列的数据类型名，例如 INT。
}

/**
 * 数据库配置。
 */
interface DatabaseConfig {
    type          : DatabaseType;
    label         : string;
    useCatalog    : boolean;
    useSchema     : boolean;
    useProcedure  : boolean;
    useFunction   : boolean;
}

/**
 * 节点所在分支的数据库信息。
 */
interface DatabaseInfo {
    type     : string;   // 数据库类型，例如 MYSQL, ORACLE。
    dbid     : number;   // 数据库的 DBID。
    catalog  : string;   // Catalog 名。
    schema   : string;   // Schema 名。
    table    : string;   // Table 名。
    view     : string;   // View 名。
    function_: string;   // 函数名。
    procedure: string;   // 存储过程名。
    connected: boolean;  // 是否已链接。
}

enum NodeType {
    DATABASE_TYPE      , // 数据库类型
    DATABASE_INSTANCE  , // 数据库实例
    CATALOG            , // Catalog
    SCHEMA             , // Schema

    TABLE_FOLDER       , // 表的目录
    TABLE              , // 表
    TABLE_COLUMN_FOLDER, // 表的列的目录
    TABLE_COLUMN       , // 表的列

    VIEW_FOLDER        , // 视图的目录
    VIEW               , // 视图
    VIEW_COLUMN_FOLDER , // 视图的列的目录
    VIEW_COLUMN        , // 视图的列

    INDEX_FOLDER       , // 索引的目录
    INDEX              , // 索引

    PROCEDURE_FOLDER   , // 存储过程的目录
    PROCEDURE          , // 存储过程
    FUNCTION_FOLDER    , // 函数的目录
    FUNCTION           , // 函数
}

/**
 * 节点的数据类型，定义了:
 * - 节点的类型
 * - CSS 类名
 * - title 显示的内容
 * - 是否叶子节点
 * - 不同节点特有的数据属性
 */
interface NodeData {
    type         : NodeType; // 节点的类型。
    title        : string;   // 节点显示的 title。
    id          ?: number;   // 节点的 ID。
    isLeaf      ?: boolean;  // 是否叶子节点，例如列、函数、存储过程都是叶子节点。
    databaseType?: string;   // 数据库类型，例如 MYSQL, ORACLE。
    dbid        ?: number;   // 数据库实例的 DBID。
    connected   ?: boolean;  // 数据库实例是否连接成功。
    catalog     ?: string;   // Catalog 名。
    schema      ?: string;   // Schema 名。
    table       ?: string;   // 表名。
    view        ?: string;   // 视图名。
    function_   ?: string;   // 函数名 (因为 function 是关键字，所以在最后增加了一个下划线 _)。
    procedure   ?: string;   // 存储过程名。
    dataType    ?: string;   // 列的类型，例如 INT。
    children    ?: any[];    // 孩子节点。
    _parent     ?: any;      // 父节点。
    _loaded     ?: boolean;  // 是否已经加载过。
}

/**
 * 节点的样式。
 */
interface NodeStyle {
    icon     : string, // 图标组件。
    className: string, // CSS 类名。
}

/**
 * 对象树节点的模板。
 */
const NODE_TEMPLATES: NodeData[] = [
    { type: NodeType.DATABASE_TYPE,       title: '<MySQL>', databaseType: '<MYSQL>' },
    { type: NodeType.DATABASE_INSTANCE,   title: '<db-name>', databaseType: '<MYSQL>', connected: false },
    { type: NodeType.CATALOG,             title: '<catalog-name>', catalog: '<catalog-name>' },
    { type: NodeType.SCHEMA,              title: '<schema-name>', schema: '<schema-name>' },

    // 表相关的。
    { type: NodeType.TABLE_FOLDER,        title: '表' },
    { type: NodeType.TABLE,               title: '<table-name>', table: '<table-name>' },
    { type: NodeType.TABLE_COLUMN_FOLDER, title: '列' },
    { type: NodeType.TABLE_COLUMN,        title: '<column-name>', dataType: '<INT>', isLeaf: true },

    // 视图相关的。
    { type: NodeType.VIEW_FOLDER,         title: '视图' },
    { type: NodeType.VIEW,                title: '<view-name>', view: '<view-name>' },
    { type: NodeType.VIEW_COLUMN_FOLDER,  title: '列' },
    { type: NodeType.VIEW_COLUMN,         title: '<column-name>', dataType: '<INT>', isLeaf: true },

    { type: NodeType.PROCEDURE_FOLDER,    title: '存储过程' },
    { type: NodeType.PROCEDURE,           title: '<proc-name>', procedure: '<proc-name>', isLeaf: true },
    { type: NodeType.FUNCTION_FOLDER,     title: '函数' },
    { type: NodeType.FUNCTION,            title: '<func-name>', function_: '<func-name>', isLeaf: true },
];

/**
 * 节点的样式，包含图标和 CSS 类名。
 */
const NODE_STYLES: {[key: string]: NodeStyle} = {
    [NodeType.DATABASE_TYPE]      : { className: 'node-db-type',             icon: 'side-tree__file' },
    [NodeType.DATABASE_INSTANCE]  : { className: 'node-db-instance',         icon: 'side-tree__file' },
    [NodeType.CATALOG]            : { className: 'node-catalog',             icon: 'side-tree__database' },
    [NodeType.SCHEMA]             : { className: 'node-schema',              icon: 'side-tree__schema' },

    [NodeType.TABLE_FOLDER]       : { className: 'node-table-folder',        icon: 'side-tree__file' },
    [NodeType.TABLE]              : { className: 'node-table',               icon: 'side-tree__table' },
    [NodeType.TABLE_COLUMN_FOLDER]: { className: 'node-table-column-folder', icon: 'side-tree__file' },
    [NodeType.TABLE_COLUMN]       : { className: 'node-table-column',        icon: 'side-tree__field' },

    [NodeType.VIEW_FOLDER]        : { className: 'node-view-folder',         icon: 'side-tree__file' },
    [NodeType.VIEW]               : { className: 'node-view',                icon: 'side-tree__view' },
    [NodeType.VIEW_COLUMN_FOLDER] : { className: 'node-view-column-folder',  icon: 'side-tree__file' },
    [NodeType.VIEW_COLUMN]        : { className: 'node-view-column',         icon: 'side-tree__field' },

    [NodeType.PROCEDURE_FOLDER]   : { className: 'node-procedure-folder',    icon: 'side-tree__file' },
    [NodeType.PROCEDURE]          : { className: 'node-procedure',           icon: 'side-tree__procedure' },
    [NodeType.FUNCTION_FOLDER]    : { className: 'node-function-folder',     icon: 'side-tree__file' },
    [NodeType.FUNCTION]           : { className: 'node-function',            icon: 'side-tree__function' },
}

/**
 * 数据库对应的图标。
 */
const DB_ICONS: {[key: string]: string} = {
    [DATABASE_TYPES.MYSQL]   : 'side-tree__mysql',
    [DATABASE_TYPES.ORACLE]  : 'side-tree__oracle',
    [DATABASE_TYPES.POSTGRES]: 'side-tree__pg',
}

/**
 * 菜单项 ID，用于识别点击的菜单项。
 */
enum MenuItemId {
    COPY,
    UPDATE,
    NEW_SQL_EDITOR,
    CLOSE_CONNECTION,
    AUDIT_TEMPLATE,
    WEB_TERMINAL,
    OPEN_TABLE,
}

/**
 * 右键菜单项。
 */
interface MenuItemData {
    id      : MenuItemId;  // 菜单项 ID。
    label   : string;      // 菜单项显示的内容。
    enabled : boolean;     // 菜单项是否可用。
    node   ?: NodeData;    // 节点数据。
}

/**
 * 右键菜单配置。
 */
const CONTEXT_MENU_CONFIGS: {[key: string]: MenuItemData[]} = {
    [NodeType.DATABASE_INSTANCE]: [
        { id: MenuItemId.NEW_SQL_EDITOR,   enabled: false, label: '新建查询' },
        { id: MenuItemId.CLOSE_CONNECTION, enabled: false, label: '关闭连接' },
        { id: MenuItemId.AUDIT_TEMPLATE,   enabled: true,  label: '审核模板' },
        { id: MenuItemId.WEB_TERMINAL,     enabled: false, label: '打开终端' },
        { id: MenuItemId.UPDATE,           enabled: true,  label: '刷新' },
        { id: MenuItemId.COPY,             enabled: true,  label: '复制' },
    ],
    [NodeType.CATALOG]: [
        { id: MenuItemId.NEW_SQL_EDITOR, enabled: true, label: '新建查询' },
        { id: MenuItemId.UPDATE,         enabled: true, label: '刷新' },
        { id: MenuItemId.COPY,           enabled: true, label: '复制数据库名' },
    ],
    [NodeType.SCHEMA]: [
        { id: MenuItemId.NEW_SQL_EDITOR, enabled: true, label: '新建查询' },
        { id: MenuItemId.UPDATE,         enabled: true, label: '刷新' },
        { id: MenuItemId.COPY,           enabled: true, label: '复制模式名' },
    ],

    // 表。
    [NodeType.TABLE_FOLDER]: [
        { id: MenuItemId.UPDATE,         enabled: true, label: '刷新' },
    ],
    [NodeType.TABLE]: [
        { id: MenuItemId.OPEN_TABLE,     enabled: false, label: '查看表数据' },
        { id: MenuItemId.COPY,           enabled: true,  label: '复制表名' },
    ],
    [NodeType.TABLE_COLUMN_FOLDER]: [
        { id: MenuItemId.UPDATE,         enabled: true, label: '刷新' },
    ],

    // 视图。
    [NodeType.VIEW_FOLDER]: [
        { id: MenuItemId.UPDATE,         enabled: true, label: '刷新' },
    ],
    [NodeType.VIEW]: [
        { id: MenuItemId.COPY,           enabled: true, label: '复制视图名' },
    ],
    [NodeType.VIEW_COLUMN_FOLDER]: [
        { id: MenuItemId.UPDATE,         enabled: true, label: '刷新' },
    ],
}

/**
 * 被删除的菜单项：key 是数据库类型，value 是菜单项的数组。
 */
const REMOVED_MENU_ITEMS: {[key:string]: MenuItemId[]} = {
    [DATABASE_TYPES.MYSQL]: [ MenuItemId.AUDIT_TEMPLATE ],
    [DATABASE_TYPES.POSTGRES]: [ MenuItemId.WEB_TERMINAL ],
}

export { NodeType, MenuItemId, NODE_TEMPLATES, NODE_STYLES, DB_ICONS, CONTEXT_MENU_CONFIGS, REMOVED_MENU_ITEMS }
export type { NodeData, NodeStyle, DatabaseConfig, DatabaseInfo, MenuItemData, DatabaseObject }
