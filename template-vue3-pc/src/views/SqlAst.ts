import type { TableCoordinator } from '@/static/types';
import { getAst } from 'sql-formatter-ast';

/**
 * 常量定义。
 */
const CURSOR               = '__cursor__';      // 光标占位字符。
const TYPE_CLAUSE          = 'clause';          // 子句。
const TYPE_PROPERTY_ACCESS = 'property_access'; // 属性访问。
const TYPE_COMMA           = 'comma';           // 分号。
const TYPE_LITERAL         = 'literal';         // 字面量，例如字符串，数字。
const TYPE_FUNCTION        = 'function_call';   // 函数调用。
const TYPE_KEYWORD         = 'keyword';         // 关键字。
const TYPE_RESERVED_JOIN   = 'RESERVED_JOIN';   // JOIN 关键字。
const TYPE_STATEMENT       = 'statement';       // 语句节点。
const KEYWORD_ON           = 'ON';              // 关键字 ON。
const KEYWORD_AS           = 'AS';              // 关键字 AS。

const CLAUSE_SELECT             = 'SELECT';             // 查询。
const CLAUSE_FROM               = 'FROM';               // FROM 子句。
const CLAUSE_UPDATE             = 'UPDATE';             // 更新。
const CLAUSE_INSERT_INTO        = 'INSERT INTO';        // 插入。
const CLAUSE_INSERT_IGNORE_INTO = 'INSERT IGNORE INTO'; // 忽略插入重复的数据。
const CLAUSE_REPLACE_INTO       = 'REPLACE INTO';       // 插入时重复则覆盖。
const CLAUSE_DELETE_FROM        = 'DELETE FROM';        // 删除数据。
const CLAUSE_DROP_TABLE         = 'DROP TABLE';         // 删除表。
const CLAUSE_DROP_VIEW          = 'DROP VIEW';          // 删除视图。
const CLAUSE_TRUNCATE_TABLE     = 'TRUNCATE TABLE';     // 清空表。
const CLAUSE_CREATE_INDEX       = 'CREATE INDEX';       // 创建索引。
const CLAUSE_CREATE_VIEW        = 'CREATE VIEW';        // 创建视图。
const CLAUSE_ALTER_TABLE        = 'ALTER TABLE';        // 修改表。

/**
 * 语法树节点类型。
 */
interface Node {
    id            : number;
    type          : string;
    parent        : Node;
    text         ?: string;
    raw          ?: string;  // 作为关键字时 raw 为用户输入内容，text 为 raw 全部大写。
    nameKw       ?: any;     // type === 'clause': 子句节点有此属性。
    index        ?: number;  // 作为数组 children 元素的节点时在数组中的下标。
    [key: string] : any;     // 其他属性。
}

/**
 * 使用 SQL 语句生成语法树，然后可以在语法树中进行搜索。
 */
class SqlAst {
    /**
     * 节点 ID，从 1 开始。
     */
    private id: number = 0;

    /**
     * 语法树对象。
     */
    private ast: Node[] = [];

    /**
     * SQL 语句是否生成有效的语法树。
     */
    private valid: boolean = false;

    /**
     * 使用传入的 SQL 语句创建 SqlAst 对象。
     *
     * @param sql 要生成语法树的 SQL 语句。
     * @param databaseType 数据库类型: bigquery,db2,hive,mariadb,mysql,n1ql,plsql,postgresql,redshift,snowflake,spark,sql,sqlite
     */
    constructor(sql: string, databaseType: string) {
        // 生成 SQL 的语法树。
        try {
            // 语言支持: bigquery,db2,hive,mariadb,mysql,n1ql,plsql,postgresql,redshift,snowflake,spark,sql,sqlite
            this.ast = getAst(sql, { language: databaseType }) as unknown as Node[];
            this.buildTreeRelations(this.ast, { id: 0, type: '', parent: null as unknown as Node });
            this.valid = true;
        } catch (err) {
            console.debug(err);
            alert('生成语法树失败，括号不匹配或者引号不匹配的概率比较大');
        }
    }

    /**
     * 获取生成的语法树是否有效。
     *
     * @returns 正确生成语法树返回 true，否则返回 false。
     */
    isValid(): boolean {
        return this.valid;
    }

    /**
     * 给树节点生成 id，构建节点之间的父子关系。
     *
     * @param node 树节点或者节点的数组。
     * @param parent 当前节点的父节点。
     */
    private buildTreeRelations(node: Node | Node[], parent: Node): void {
        /*
         逻辑:
         1. 如果 node 是数组，则递归处理数组中的每一个元素。
         2. 如果 node 是对象:
            2.1. 生成 id、设置父节点，建立树节点的父子关系。
            2.2. 遍历 node 除 parent 外的属性，如果是对象或者数组则递归处理。
         */

        if (Array.isArray(node)) {
            // [1] 如果 node 是数组，则递归处理数组中的每一个元素。
            for (let i = 0; i < node.length; i++) {
                const n = node[i];

                if (isObject(n)) {
                    n.index = i; // 给每个数组元素下标属性，方便向前查找。
                    this.buildTreeRelations(n, parent);
                }
            }
        } else if (isObject(node)) {
            // [2] 如果 node 是对象:
            // [2.1] 生成 id、设置父节点，建立树节点的父子关系。
            node.id = ++this.id;
            node.parentId = parent.id;
            node.parent = parent;

            // [2.2] 遍历 node 除 parent 外的属性，如果是对象或者数组则递归处理。
            for (const key of Object.keys(node).filter(k => k !== 'parent')) {
                const field = node[key];
                if (isObject(field) || Array.isArray(field)) {
                    this.buildTreeRelations(field, node);
                }
            }
        }
    }

    /**
     * 把语法树对象转为字符串。
     *
     * @returns 返回字符串。
     */
    toString(): string {
        // 忽略 parent 属性，否则会造成无限递归。
        return JSON.stringify(this.ast, (key, value) => {
            return key === 'parent' ? undefined : value;
        }, 4);
    }

    /**
     * 查找语法树中光标所在节点。
     */
    findCursorNode(): Node | null {
        return this.doFindCursorNode(this.ast);
    }

    /**
     * 查找语法树中光标所在节点。
     */
    private doFindCursorNode(node: Node | Node[]): Node | null {
        /*
         逻辑:
         1. 如果 node 是数组，递归递归数组中的每一个元素，查找到则立即返回。
         2. 如果 node 是对象:
            2.1. 如果 node.text 包含了 '__cursor__' 则是光标所在节点，返回它。
            2.2. 遍历 node 除 parent 外的属性，如果是对象或者数组则递归查找，找到立即返回。
         */

        if (Array.isArray(node)) {
            // [1] 如果 node 是数组，递归递归数组中的每一个元素，查找到则立即返回。
            for (const n of node) {
                const found = this.doFindCursorNode(n);
                if (found) {
                    return found;
                }
            }
        } else if (isObject(node)) {
            // [2] 如果 node 是对象:
            // [2.1] 如果 node.text 包含了 '__cursor__' 则是光标所在节点，返回它。
            if (node.text?.includes(CURSOR)) {
                return node;
            }

            // [2.2] 遍历 node 除 parent 外的属性，如果是对象或者数组则递归查找，找到立即返回。
            for (const key of Object.keys(node).filter(k => k !== 'parent')) {
                const field = node[key];
                if (isObject(field) || Array.isArray(field)) {
                    const found = this.doFindCursorNode(field);
                    if (found) {
                        return found;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 查询节点所在的最近的子句 (子查询的时候会有多级子句)。
     *
     * @param node 语法树节点。
     * @returns 返回查找到的最近的子句对象。
     */
    findNearestClause(node: Node): Node {
        while (node?.type !== TYPE_CLAUSE) {
            node = node.parent;
        }

        return node;
    }

    /**
     * 获取节点的根节点，链式调用的相关节点看着一个整体，非链式调用的节点的根节点就是他自己，例如 column, schema.table.column。
     *
     * @param node 语法树节点。
     * @returns 返回节点根节点。
     */
    rootNodeMayWithPropertyAccess(node: Node): Node {
        return rootNodeMayWithPropertyAccess(node);
    }

    /**
     * 获取子句使用到的表名。
     *
     * @param clauseNode 子句的语法树节点。
     * @returns 返回表名数组。
     */
    findTablesInClause(clauseNode: Node): TableCoordinator[] {
        /*
        逻辑:
        1. 只有一个表的子句: INSERT INTO, INSERT IGNORE INTO, REPLACE INTO, UPDATE, DELETE FROM, DROP TABLE, TRUNCATE TABLE
        2. 创建索引子句。
        3. FROM 子句 (有多个表)。
        4. SELECT 子句 (找到 SELECT 的兄弟 FROM 节点，从 FROM 节点中获取表名)。
        */

        if (this.isClauseOfInsertInto(clauseNode)
            // [1] 只有一个表的子句: INSERT INTO, INSERT IGNORE INTO, REPLACE INTO, UPDATE, DELETE FROM, DROP TABLE, TRUNCATE TABLE
            || this.isClauseOfInsertIgnoreInto(clauseNode)
            || this.isClauseOfReplaceInto(clauseNode)
            || this.isClauseOfUpdate(clauseNode)
            || this.isClauseOfDeleteFrom(clauseNode)
            || this.isClauseOfDropTable(clauseNode)
            || this.isClauseOfTruncateTable(clauseNode)
        ) {
            return this.findTableInClauseWhichHasOnlyOneTable(clauseNode);
        } else if (this.isClauseOfCreateIndex(clauseNode)) {
            // [2] 创建索引子句。
            return this.findTableInCreateIndex(clauseNode);
        } else if (this.isClauseOfFrom(clauseNode)) {
            // [3] FROM 子句 (有多个表)。
            return this.findTablesInFromClause(clauseNode);
        }
        //  else if (this.isClauseOfSelect(clauseNode)) {
        //     // [4] SELECT 子句 (找到 SELECT 的兄弟 FROM 节点，从 FROM 节点中获取表名)。
        //     const statement = clauseNode.parent;
        //     for (const clause of statement.children) {
        //         if (this.isClauseOfFrom(clause)) {
        //             return this.findTablesInFromClause(clause);
        //         }
        //     }
        // }

        return [];
    }

    /**
     * 获取创建索引子句中的表。
     *
     * @param clauseNode 创建索引子句节点。
     * @returns 返回表坐标数组，如果查询不到则返回空数组。
     */
    findTableInCreateIndex(clauseNode: Node): TableCoordinator[] {
        // 在 ON 子句后面第一个元素: CREATE INDEX index_name ON table_name(column1, column2, column3);
        // 注意: table_name 为 user 的时候也会被解析为函数调用。
        const children = clauseNode.children;

        for (let i = 0; i < children && children.length; i++) {
            if (children[i].text === KEYWORD_ON && i+1 < children.length) {
                const tableNode: Node = children[i+1];
                const tokens: string[] = getNodeTokens(tableNode);
                const coord: TableCoordinator | null = buildTableCoordinator(tokens);
                return coord ? [coord] : [];
            }
        }

        return [];
    }

    /**
     * 从只有一个表的子句中获取表，这样的子句有:
     * - INSERT INTO
     * - INSERT IGNORE INTO
     * - REPLACE INTO
     * - UPDATE
     * - DELETE FROM
     * - DROP TABLE
     * - TRUNCATE TABLE
     *
     * @param clauseNode 子句节点。
     * @returns 返回表坐标数组，如果查询不到则返回空数组。
     */
    private findTableInClauseWhichHasOnlyOneTable(clauseNode: Node): TableCoordinator[] {
        /*
        逻辑 (以 INSERT INTO 为例: 有多种情况，表部分有可能解析为普通的表节点，也有可能被解析为函数调用节点):
        1. 只有 INSERT INTO，表信息为空。
        2. 被解析为函数调用: INSERT INTO user(id, name)，从 nameKw 节点获取表信息。
        3. 被解析为普通节点: INSERT INTO user2(id, name), INSERT INTO schema.user2(id, name)，需要处理链式调用，最多支持 2 级。
        */

        const children = clauseNode.children;

        // [1] 只有 INSERT INTO，表信息为空。
        if (!children || children.length == 0) {
            return [];
        }

        if (this.isClauseOfUpdate(clauseNode) || this.isClauseOfDeleteFrom(clauseNode)) {
            // 有别名的情况: UPDATE, DELETE FROM。
            const coord: TableCoordinator | null =  buildTableCoordinatorInRange(children, 0, children.length);
            return coord ? [coord] : [];
        } else {
            // 没有别名的情况。
            const coord: TableCoordinator | null =  buildTableCoordinatorInRange(children, 0, 1);
            return coord ? [coord] : [];
        }
    }

    /**
     * 获取 FROM 子句中的表。
     *
     * @param fromClauseNode FROM 子句节点。
     * @returns 返回表坐标数组，如果查询不到则返回空数组。
     */
    findTablesInFromClause(fromClauseNode: Node): TableCoordinator[] {
        // select id, age from user, person p left join student as s on s.id = user.id
        const children = fromClauseNode.children;
        const coords: TableCoordinator[] = [];

        let start: number = 0;
        let end: number = 0;

        // 处理: 逗号和第一个 JOIN 之前的表。
        // 分割:      ____  ________ _________
        // 样例: from user, person p left join
        while (++end < children.length) {
            if (this.isNodeOfComma(children[end]) || this.isNodeOfJoin(children[end])) {
                const coord: TableCoordinator | null = buildTableCoordinatorInRange(children, start, end);
                if (coord) {
                    coords.push(coord);
                }

                start = end + 1;
            }

            // 遇到 JOIN 节点结束循环。
            if (this.isNodeOfJoin(children[end])) {
                break;
            }
        }

        // 处理: JOIN 和 ON 之间的表。
        // 分割: _________ ____________ __
        // 样例: left join student as s on s.id = user.id
        let joinMatched: boolean = false;
        while (end < children.length) {
            if (this.isNodeOfJoin(children[end])) {
                start = end + 1;
                joinMatched = false;
            }

            if (this.isNodeOfOn(children[end])) {
                joinMatched = true;

                // 查找 JOIN 和 ON 之间的表。
                const coord: TableCoordinator | null = buildTableCoordinatorInRange(children, start, end);
                if (coord) {
                    coords.push(coord);
                }
            }

            end++;
        }

        // 处理 (最后一个表):
        // - from user
        // - from user u, person as p 中 (', person as p' 部分)
        if (!joinMatched) {
            // JOIN 如果配对处理过不要重复处理。
            const coord: TableCoordinator | null = buildTableCoordinatorInRange(children, start, end);
            if (coord) {
                coords.push(coord);
            }
        }

        return coords;
    }

    /**
     * 判断传入的节点是否 FROM 子句的表名节点 (不是别名节点)。
     *
     * @param nodeInFromClause FROM 子句中的节点。
     * @returns 传入的节点是表名返回 true，否则返回 false。
     */
    isNodeOfTableNameInFromClause(nodeInFromClause: Node): boolean {
        const node: Node = this.rootNodeMayWithPropertyAccess(nodeInFromClause);
        const children: Node[] = node.parent.children;
        const prevNode: Node = children[node.index!-1];

        // 非 FROM 的直接节点返回 false。
        if (!this.isDirectChildOfClause(node, CLAUSE_FROM)) {
            return false;
        }

        // 第一个节点、前一个节点为逗号或者 JOIN 则是表名，返回 false。
        // FROM user
        // FROM user, student
        if (node.index === 0 || this.isNodeOfComma(prevNode) || this.isNodeOfJoin(prevNode)) {
            return true;
        }

        return false;
    }

    /**
     * 判断传入的节点是否非 FROM 子句的表名节点 (不是别名节点)。
     * 前提条件: 首先知道节点 node 是表名部分的节点。
     *
     * @param node 语法树节点。
     * @returns 传入的节点是表名节点返回 true，否则返回 false。
     */
    isNodeOfTableNameNotInFromClause(node: Node): boolean {
        node = this.rootNodeMayWithPropertyAccess(node);
        const children: Node[] = node.parent.children;
        const prevNode: Node = children[node.index!-1];

        // 第一个节点、前一个节点为逗号则是表名，返回 false。
        if (node.index === 0 || this.isNodeOfComma(prevNode)) {
            return true;
        }

        return false;
    }

    /**
     * 判断传入的 node 是否传入的子句 clause 的直接子节点。
     * - FROM schema.table: schema 和 table 是子句 FROM 的直接子节点 (注意链式调用作为一个整体)。
     * - INSERT INTO user (id, name): user 是子句 INSERT INTO 的直接子节点，id 和 name 不是子句 INSERT INTO 的直接子节点。
     *
     * @param node 语法树节点。
     * @param clauseName 子句类型名例如 'FROM'。
     * @returns 如果 node 是传入的子句 clause 的直接子节点返回 true，否则返回 false。
     */
    isDirectChildOfClause(node: Node, clauseName: string): boolean {
        /*
         逻辑:
         1. 找到节点的根节点: 非链式访问和链式访问。
         2. 根节点的父节点是传入的子句节点则返回 true，否则返回 false。
         */
        const rootNode = this.rootNodeMayWithPropertyAccess(node);
        return isClauseNode(rootNode.parent, clauseName);
    }

    //////////////////////////////////////////////////////////////////////////
    //                               判断子句类型                             //
    //////////////////////////////////////////////////////////////////////////
    /**
     * 判断传入的 clauseNode 是否 SELECT 子句节点。
     *
     * @param clauseNode 语法树节点。
     * @returns 是 SELECT 子句返回 true，否则返回 false。
     */
    isClauseOfSelect(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_SELECT);
    }
    isClauseOfFrom(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_FROM);
    }
    isClauseOfUpdate(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_UPDATE);
    }
    isClauseOfInsertInto(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_INSERT_INTO);
    }
    isClauseOfInsertIgnoreInto(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_INSERT_IGNORE_INTO);
    }
    isClauseOfReplaceInto(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_REPLACE_INTO);
    }
    isClauseOfDeleteFrom(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_DELETE_FROM);
    }
    isClauseOfDropTable(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_DROP_TABLE);
    }
    isClauseOfDropView(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_DROP_VIEW);
    }
    isClauseOfTruncateTable(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_TRUNCATE_TABLE);
    }
    isClauseOfAlterTable(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_ALTER_TABLE);
    }
    isClauseOfCreateIndex(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_CREATE_INDEX);
    }
    isClauseOfCreateView(clauseNode: Node): boolean {
        return isClauseNode(clauseNode, CLAUSE_CREATE_VIEW);
    }
    isNodeOfComma(node: Node): boolean {
        return node?.type === TYPE_COMMA;
    }
    isNodeOfJoin(node: Node): boolean {
        return node?.tokenType === TYPE_RESERVED_JOIN;
    }
    isNodeOfOn(node: Node): boolean {
        return node?.type === TYPE_KEYWORD && node.text === KEYWORD_ON;
    }
    isNodeOfFunction(node: Node): boolean {
        return node?.type === TYPE_FUNCTION;
    }
    isNodeOfAs(node: Node): boolean {
        return node?.type === TYPE_KEYWORD && node.text === KEYWORD_AS;
    }
    isNodeOfStatement(node: Node): boolean {
        return node?.type === TYPE_STATEMENT;
    }
}

/**
 * 判断传入的参数是否对象 (不是数组)。
 *
 * @param obj 对象。
 * @returns 是对象返回 true，否则返回 false。
 */
function isObject(obj: any): boolean {
    return (typeof obj === 'object') && !Array.isArray(obj) && obj !== null;
}

/**
 * 判断传入的节点 node 是否传入的 clauseName 指定的子句。
 *
 * @param node 语法树节点。
 * @param clauseName 子句的类型名。
 * @returns 如果目标子句返回 true，否则返回 false。
 */
function isClauseNode(node: Node, clauseName: string): boolean {
    return node?.type === TYPE_CLAUSE && node.nameKw.text === clauseName;
}

/**
 * 获取节点的根节点，链式调用的相关节点看着一个整体，非链式调用的节点的根节点就是他自己，例如 column, schema.table.column。
 *
 * @param node 语法树节点。
 * @returns 返回节点根节点。
 */
function rootNodeMayWithPropertyAccess(node: Node): Node {
    while (node?.parent.type === TYPE_PROPERTY_ACCESS) {
        node = node.parent;
    }

    return node;
}

/**
 * 获取语法树节点相关的用户输入的内容，如果参数 beforeCursor 为 true 则获取光标前的输入内容 (可能包含级联的情况) ('|' 表示光标)。
 *
 * 测试用例:
 * beforeCursor 为 false:
 * - table|                 返回 ['table']
 * - table|xx               返回 ['tablexx']
 * - schema.table|          返回 ['schema', 'table']
 * - schema.table|.column   返回 ['schema', 'table', 'column']
 * - schema.table|xx.column 返回 ['schema', 'tablexx', 'column']
 * - schema.table.column|   返回 ['schema', 'table', 'column']
 * - schema.table.column    返回 ['schema', 'table', 'column']
 *
 * beforeCursor 为 true:
 * - table|                返回 ['table']
 * - table|xx              返回 ['table']
 * - schema.table|         返回 ['schema', 'table']
 * - schema.table|.column  返回 ['schema', 'table']
 * - schema.table|x.column 返回 ['schema', 'table']
 * - schema.table.column|  返回 ['schema', 'table', 'column']
 * - schema.table.column   返回 [] (没有光标)
 *
 * @param node 语法树节点。
 * @param beforeCursor 是否只获取光标前的 token，默认为 false。
 * @returns 返回光标前输入内容 tokens 的数组，每个 token 作为一个数组的元素，例如 [ 'schema', 'table_name' ]。
 */
function getNodeTokens(node: Node, beforeCursor: boolean = false): string[] {
    /*
     逻辑:
     1. 如果 node 的类型是字面量直接返回: name='hel__cursor__lo'。
     2. 如果是函数调用节点则从 nameKw 节点获取。
     3. 获取节点的输入内容，有可能包含链式的情况，例如 schema.table.column。
     4. 去掉 ` 和双引号: 表名和列有可能会用 ` 或 " 括起来。
     5. 只保留到包含 __cursor__ 的元素并且把 __cursor__ 去掉: ['schema', 'table__cursor__', 'column'] 返回 ['schema', 'table']
     */

    // 先定位到链式节点的最顶层节点 (处理链式情况)。
    node = rootNodeMayWithPropertyAccess(node);

    // [1] 如果 node 的类型是字面量直接返回: name='hel__cursor__lo'。
    if (node.type === TYPE_LITERAL) {
        return [];
    }

    let tokens: string[] = [];

    // [2] 如果是函数调用节点则从 nameKw 节点获取。
    if (node.type === TYPE_FUNCTION) {
        node = node.nameKw;
    }

    // [3] 获取节点的输入内容，有可能包含链式的情况，例如 schema.table.column。
    while (node.type === 'property_access') {
        tokens.unshift(node.property.raw || node.property.text);
        node = node.object;

    }
    tokens.unshift(node.raw || node.text!);

    // [4] 去掉 ` 和双引号: 表名和列有可能会用 ` 或 " 括起来。
    tokens = tokens.map(t => t.replace(/"/g, '')).map(t => t.replace(/`/g, ''));

    if (beforeCursor) {
        // [5] 只保留到包含 __cursor__ 的元素并且把 __cursor__ 去掉: ['schema', 'table__cursor__', 'column'] 返回 ['schema', 'table']
        for (let i = 0; i < tokens.length; i++) {
            if (tokens[i].includes(CURSOR)) {
                tokens[i] = tokens[i].replace(CURSOR, '')

                return tokens.slice(0, i+1);
            }
        }

        // 没有光标，则返回空数组。
        return [];
    } else {
        return tokens.map(t => t.replace(CURSOR, ''))
    }
}

/**
 * 构建表的坐标对象。
 *
 * @param tokens 表的信息，可包括 schema > table, table
 * @returns 返回表的坐标对象，如果 tokens 无效则返回 null。
 */
function buildTableCoordinator(tokens: string[]): TableCoordinator | null {
    if (tokens.length === 1) {
        // INSERT INTO user2(id, name)
        return { catalog: '', schema: '', table: tokens[0] };
    } else if (tokens.length === 2) {
        // INSERT INTO schema.user2(id, name)
        return { catalog: '', schema: tokens[0], table: tokens[1] };
    } else {
        // 不是 2 级，级既不是 [table] 也不是 [schema, table] 则数据无效。
        console.debug(`Tokens 数量不为 1 或者 2: ${tokens}`);
        return null;
    }
}

/**
 * 使用 children 指定范围的节点构建表的坐标对象，表节点的范围为 children[start, end)。
 *
 * @param children 节点所在的数组，即子句的 children 子节点数组。
 * @param start 表开始节点的位置。
 * @param end 表结束节点的下一个位置。
 * @returns 返回表的坐标对象，如果范围无效则返回 null。
 */
function buildTableCoordinatorInRange(children: Node[], start: number, end: number): TableCoordinator | null {
    if (start < 0 || start >= children.length) {
        return null;
    }

    const tokens: string[] = getNodeTokens(children[start], false);
    const coord: TableCoordinator | null = buildTableCoordinator(tokens);

    // 注意: 三级及以上链式调用是无效的，会创建 null 的表坐标: a.b.c, a.b.c.d, ...
    if (coord === null) {
        return null;
    }

    // start 后面的第 1 个或者第 2 节点是别名。
    if (end === start+1) {
        // FROM schema.user WHERE
    } else if (end === start+2 && start+1 < children.length) {
        // FROM schema.user u WHERE
        coord!.alias = children[start+1].raw || children[start+1].text;
    } else if (end >= start+3 && start+2 < children.length && children[start+1].text === KEYWORD_AS) {
        // FROM schema.user AS u WHERE: 第 2 个节点是 AS
        coord!.alias = children[start+2].raw || children[start+2].text;
    }

    return coord;
}

export type { Node };

export {
    SqlAst,
    getNodeTokens,
};
