/**
 * 对象树的数据结构。
 */

/*
对象树结构 (只有 catalog, 只有 schema，都有 catalog+schema):
MySQL
    192.168.12.100:3306
        catalog-test
            表
                table-1
                    列
                        column-1
                        column-2
                    索引
                        primary-key
                table-2
            视图
            函数
            存储过程
Oracle
    192.168.12.101:31001
        schema-test
            表
                table-1
                    列
                        column-1
                        column-2
                    索引
                        primary-key
                table-2
            视图
            函数
            存储过程
PostgreSQL
    192.168.12.102:5432
        catalog-test
            schema-test
                表
                    table-1
                        列
                            column-1
                            column-2
                        索引
                            primary-key
                    table-2
                视图
                函数
                存储过程
*/

import { NODE_TEMPLATES, NODE_STYLES, DB_ICONS } from '@/static/types/DatabaseObjectTreeTypes';
import { NodeType } from '@/static/types/DatabaseObjectTreeTypes';
import type { NodeData, NodeStyle, DatabaseInfo, DatabaseConfig } from '@/static/types/DatabaseObjectTreeTypes';
import type { DatabaseType } from '@/static/types/DatabaseTypes';

// 自动生成节点的 ID。
let nodeId: number = 0;

/**
 * 生成下一个节点 ID。
 *
 * @returns 返回节点 ID。
 */
function nextNodeId(): number {
    return ++nodeId;
}

/**
 * 节点的数据操作的辅助类。
 */
class NodeHelper {
    /**
     * 获取 node 所在分支的数据库信息，包含了数据库类型，dbid, catalog 和 schema, table, view, function, procedure 等。
     *
     * @param {Object} node Vue-Tree 的节点对象。
     * @returns 返回数据库信息对象。
     */
    static findDatabaseInfo(node: NodeData): DatabaseInfo {
        const info: DatabaseInfo = {
            type     : '',
            dbid     : 0,
            catalog  : '',
            schema   : '',
            table    : '',
            view     : '',
            function_: '',
            procedure: '',
            connected: false,
        };

        // 向上查找。
        while (node) {
            if (node.databaseType) {
                info.type = node.databaseType;
            }
            if (node.dbid) {
                info.dbid = node.dbid;
            }
            if (node.catalog) {
                info.catalog = node.catalog;
            }
            if (node.schema) {
                info.schema = node.schema;
            }
            if (node.table) {
                info.table = node.table;
            }
            if (node.view) {
                info.view = node.view;
            }
            if (node.function_) {
                info.function_ = node.function_;
            }
            if (node.procedure) {
                info.procedure = node.procedure;
            }
            if (node.type === NodeType.DATABASE_INSTANCE) {
                info.connected = !!node.connected;
            }

            node = node._parent;
        }

        return info;
    }

    /**
     * 获取树节点所属的数据库类型。
     *
     * @param node 树节点。
     * @returns 返回数据库类型。
     */
    static findDatabaseType(node: NodeData): DatabaseType {
        const dbInfo: DatabaseInfo = NodeHelper.findDatabaseInfo(node);
        const dbType: DatabaseType = dbInfo.type as DatabaseType;
        return dbType;
    }

    /**
     * 判断数据库是否只使用 catalog，不使用 schema。
     *
     * @param config 数据库元数据配置。
     * @returns 数据库只使用 catalog 时返回 true，否则返回 false。
     */
    static onlyUseCatalog(config: DatabaseConfig): boolean {
        return config.useCatalog && !config.useSchema;
    }

    /**
     * 判断数据库是否只使用 schema，不使用 catalog。
     *
     * @param config 数据库元数据配置。
     * @returns 数据库只使用 schema 时返回 true，否则返回 false。
     */
    static onlyUseSchema(config: DatabaseConfig): boolean {
        return !config.useCatalog && config.useSchema;
    }

    /**
     * 判断数据库是否同时使用 catalog 和 schema。
     *
     * @param config 数据库元数据配置。
     * @returns 数据库同时使用 catalog 和 schema 时返回 true，否则返回 false。
     */
    static useBothCatalogAndSchema(config: DatabaseConfig): boolean {
        return config.useCatalog && config.useSchema;
    }

    /**
     * 查询节点的样式，其属性有 icon 和 className。
     *
     * @param node Vue-Tree 节点数据。
     * @returns 返回节点的样式对象。
     */
    static findNodeStyle(node: NodeData): NodeStyle {
        /*
        逻辑:
        1. 获取节点对应的图标。
        2. 如果节点是数据库分配或者数据库实例，把图标换为对应数据库的商标。
        */

        // [1] 获取节点对应的图标。
        const style = Object.assign({}, NODE_STYLES[node.type]);

        // [2] 如果节点是数据库分配或者数据库实例，把图标换为对应数据库的商标。
        if (NodeType.DATABASE_TYPE === node.type || NodeType.DATABASE_INSTANCE === node.type) {
            const dbIcon = DB_ICONS[node.databaseType!];

            // 存在则替换，不存在则控制台输出警告信息。
            if (dbIcon) {
                style.icon = dbIcon;
            } else {
                console.warn(`数据库没有配置图标: ${node.databaseType}`);
            }
        }

        return style;
    }

    /**
     * 查找传入的节点类型对应的节点模板。
     *
     * @param type 节点的类型。
     * @returns 返回节点的模板。
     */
    static findNodeTemplate(type: NodeType): NodeData | null {
        for (const temp of NODE_TEMPLATES) {
            if (temp.type === type) {
                return temp;
            }
        }

        return null;
    }
}


/**
 * 节点创建工具，创建对象树的节点数据。
 */
class NodeCreator {
    /**
     * 创建数据库类型节点。
     *
     * @param databaseType 数据库类型，例如 MYSQL, ORACLE。
     * @param databaseLabel 数据库类型显示的 label，例如 MySQL, Oracle。
     * @returns 返回 Vue-Tree 的节点数据。
     */
    static createDatabaseTypeNode(databaseType: string, databaseLabel: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.DATABASE_TYPE),
            { id: nextNodeId(), databaseType: databaseType, title: databaseLabel }
        );
    }

    /**
     * 创建数据库实例节点。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param dbName 数据库名字，节点显示的 title。
     * @returns 返回 Vue-Tree 的节点数据。
     */
    static createDatabaseInstanceNode(type: string, dbid: number, dbName: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.DATABASE_INSTANCE),
            { id: nextNodeId(), databaseType: type, dbid, title: dbName }
        );
    }

    /**
     * 创建 catalog 节点。
     *
     * @param catalog 数据库的 catalog。
     * @returns 返回 Vue-Tree 的节点数据。
     */
    static createCatalogNode(catalog: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.CATALOG),
            { id: nextNodeId(), catalog: catalog, title: catalog }
        );
    }

    /**
     * 创建 schema 节点。
     *
     * @param schema 数据库的 schema。
     * @returns 返回 Vue-Tree 的节点数据。
     */
    static createSchemaNode(schema: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.SCHEMA),
            { id: nextNodeId(), title: schema, schema }
        );
    }

    /**
     * 创建表的目录节点。
     */
    static createTableFolderNode(): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.TABLE_FOLDER),
            { id: nextNodeId() }
        );
    }

    /**
     * 创建表的节点。
     *
     * @param table 表名。
     */
    static createTableNode(table: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.TABLE),
            { id: nextNodeId(), title: table, table }
        );
    }

    /**
     * 创建表的列的目录节点。
     */
    static createTableColumnFolderNode(): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.TABLE_COLUMN_FOLDER),
            { id: nextNodeId() }
        );
    }

    /**
     * 创建视图的目录节点。
     */
    static createViewFolderNode(): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.VIEW_FOLDER),
            { id: nextNodeId() }
        );
    }

    /**
     * 创建视图的节点。
     *
     * @param view 视图名。
     */
    static createViewNode(view: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.VIEW),
            { id: nextNodeId(), title: view, view }
        );
    }

    /**
     * 创建视图列的目录节点。
     */
    static createViewColumnFolderNode(): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.VIEW_COLUMN_FOLDER),
            { id: nextNodeId() }
        );
    }

    /**
     * 创建表的列节点。
     *
     * @param columnName 列名。
     * @param columnDataType 列的数据类型。
     */
    static createTableColumnNode(columnName: string, columnDataType: string): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.TABLE_COLUMN),
            { id: nextNodeId(), title: columnName, dataType: columnDataType }
        );
    }

    /**
     * 创建函数的目录节点。
     */
    static createFunctionFolderNode(): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.FUNCTION_FOLDER),
            { id: nextNodeId() }
        );
    }

    /**
     * 创建存储过程的目录节点。
     */
    static createProcedureFolderNode(): NodeData {
        return Object.assign(
            {},
            NodeHelper.findNodeTemplate(NodeType.PROCEDURE_FOLDER),
            { id: nextNodeId() }
        );
    }
}

/**
 * 导出。
 */
export { NodeCreator, NodeHelper }
