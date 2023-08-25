import { editor, languages, Position } from 'monaco-editor';
import { SqlAdvisor, AdviceType } from './SqlAdvisor';
import type { Advice } from './SqlAdvisor';
import { useDatabaseObjectStore } from '@/stores/DatabaseObjectStore';
import type { DatabaseObject, DatabaseConfig } from '@/static/types/DatabaseObjectTreeTypes';

import { LOAD_STATE_INIT, LOAD_STATE_LOADING, LOAD_STATE_SUCCESS, TABLE, VIEW } from '@/static/types';
import type { DatabaseType, TableCoordinator } from '@/static/types';

/**
 * 自动补全获取结果:
 * - 0: 可补全的数据库对象数组。
 * - 1: 可使用状态，加载中还是加载成功可直接使用。
 */
type CompletionResult = [DatabaseObject[], number];
const COMPLETION_RESULT_INIT: CompletionResult          = [[], LOAD_STATE_INIT];
const COMPLETION_RESULT_LOADING: CompletionResult       = [[], LOAD_STATE_LOADING];
const COMPLETION_RESULT_SUCCESS_EMPTY: CompletionResult = [[], LOAD_STATE_SUCCESS];

/**
 * SQL 语句补全提供类。
 */
export class SqlCompletionProvider implements languages.CompletionItemProvider {
    /**
     * 触发自动提示的字符。
     */
    public readonly triggerCharacters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.('.split('');

    /**
     * 数据库对象 Store。
     */
    private dbStore;

    /**
     * 数据库配置。
     */
    private dbConfig: DatabaseConfig | null = null;

    /**
     * 数据库 ID。
     */
    private dbid: number = 0;

    /**
     * Catalog。
     */
    private catalog: string | null = null;

    /**
     * Schema。
     */
    private schema: string | null = null;

    /**
     * 补全环境是否准备好了。
     */
    private valid: boolean = false;

    /**
     * 编辑器对象。
     */
    private editor: editor.IStandaloneCodeEditor;

    constructor(editor: editor.IStandaloneCodeEditor) {
        this.dbStore = useDatabaseObjectStore();
        this.editor = editor;
    }

    /**
     * 设置编辑器的使用环境。
     */
    public setup(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null): void {
        this.dbid     = dbid;
        this.catalog  = catalog;
        this.schema   = schema;
        this.valid    = false;
        this.dbConfig = this.dbStore.findDatabaseConfig(type); // 获取数据库配置。

        // 参数校验。
        if (!this.dbConfig) {
            return;
        }
        if (this.dbConfig.useCatalog && !catalog) {
            return;
        }
        if (this.dbConfig.useSchema && !schema) {
            return;
        }

        // 确保预先加载当前 catalog | schema 下的表和视图，补全的时候体验更好。
        this.dbStore.findTablesAndViews(this.dbConfig.type, this.dbid, this.catalog, this.schema).then(() => {
            this.valid = true;
        }).catch(() => {});
    }

    public provideCompletionItems(model: editor.ITextModel, position: Position): languages.ProviderResult<languages.CompletionList> {
        // 补全环境准备好后才会使用 SQL 补全提示，否则使用编辑器的默认补全功能。
        if (!this.valid) {
            return { suggestions: [] }
        }

        const beforeRange = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: model.getLineMinColumn(position.lineNumber),
            endColumn: position.column,
        }

        const afterRange = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: position.column,
            endColumn: model.getLineMaxColumn(position.lineNumber),
        }

        const sqlBeforeCursor = model.getValueInRange(beforeRange);
        const sqlAfterCursor = model.getValueInRange(afterRange);
        const advice: Advice = new SqlAdvisor({ sqlBeforeCursor, sqlAfterCursor, databaseType: 'postgresql' }).advise();
        console.log(JSON.stringify(advice, null, 4));

        // Oracle
        // this.dbStore.findTableColumns('ORACLE', 2, { catalog: null, schema: 'BIAO', table: 'SP_TEST' });
        // setTimeout(() => {
        //     this.dbStore.findTableColumns('ORACLE', 2, { catalog: null, schema: 'BIAO', table: 'SP_TEST' }).then((data) => {
        //         console.log(data);
        //     }).catch((error) => {
        //         console.error(error);
        //     });
        // }, 10)

        // MySQL
        // this.dbStore.findTableColumns('MYSQL', 1, { catalog: 'test', schema: null, table: 'test' });
        // setTimeout(() => {
        //     this.dbStore.findTableColumns('MYSQL', 1, { catalog: 'test', schema: null, table: 'test' }).then((data) => {
        //         console.log(data);
        //     }).catch((error) => {
        //         console.error(error);
        //     });
        // }, 10)

        // this.findCompletionCatalogsOrSchemas(cfg, 'MYSQL', 1, null);
        // this.findCompletionCatalogsOrSchemas(cfg, 'ORACLE', 2, null);
        // this.findCompletionCatalogsOrSchemas(cfg, 'POSTGRES', 3, 'postgres');
        // this.findCompletionTablesAndViews(cfg, 'MYSQL', 1, 'test', null);

        // return {
        //     suggestions: []
        // }

        const range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: position.column-1,
            endColumn: position.column,
        };

        // console.log(this.findTableParentCatalogsOrSchemas());
        // console.log(this.findTables('biao'));
        const tableCoordinators = [
            { catalog: null, schema: null, table: 'sp_test' },
            { catalog: null, schema: 'ldoa', table: 'audit' },
        ];
        this.correctTableCoordinatorsFromCompletion(tableCoordinators);
        const [columns, loadState] = this.findTableColumnsList(tableCoordinators);
        console.log('补全', loadState, columns);

        if (loadState === LOAD_STATE_INIT) {
            this.dbStore.findTableColumnsList(this.dbConfig?.type as DatabaseType, this.dbid, tableCoordinators);
        }


        // return {
        //     suggestions: []
        // }

        return { suggestions: [
            {
                label: 'foo',
                kind: languages.CompletionItemKind.Function,
                insertText: 'keyword',
                range: range,
                detail: "说明",
            },
            {
                label: 'bar',
                kind: languages.CompletionItemKind.Folder,
                insertText: 'table_name',
                range: range,
            },
            {
                label: 'fox',
                kind: languages.CompletionItemKind.Class,
                insertText: 'column_name',
                range: range,
            }
        ] };
    }

    /**
     * 直接从 dbStore.dbObjects 中获取当前环境的表的上一级节点 catalogs or schemas:
     * - useCatalog  && useSchema : 返回 schemas。
     * - useCatalog  && !useSchema: 返回 catalogs。
     * - !useCatalog && useSchema : 返回 schemas。
     *
     * 补全行为:
     * - LOAD_STATE_INIT   : 未加载，调用者发送请求从服务器端进行加载。
     * - LOAD_STATE_LOADING: 加载中，调用者忽略，不用重复发起加载请求。
     * - LOAD_STATE_SUCCESS: 加载成功，可补全使用。
     *
     * @returns 返回 catalogs 或者 schemas 可补全的结果。
     */
    private findTableParentCatalogsOrSchemas(): CompletionResult {
        /*
        逻辑:
        1. 获取 root 节点，其子节点可能为 catalog 也可能为 schema。
        2. 同时使用 useCatalog 和 useSchema 时找到第一级 catalog。
        3. 根据 parent 节点的加载状态进行处理。

        提示:
        - 在 setup 的时候先加载了当前 catalog | schema 下的表。
        - 此时 root 节点一定存在。
        - 表的父级节点的 catalog | schema 即使不存在也可以表示为加载中，因为至少已经有请求在加载。
        */

        // [1] 获取 root 节点，其子节点可能为 catalog 也可能为 schema。
        const cfg = this.dbConfig!;
        const key: string = this.dbStore.buildDbObjectKey(cfg.type, this.dbid);
        let parent: DatabaseObject | null = this.dbStore.dbObjects.get(key)!; // root.

        // [2] 同时使用 useCatalog 和 useSchema 时找到第一级 catalog。
        if (cfg.useCatalog && cfg.useSchema) {
            if (parent.loadState === LOAD_STATE_INIT || parent.loadState === LOAD_STATE_LOADING) {
                return COMPLETION_RESULT_LOADING;
            }

            // Parent 此时是 catalog (实际上这里不应该不存在，因为是下拉框选择的)。
            parent = parent.children.find(cat => cat.name === this.catalog) || null;
            if (!parent) {
                return COMPLETION_RESULT_SUCCESS_EMPTY;
            }
        }

        // [3] 根据 parent 节点的加载状态进行处理。
        // Parent 可能是 root (children 是 catalogs or schemas)，也可能是 catalog (children 是 schemas)。
        if (parent.loadState === LOAD_STATE_INIT || parent.loadState === LOAD_STATE_LOADING) {
            return COMPLETION_RESULT_LOADING;
        } else {
            return [parent.children, LOAD_STATE_SUCCESS];
        }
    }

    /**
     * 直接从 dbStore.dbObjects 中获取可补全的表。
     *
     * @param tableParentCatalogOrSchema 表的父级节点 catalog 或者 schema。
     * @returns 返回 table 可补全的结果。
     */
    private findTables(tableParentCatalogOrSchema: string): CompletionResult {
        /*
        逻辑:
        1. 查找当前环境下的 catalogs 和 schemas，如果父级节点没有加载完成则返回。
        2. 查找表的父级节点 parent，根据 parent 的状态分别处理:
           2.1. parent 不存在则返回空的成功。
           2.2. parent 未加载。
           2.3. parent 加载中。
           2.4. parent 加载完成。
        */

        // [1] 查找当前环境下的 catalogs 和 schemas，如果父级节点没有加载完成则返回。
        const [tableParentCatalogsOrSchemas, loadState]: CompletionResult = this.findTableParentCatalogsOrSchemas();

        if (loadState === LOAD_STATE_INIT || loadState === LOAD_STATE_LOADING) {
            return [[], loadState];
        }

        // [2] 查找表的父级节点 parent，根据 parent 的状态分别处理:
        const parent: DatabaseObject | null = tableParentCatalogsOrSchemas.find(p => p.name === tableParentCatalogOrSchema) || null;

        if (!parent) {
            return COMPLETION_RESULT_SUCCESS_EMPTY;
        } else if (parent.loadState === LOAD_STATE_INIT) {
            return COMPLETION_RESULT_INIT;
        } else if (parent.loadState === LOAD_STATE_LOADING) {
            return COMPLETION_RESULT_LOADING;
        } else {
            const tables: DatabaseObject[] = parent.children.filter(tv => tv.type === TABLE || tv.type === VIEW);
            return [tables, LOAD_STATE_SUCCESS];
        }
    }

    /**
     * 校正 TableCoordinator 的数据 (自动补全时传入的坐标中 catalog 为空，schema 为 catalog 或者 schema)。
     *
     * @param tableCoordinators 表的坐标。
     * @returns 返回校正后的表坐标数组。
     */
    private correctTableCoordinatorsFromCompletion(tableCoordinators: TableCoordinator[]): TableCoordinator[] {
        const cfg: DatabaseConfig = this.dbConfig!;

        for (const coord of tableCoordinators) {
            const catOrSch = coord.schema;

            if (cfg.useCatalog && cfg.useSchema) {
                coord.catalog = this.catalog;
                coord.schema = catOrSch ? catOrSch : this.schema;
            } else if (cfg.useCatalog && !cfg.useSchema) {
                coord.catalog = catOrSch ? catOrSch : this.catalog;
                coord.schema = null;
            } else if (!cfg.useCatalog && cfg.useSchema) {
                coord.catalog = null;
                coord.schema = catOrSch ? catOrSch : this.schema;
            }
        }

        return tableCoordinators;
    }

    /**
     * 从表的坐标中获取它的父级节点的名字。
     *
     * @param tableCoordinator 表的坐标。
     */
    private getTableParentName(tableCoordinator: TableCoordinator): string {
        const cfg: DatabaseConfig = this.dbConfig!;

        if (cfg.useCatalog && cfg.useSchema) {
            return tableCoordinator.schema!;
        } else if (cfg.useCatalog && !cfg.useSchema) {
            return tableCoordinator.catalog!;
        } else if (!cfg.useCatalog && cfg.useSchema) {
            return tableCoordinator.schema!;
        }

        return '';
    }

    /**
     * 获取表的列。
     *
     * @param tableCoordinator 表的坐标。
     * @returns 返回表的列可补全的结果。
     */
    private findTableColumns(tableCoordinator: TableCoordinator): CompletionResult {
        /*
        逻辑:
        1. 查找表的父节点下的所有表。
        2. 找到坐标对应的表。
        3. 根据表的状态分别处理。
        */

        // [1] 查找表的父节点下的所有表。
        const tableParentName: string = this.getTableParentName(tableCoordinator);
        const [tables, loadState]: CompletionResult = this.findTables(tableParentName);

        if (loadState === LOAD_STATE_INIT || loadState === LOAD_STATE_LOADING) {
            return [[], loadState];
        }

        // [2] 找到坐标对应的表。
        const table: DatabaseObject | null = tables.find(tv => tv.name === tableCoordinator.table) || null;

        // [3] 根据表的状态分别处理。
        if (!table) {
            return COMPLETION_RESULT_SUCCESS_EMPTY;
        } else if (table.loadState === LOAD_STATE_INIT) {
            return COMPLETION_RESULT_INIT;
        } else if (table.loadState === LOAD_STATE_LOADING) {
            return COMPLETION_RESULT_LOADING;
        } else {
            return [table.children, LOAD_STATE_SUCCESS];
        }
    }

    /**
     * 获取多个表可补全的列。
     *
     * @param tableCoordinators 多个表的坐标。
     * @returns 返回多个表可补全的列。
     */
    private findTableColumnsList(tableCoordinators: TableCoordinator[]): CompletionResult {
        /*
        逻辑:
        1. 逐个获取表的列，把可补全的内容收集起来。
        2. 只要有一个表的列是未加载的就需要请求加载。
        */

        // 综合的的加载状态。
        let resultLoadState: number = LOAD_STATE_SUCCESS;

        // 可补全的列。
        const resultColumns: DatabaseObject[] = [];

        for (const coord of tableCoordinators) {
            // [1] 逐个获取表的列，把可补全的内容收集起来。
            const [columns, loadState] = this.findTableColumns(coord);
            resultColumns.push(...columns);

            // [2] 只要有一个表的列是未加载的就需要请求加载。
            if (loadState === LOAD_STATE_INIT) {
                resultLoadState = LOAD_STATE_INIT;
            }
        }

        return [resultColumns, resultLoadState];
    }
}
