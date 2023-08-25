import {
    SqlAst,
    getNodeTokens,
} from './SqlAst';

import type { TableCoordinator  } from '@/static/types';
import type { Node } from './SqlAst';

/**
 * 提示建议枚举。
 */
enum AdviceType {
    NO            , // 0：不补全。
    KEYWORD       , // 1：关键字 (关键字包含函数)。
    TABLE         , // 2：表名。
    COLUMN_KEYWORD, // 3：列和关键字。
}

/**
 * 提示建议类型。
 *
 * prefix 为输入前缀，例如 schema.table.| 中的 schema 和 table:
 * - type 为 TABLE:
 *   - 只有一个元素: [schema_pre], [table_pre]
 *   - 包含二个元素: [schema, table_pre], [schema, ''] (空字符串表示输入 schema.)
 * - type 为 KEYWORD，只取最后一个有意义: [keyword_pre]
 * - type 为 COLUMN_KEYWORD:
 *   - 只有一个元素: [schema_pre], [table_pre], [column_pre]
 *   - 包含二个元素: [schema, table_pre], [schema, ''], [table, column_pre], [table, ''] (空字符串表示输入 table.)
 *   - 包含三个元素: [schema, table, column_pre], [schema, table, ''] (空字符串表示输入 schema.table.)
 *   - 如果 tables 为空，则说明只需要补全关键字。
 * 上面提示中 column 使用到的 schema, table 需要从 tables 中进行匹配。
 */
interface Advice {
    type  : AdviceType;
    prefix: string[];
    tables: TableCoordinator[];
}

// 不补全提示。
const ADVICE_NO: Advice = { type: AdviceType.NO, prefix: [], tables: [] };

// 关键字补全提示。
const ADVICE_KEYWORD = (prefix: string[]) => {
    return { type: AdviceType.KEYWORD, prefix, tables: [] };
}

/**
 * 创建 SqlAdvisor 的参数。
 */
interface SqlAdvisorParam {
    wholeSql       ?: string; // 完整的 SQL 语句。
    sqlBeforeCursor?: string; // 光标前的 SQL。
    sqlAfterCursor ?: string; // 光标后的 SQL。
    databaseType    : 'mysql' | 'db2' | 'plsql' | 'postgresql' | 'sql';
}

/**
 * SQL 补全提示类。
 */
class SqlAdvisor {
    private sqlAst: SqlAst;

    /**
     * 如果提供了光标前的 sqlBeforeCursor 和光标后的 sqlAfterCursor SQL 语句，
     * 把它们拼接起来作为完整的 SQL 语句进行分析，否则使用 wholeSql 进行分析。
     *
     * @param param 创建 Advisor 的参数。
     */
    constructor(param: SqlAdvisorParam) {
        // 生成语法树。
        let sql: string = param.wholeSql || '';

        if (param.sqlBeforeCursor || param.sqlAfterCursor) {
            sql = `${param.sqlBeforeCursor}__cursor__${param.sqlAfterCursor}`;
        }

        this.sqlAst = new SqlAst(sql, param.databaseType);
    }

    /**
     * 生成 SQL 语句光标处的提示。
     *
     * @returns 返回提示对象。
     */
    advise(): Advice {
        /*
        逻辑 (根据光标节点所在子句类型决定补全的内容):
        1. 查找光标所在节点的顶层节点。
        2. 光标前一个节点是 AS 则补全关键字。
        3. 简单语句的表补全: 子句中只有表内容。
        4. 创建视图子句中的视图名不需要补全。
        5. FROM 子句中有 3 种情况: 表名、别名、列名
           5.1 是表名节点补全表。
           5.2 向前遍历，先遇到 ON 则补全列和关键字，先遇到 JOIN 则说明是表的别名或者后面是 LEFT，WHERE 等则补全关键字 (例如 FROM user WHER|)。
        6. 其他情况补全列和关键字。
        */

        if (!this.sqlAst.isValid) {
            return ADVICE_NO;
        }

        // [1] 查找光标所在节点的顶层节点。
        let cursorNode: Node | null = this.sqlAst.findCursorNode();
        if (!cursorNode) {
            return ADVICE_NO;
        }
        cursorNode = this.sqlAst.rootNodeMayWithPropertyAccess(cursorNode);

        // 光标前输入的内容，提示时用于 scope 定位。
        const prefix: string[] = getNodeTokens(cursorNode, true);

        // [2] 光标前一个节点是 AS 则补全关键字。
        if (cursorNode.index! > 0 && this.sqlAst.isNodeOfAs(cursorNode.parent.children[cursorNode.index!-1])) {
            return ADVICE_KEYWORD(prefix);
        }

        const clause = cursorNode.parent;
        if (this.sqlAst.isClauseOfInsertInto(clause)
            || this.sqlAst.isClauseOfInsertIgnoreInto(clause)
            || this.sqlAst.isClauseOfReplaceInto(clause)
            || this.sqlAst.isClauseOfUpdate(clause)
            || this.sqlAst.isClauseOfDeleteFrom(clause)
            || this.sqlAst.isClauseOfDropTable(clause)
            || this.sqlAst.isClauseOfDropView(clause)
            || this.sqlAst.isClauseOfTruncateTable(clause)
            || this.sqlAst.isClauseOfAlterTable(clause)
        ) {
            // [3] 简单语句的表补全: 子句中只有表内容。
            if (this.sqlAst.isNodeOfTableNameNotInFromClause(cursorNode)) {
                // 表名: 补全表。
                return { type: AdviceType.TABLE, prefix, tables: [] };
            } else {
                // 别名和其他情况: 补全关键字, 例如 DELETE FROM user WHER|。
                return ADVICE_KEYWORD(prefix);
            }
        } else if (this.sqlAst.isClauseOfCreateView(clause)) {
            // [4] 创建视图子句中的视图名不需要补全。
            return ADVICE_NO;
        } else if (this.sqlAst.isClauseOfFrom(clause)) {
            // [5] FROM 子句中有 3 种情况: 表名、别名、列名
            // [5.1] 是表名节点补全表。
            if (this.sqlAst.isNodeOfTableNameInFromClause(cursorNode)) {
                return { type: AdviceType.TABLE, prefix, tables: [] };
            }

            // [5.2] 向前遍历，先遇到 ON 则补全列和关键字，先遇到 JOIN 则说明是表的别名或者后面是 LEFT，WHERE 等则补全关键字 (例如 FROM user WHER|)。
            for (let i = cursorNode.index!-1; i >= 0; i--) {
                const prevNode: Node = cursorNode.parent.children[i];

                if (this.sqlAst.isNodeOfOn(prevNode)) {
                    // 先遇到 ON 则补全列和关键字。
                    return this.createAdviceForColumnAndKeyword(cursorNode, prefix);
                } else if (this.sqlAst.isNodeOfJoin(prevNode)) {
                    // 先遇到 JOIN 则说明是表的别名或者光标后面是 LEFT/RIGHT，WHERE 等则补全关键字 (例如 FROM user WHER|)。
                    break;
                }
            }

            return ADVICE_KEYWORD(prefix);
        } else {
            // [6] 其他情况补全列和关键字。
            return this.createAdviceForColumnAndKeyword(cursorNode, prefix);
        }
    }

    // 创建列和关键字提示对象。
    private createAdviceForColumnAndKeyword(node: Node, prefix: string[]): Advice {
        /*
        逻辑 (可能存在嵌套子查询):
        1. 查询节点所在最近的子句 clause。
        2. 遍历子句 clause 的兄弟子句查找使用的表。
        3. 向上查找子句 (嵌套子查询)，如果节点是 statement 则结束循环。
        */
        const tables: TableCoordinator[] = [];

        for (;;) {
            // [1] 查询节点所在最近的子句 clause。
            const clause: Node = this.sqlAst.findNearestClause(node);
            if (!clause || !clause.parent) {
                break;
            }

            // [2] 遍历子句 clause 的兄弟子句查找使用的表。
            for (const siblingClause of clause.parent.children) {
                tables.push(...this.sqlAst.findTablesInClause(siblingClause));
            }

            // [3] 向上查找子句 (嵌套子查询)，如果节点是 statement 则结束循环。
            node = clause.parent;
            if (this.sqlAst.isNodeOfStatement(node)) {
                break;
            }
        }

        return { type: AdviceType.COLUMN_KEYWORD, prefix, tables };
    }
}

export {
    SqlAdvisor,
    AdviceType
};

export type { Advice };
