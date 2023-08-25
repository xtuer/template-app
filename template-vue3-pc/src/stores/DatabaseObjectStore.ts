import { computed } from 'vue';
import { defineStore } from 'pinia'
import DatabaseMetadataApi from '@/static/api/DatabaseMetadataApi';
import type { DatabaseObject, DatabaseConfig } from '@/static/types/DatabaseObjectTreeTypes';
import { LoadingError, CATALOG, SCHEMA, TABLE, VIEW, COLUMN, LOAD_STATE_INIT, LOAD_STATE_LOADING, LOAD_STATE_SUCCESS } from '@/static/types'
import type { DatabaseType, DatabaseInstance, NameTypePair, TableCoordinator, TableColumn, TableColumns } from '@/static/types';

/**
 * 数据库对象 Store。
 */
export const useDatabaseObjectStore = defineStore('database-object-store', () => {
    // 数据库的配置。
    const dbConfigs: DatabaseConfig[] = [];

    // 数据库实例 Map: key 为数据库类型，value 为数据库实例数组。
    const dbInstances: Map<DatabaseType, DatabaseInstance[]> = new Map();

    /*
    保存数据库实例的数据库对象 Map: key 为 <databaseType>-<dbid>，value 为数据库对象的数组。

    存储结构 (根节点 root 引入可以使得各种请求的逻辑更统一):
    databaseType-dbid:  ├── root
                        │   ├── catalog
                        │   │   ├── schema
                        │   │   │   ├── table
                        │   │   │   │   └── column
                        │   │   │   ├── view
                        │   │   │   │   └── column
                        │   │   │   ├── procedure
                        │   │   │   ├── function
                        │   │   │   └── synonym

    databaseType-dbid:  ├── root
                        │   ├── catalog
                        │   │   ├── table
                        │   │   │   └── column
                        │   │   ├── view
                        │   │   │   └── column

    databaseType-dbid:  ├── root
                        │   ├── schema
                        │   │   ├── table
                        │   │   │   └── column
                        │   │   ├── view
                        │   │   │   └── column
    */
    const dbObjects: Map<string, DatabaseObject> = new Map();

    // 只是为了用于在 Chrome 的 Vue > Pinia 插件里观察数据。
    const data = computed(() => dbObjects);

    /**
     * 构建 Map dbObjects 的 key: <databaseType>-<dbid>
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @returns 返回 Map 的 key。
     */
    function buildDbObjectKey(type: DatabaseType, dbid: number): string {
        return `${type}-${dbid}`;
    }

    /**
     * 获取数据库元数据配置。
     *
     * @returns 返回 Promise 的 resolve() 的参数为获取数据库元数据配置的数组，reject() 的参数为错误原因。
     */
    async function findDatabaseMetadataConfigs(): Promise<DatabaseConfig[]> {
        if (dbConfigs.length > 0) {
            return dbConfigs;
        }

        const configs = await DatabaseMetadataApi.findDatabaseMetadataConfigs();
        dbConfigs.push(...configs.map(cfg => ({
            type          : cfg.type,
            label         : cfg.label,
            useCatalog    : cfg.useCatalog,
            useSchema     : cfg.useSchema,
            useWebTerminal: cfg.useWebTerminal,
            useProcedure  : cfg.useProcedure,
            useFunction   : cfg.useFunction,
        } as DatabaseConfig)));

        return dbConfigs;
    }

    /**
     * 获取传入类型的数据库的元数据配置。
     *
     * @param type 数据库类型。
     * @returns 返回查找到的数据库配置，如果查找不到返回 null
     */
    function findDatabaseConfig(type: DatabaseType): DatabaseConfig | null {
        // 提示: 由于这个函数应该是在数据库配置从服务器加载后才会被调用，所以就不需要返回 promise。
        if (dbConfigs) {
            return dbConfigs.find(cfg => cfg.type === type) || null;
        } else {
            return null;
        }
    }

    /**
     * 获取传入的数据库类型的数据库实例，例如所有 MySQL 数据库的实例。
     *
     * @param type 数据库类型。
     * @returns 返回 Promise 的 resolve() 的参数为数据库实例的数组，reject() 的参数为错误原因。
     */
    async function findDatabaseInstances(type: DatabaseType): Promise<DatabaseInstance[]> {
        if (dbInstances.has(type)) {
            return Promise.resolve(dbInstances.get(type)!);
        }

        const dbids = await DatabaseMetadataApi.findDatabaseInstances(type);
        const instances: DatabaseInstance[] = dbids.map(dbid => ({
            type, dbid
        }));
        dbInstances.set(type, instances);

        return Promise.resolve(instances);
    }

    /**
     * 查找 catalog 的父节点。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @returns 返回 catalog 的父节点 (虚拟 root 节点)。
     */
    function findCatalogParent(type: DatabaseType, dbid: number): DatabaseObject {
        /*
        逻辑:
        1. 构建 dbObject 的 key。
        2. 如果  dbObjects 中还没有 key 的记录则创建一个虚拟根节点作为 catalog 的父节点。
        3. 返回 catalog 的父节点。
        */
        const key: string = buildDbObjectKey(type, dbid);

        // [2] 如果  dbObjects 中还没有 key 的记录则创建一个虚拟根节点作为 catalog 的父节点。
        if (!dbObjects.has(key)) {
            const catalogParent: DatabaseObject = {
                type: 'root',
                name: 'catalog-parent',
                loadState: LOAD_STATE_INIT,
                children: [],
            }
            dbObjects.set(key, catalogParent)
        }

        // [3] 返回 catalog 的父节点。
        const catalogParent = dbObjects.get(key)!;
        return catalogParent;
    }

    /**
     * 获取数据库的 catalogs。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @returns 返回 Promise 的 resolve() 的参数为 catalog 的对象数组，reject() 的参数为错误原因。
     */
    async function findCatalogs(type: DatabaseType, dbid: number): Promise<DatabaseObject[]> {
        /*
        逻辑:
        1. 查找 catalog 的父节点 catalogParent。
        2. 如果 catalogParent 加载过子节点则返回其 children 即 catalogs。
        3. 如果 catalogParent 正在加载子节点中则 reject 提示加载中。
        4. 如果 catalogParent 没有加载过子节点，则加载其子节点即 catalogs。
        5. 缓存从服务器获取到的 catalogs。
        */

        // [1] 查找 catalog 的父节点 catalogParent。
        const catalogParent: DatabaseObject = findCatalogParent(type, dbid);

        if (catalogParent.loadState === LOAD_STATE_SUCCESS) {
            // [2] 如果 catalogParent 加载过子节点则返回其 children 即 catalogs。
            return Promise.resolve(catalogParent.children);
        } else if (catalogParent.loadState === LOAD_STATE_LOADING) {
            // [3] 如果 catalogParent 正在加载子节点中则 reject 提示加载中。
            return Promise.reject(new LoadingError(`Catalog 加载中: dbid [${dbid}]`));
        } else {
            catalogParent.loadState = LOAD_STATE_LOADING;
        }

        // [4] 如果 catalogParent 没有加载过子节点，则加载其子节点即 catalogs。
        let catalogNames: string[];
        try {
            catalogNames = await DatabaseMetadataApi.findCatalogNames(type, dbid);
        } catch (error) {
            // 请求发生异常时加载失败，重置加载状态为 LOAD_STATE_INIT。
            catalogParent.loadState = LOAD_STATE_INIT;
            return Promise.reject(error);
        }

        const catalogs: DatabaseObject[] = catalogNames.map(name => ({
            type     : CATALOG,
            name     : name,
            loadState: LOAD_STATE_INIT,
            children : [],
        }));

        // [5] 缓存从服务器获取到的 catalogs。
        catalogParent.children = catalogs;
        catalogParent.loadState = LOAD_STATE_SUCCESS;

        return Promise.resolve(catalogs);
    }

    /**
     * 查找 schema 的父节点。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 所属 catalog (可能为空)。
     * @returns 返回 schema 的父节点 (可能是 catalog，也可能是虚拟 root 节点)。
     */
    async function findSchemaParent(type: DatabaseType, dbid: number, catalog: string | null): Promise<DatabaseObject | null> {
        /*
        逻辑:
        1. 如果 catalog 不为空则表示 schema 的父节点是 catalog，查找并返回。
        2. 如果 catalog 为空，则说明 schema 的父节点为 dbObjects 的虚拟根节点，
        3. 构建 dbObject 的 key。
        4. 如果 dbObjects 中还没有 key 的记录则创建一个虚拟根节点作为 schema 的父节点。
        5. 返回 schema 的父节点。
        */
        if (catalog) {
            // [1] 如果 catalog 不为空则表示 schema 的父节点是 catalog，查找并返回。
            const catalogObj: DatabaseObject | undefined = (await findCatalogs(type, dbid)).find(cat => cat.name === catalog);
            return Promise.resolve(catalogObj || null);
        }

        // [2] 如果 catalog 为空，则说明 schema 的父节点为 dbObjects 的虚拟根节点，
        // [3] 构建 dbObject 的 key。
        const key: string = buildDbObjectKey(type, dbid);

        // [4] 如果 dbObjects 中还没有 key 的记录则创建一个虚拟根节点作为 schema 的父节点。
        if (!dbObjects.has(key)) {
            const schemaParent: DatabaseObject = {
                type: 'root',
                name: 'schema-parent',
                loadState: LOAD_STATE_INIT,
                children: [],
            }
            dbObjects.set(key, schemaParent)
        }

        // [5] 返回 schema 的父节点。
        const schemaParent = dbObjects.get(key)!;

        return Promise.resolve(schemaParent);
    }

    /**
     * 获取数据的 schemas。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog Schema 所属 catalog。
     * @returns 返回 Promise 的 resolve() 的参数为 schema 的对象数组，reject() 的参数为错误原因。
     */
    async function findSchemas(type: DatabaseType, dbid: number, catalog: string | null): Promise<DatabaseObject[]> {
        /*
        逻辑:
        1. 查找 schema 的父节点 schemaParent。
        2. 如果 schemaParent 不存在则返回。
        3. 如果 schemaParent 加载过子节点则返回其 children 即 schemas。
        4. 如果 schemaParent 正在加载子节点中则 reject 提示加载中。
        5. 如果 schemaParent 没有加载过子节点，则加载其子节点即 schemas。
        6. 缓存从服务器获取到的 schemas。
        */

        // [1] 查找 schema 的父节点 schemaParent。
        const schemaParent: DatabaseObject | null = await findSchemaParent(type, dbid, catalog);

        // [2] 如果 schemaParent 不存在则返回。
        if (!schemaParent) {
            console.warn(`[findSchemas] Catalog 不存在: dbid [${dbid}], catalog [${catalog}]`);
            return Promise.resolve([]);
        }

        if (schemaParent.loadState === LOAD_STATE_SUCCESS) {
            // [3] 如果 schemaParent 加载过子节点则返回其 children 即 schemas。
            return Promise.resolve(schemaParent.children);
        } else if (schemaParent.loadState === LOAD_STATE_LOADING) {
            // [4] 如果 schemaParent 正在加载子节点中则 reject 提示加载中。
            return Promise.reject(new LoadingError(`Schema 加载中: dbid [${dbid}], catalog: [${catalog}]`));
        } else {
            schemaParent.loadState = LOAD_STATE_LOADING;
        }

        // [5] 如果 schemaParent 没有加载过子节点，则加载其子节点即 schemas。
        let schemaNames: string[];
        try {
            schemaNames = await DatabaseMetadataApi.findSchemaNames(type, dbid, catalog);
        } catch (error) {
            // 请求发生异常时加载失败，重置加载状态为 LOAD_STATE_INIT。
            schemaParent.loadState = LOAD_STATE_INIT;
            return Promise.reject(error);
        }

        const schemas: DatabaseObject[] = schemaNames.map(name => ({
            type     : SCHEMA,
            name     : name,
            loadState: LOAD_STATE_INIT,
            children : [],
        }));

        // [6] 缓存从服务器获取到的 schemas。
        schemaParent.children = schemas;
        schemaParent.loadState = LOAD_STATE_SUCCESS; // 表示 catalog 已经加载过 schema。

        return Promise.resolve(schemas);
    }

    /**
     * 查找表的父节点。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 所属 catalog (可能为空)。
     * @param schema 所属 schema (可能为空)。
     * @returns 返回 table 的父节点 (可能是 catalog，也可能是 schema)。
     */
    async function findTableParent(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null): Promise<DatabaseObject | null> {
        /*
        逻辑: schema 不为空则父节点为 schema，其他情况父节点为 catalog:
        - catalog > schema > table
        - catalog > table
        - schema > table
        */

        let tableParent: DatabaseObject | null;

        if (schema) {
            tableParent = (await findSchemas(type, dbid, catalog)).find(sch => sch.name === schema) || null;
        } else {
            tableParent = (await findCatalogs(type, dbid)).find(cat => cat.name === catalog) || null;
        }

        return tableParent;
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 tables and views。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @returns 返回 Promise 的 resolve() 的参数为 tables and views 的对象数组，reject() 的参数为错误原因。
     */
    async function findTablesAndViews(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null): Promise<DatabaseObject[]> {
        /*
        逻辑:
        1. 查找 table 的父节点 tableParent。
        2. 如果 tableParent 不存在则返回。
        3. 如果 tableParent 加载过子节点则返回其 children 即 tables。
        4. 如果 tableParent 正在加载子节点中则 reject 提示加载中。
        5. 如果 tableParent 没有加载过子节点，则加载其子节点即 tables。
        6. 缓存从服务器获取到的 tables。
        */

        // [1] 查找 table 的父节点 tableParent。
        const tableParent: DatabaseObject | null = await findTableParent(type, dbid, catalog, schema);

        // [2] 如果 tableParent 不存在则返回。
        if (!tableParent) {
            console.warn(`[findTablesAndViews] Table 不存在: dbid [${dbid}], catalog [${catalog}], schema [${schema}]`);
            return Promise.resolve([]);
        }

        if (tableParent.loadState === LOAD_STATE_SUCCESS) {
            // [3] 如果 tableParent 加载过子节点则返回其 children 即 tables。
            const tablesAndViews: DatabaseObject[] = tableParent.children.filter(child => child.type === TABLE || child.type === VIEW);
            return Promise.resolve(tablesAndViews);
        } else if (tableParent.loadState === LOAD_STATE_LOADING) {
            // [4] 如果 tableParent 正在加载子节点中则 reject 提示加载中。
            return Promise.reject(new LoadingError(`Tables 加载中: dbid [${dbid}], catalog [${catalog}], schema [${schema}]`));
        } else {
            tableParent.loadState = LOAD_STATE_LOADING;
        }

        // [5] 如果 tableParent 没有加载过子节点，则加载其子节点即 tables。
        let tableAndViewNames: NameTypePair[];
        try {
            tableAndViewNames = await DatabaseMetadataApi.findTableAndViewNames(type, dbid, catalog, schema);
        } catch (error) {
            // 请求发生异常时加载失败，重置加载状态为 LOAD_STATE_INIT。
            tableParent.loadState = LOAD_STATE_INIT;
            return Promise.reject(error);
        }

        const tablesAndViews: DatabaseObject[] = tableAndViewNames.map(tv => ({
            type     : tv.type === TABLE ? TABLE: VIEW,
            name     : tv.name,
            loadState: LOAD_STATE_INIT,
            children : [],
        }));

        // [6] 缓存从服务器获取到的 tables。
        tableParent.children = tablesAndViews;
        tableParent.loadState = LOAD_STATE_SUCCESS;

        return Promise.resolve(tablesAndViews);
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 tables。
     */
    async function findTables(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null): Promise<DatabaseObject[]> {
        const tablesAndViews = await findTablesAndViews(type, dbid, catalog, schema);
        const tables = tablesAndViews.filter(tv => tv.type === TABLE);
        return Promise.resolve(tables);
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 views。
     */
    async function findViews(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null): Promise<DatabaseObject[]> {
        const tablesAndViews = await findTablesAndViews(type, dbid, catalog, schema);
        const views = tablesAndViews.filter(tv => tv.type === VIEW);
        return Promise.resolve(views);
    }

    /**
     * 获取多个表的列。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param tableCoordinators 定位表的坐标数组。
     * @returns 返回 Promise 的 resolve() 的参数为表的列的对象数组，reject() 的参数为错误原因。
     */
    async function findTableColumnsList(type: DatabaseType, dbid: number, tableCoordinators: TableCoordinator[]): Promise<TableColumns[]> {
        /*
        逻辑:
        1. 收集需要加载列的表 (可跨 schema 同时加载，同一个 schema 下的表只会向服务器发一次请求，因为 store 里已经做了缓存)。
        2. 从服务器获取需要加载的表的列。
        3. 从缓存中获取表的列。
        */

        // 需要加载列的表。
        const loadingTables: DatabaseObject[] = [];
        const tableCoordinatorsOfNeedLoadingColumns: TableCoordinator[] = [];

        // [1] 收集需要加载列的表。
        for (const coord of tableCoordinators) {
            // 可跨 schema 同时加载，同一个 schema 下的表只会向服务器发一次请求，因为 store 里已经做了缓存。
            const tables = await findTablesAndViews(type, dbid, coord.catalog, coord.schema);
            const table: DatabaseObject | undefined = tables.find(t => t.name === coord.table);

            // 表存在但没有加载过列。
            if (table?.loadState === LOAD_STATE_INIT) {
                table.loadState = LOAD_STATE_LOADING;
                loadingTables.push(table);
                tableCoordinatorsOfNeedLoadingColumns.push(coord);
            }
        }

        // [2] 从服务器获取需要加载的表的列。
        if (tableCoordinatorsOfNeedLoadingColumns.length > 0) {
            let tableColumnsList: TableColumns[];
            try {
                tableColumnsList = await DatabaseMetadataApi.findTablesColumns(type, dbid, tableCoordinatorsOfNeedLoadingColumns);
            } catch (error) {
                // 请求发生异常时加载失败，重置加载状态为 LOAD_STATE_INIT。
                for (const table of loadingTables) {
                    table.loadState = LOAD_STATE_INIT;
                }
                return Promise.reject(error);
            }

            // 把获取到的列放入对应的表中 (findTablesAndViews() 不会重复从服务器加载表，因为前面已经加载并缓存过了)。
            for (const tableColumns of tableColumnsList) {
                const tables = await findTablesAndViews(type, dbid, tableColumns.catalog, tableColumns.schema);
                const table: DatabaseObject | undefined = tables.find(t => t.name === tableColumns.table);

                if (!table) {
                    continue;
                }

                const columns: DatabaseObject[] = tableColumns.columns.map(column => ({
                    type     : COLUMN,
                    name     : column.name,
                    loadState: LOAD_STATE_INIT,
                    typeName : column.typeName,
                    children : [],
                }));

                table.children = columns;
                table.loadState = LOAD_STATE_SUCCESS;
            }
        }

        // [3] 从缓存中获取表的列。
        const tableColumnsList: TableColumns[] = [];

        for (const coord of tableCoordinators) {
            const tables = await findTablesAndViews(type, dbid, coord.catalog, coord.schema);
            const table: DatabaseObject | undefined = tables.find(t => t.name === coord.table);

            if (table?.loadState === LOAD_STATE_SUCCESS) {
                const columns: TableColumn[] = table.children.map(col => ({ name: col.name, typeName: col.typeName! }));
                const tableColumns: TableColumns = {
                    catalog: coord.catalog,
                    schema : coord.schema,
                    table  : coord.table,
                    columns: columns,
                }

                tableColumnsList.push(tableColumns);
            }
        }

        return Promise.resolve(tableColumnsList);
    }

    /**
     * 获取指定表的列 (视图和表的列都是用这个函数获取)。
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param tableCoordinator 表的坐标。
     * @returns 返回 Promise 的 resolve() 的参数为表的列，表不存在时返回 null。
     */
    async function findTableColumns(type: DatabaseType, dbid: number, tableCoordinator: TableCoordinator): Promise<TableColumns | null> {
        const tableColumnsList: TableColumns[] = await findTableColumnsList(type, dbid, [tableCoordinator]);

        for (const tableColumns of tableColumnsList) {
            if (tableColumns.catalog === tableCoordinator.catalog
                && tableColumns.schema === tableCoordinator.schema
                && tableColumns.table === tableCoordinator.table) {

                return Promise.resolve(tableColumns);
            }
        }

        return Promise.resolve(null);
    }

    /**
     * 删除指定类型的数据库实例。
     *
     * @param type 数据库类型。
     */
    function removeDatabaseInstances(type: DatabaseType): void {
        dbInstances.delete(type);
    }

    /**
     * 删除指定数据库的数据。
     *
     * @param dbid 数据库 ID。
     */
    function removeDataOfDatabaseInstance(type: DatabaseType, dbid: number): void {
        dbObjects.delete(buildDbObjectKey(type, dbid));
    }

    /**
     * 删除指定数据库 pathElements 路径对应对象的 children。
     *
     * @param dbid 数据库 ID。
     * @param pathElements 数据库对象的路径: (catalog | schema | catalog -> schema) -> (table | view | procedure | function)。
     */
    function removeChildrenOfDatabaseObject(type: DatabaseType, dbid: number, pathElements: NameTypePair[]): void {
        if (!dbObjects.has(buildDbObjectKey(type, dbid))) {
            return;
        }

        // 找到 root 节点。
        let parent: DatabaseObject | null = dbObjects.get(buildDbObjectKey(type, dbid))!;
        let target: DatabaseObject | null = null;

        // 从上往下搜索，直到匹配到路径最后一个元素。
        for (const elem of pathElements) {
            target = parent?.children.find(child => child.type === elem.type && child.name === elem.name) || null;
            parent = target;

            if (!target) {
                return;
            }
        }

        // 清空 target 的 children。
        if (target) {
            target.children = [];
            target.loadState = LOAD_STATE_INIT;
        }
    }

    return {
        data,
        dbObjects,
        buildDbObjectKey,
        findDatabaseMetadataConfigs,
        findDatabaseConfig,
        findDatabaseInstances,
        findCatalogs,
        findSchemas,
        findTablesAndViews,
        findTables,
        findViews,
        findTableColumns,
        findTableColumnsList,
        removeDatabaseInstances,
        removeDataOfDatabaseInstance,
        removeChildrenOfDatabaseObject,
    };
})
