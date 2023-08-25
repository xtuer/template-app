import { Rest } from '@/static/ts/axios.rest';
import { Urls } from '@/static/ts/urls';
import type { DatabaseType, NameTypePair, TableColumn, TableColumnFull, TableColumns, TableCoordinator } from '@/static/types';

/**
 * 获取数据库元数据的 API。
 */
export default class DatabaseMetadataApi {
    /**
     * 获取数据库元数据配置。
     *
     * 网址: http://localhost:8080/api/dsc/databases/configs
     * 参数: 无
     * 测试: curl http://localhost:8080/api/dsc/databases/configs
     *
     * @returns {Promise} 返回 Promise 对象，resolve 的参数为数据库元数据配置的数组，reject 的参数为错误信息。
     * @returns 返回 Promise 的 resolve() 的参数为数据库元数据配置的数组，reject() 的参数为错误原因。
     */
    static async findDatabaseMetadataConfigs(): Promise<Array<{[key: string]: any}>> {
        return Rest.url(Urls.API_DATABASE_CONFIGS)
            .get<Array<{[key: string]: any}>>()
            .then(({ data: config, success, message }) => {
                return Rest.normalize({ data: config, success, message });
            });
    }

    /**
     * 获取测试的数据库信息。
     *
     * 测试: curl 'http://localhost:8080/api/moc/dsc/databases/instances?type=MYSQL'
     *
     * @param type 数据库类型。
     * @returns 返回 Promise 的 resolve() 的参数为数据库实例 DBID 的数组，reject() 的参数为错误原因。
     */
    static async findDatabaseInstances(type: DatabaseType): Promise<number[]> {
        return Rest.url(Urls.API_MOC_DATABASE_INSTANCES)
            .data({ type })
            .get<number[]>()
            .then(({ data: dbids, success, message }) => {
                return Rest.normalize({ data: dbids, success, message });
            });
    }

    /**
     * 获取数据库的 catalog 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/catalogNames
     * 参数:
     *     type (必要): 数据库类型。
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/catalogNames?type=MYSQL'
     *     curl 'http://localhost:8080/api/dsc/databases/3/catalogNames?type=POSTGRES'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @returns 返回 Promise 的 resolve() 的参数为 catalog 名字的数组，reject() 的参数为错误原因。
     */
    static async findCatalogNames(type: DatabaseType, dbid: number): Promise<string[]> {
        return Rest.url(Urls.API_DATABASE_CATALOG_NAMES)
            .params({ dbid })
            .data({ type })
            .get<string[]>()
            .then(({ data: catalogNames, message, success }) => {
                return Rest.normalize({ data: catalogNames, success, message });
            });
    }

    /**
     * 获取数据库的 schema 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/schemaNames
     * 参数:
     *     type    (必要): 数据库类型。
     *     catalog [可选]: Schema 所属 catalog。
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/2/schemaNames?type=ORACLE'
     *     curl 'http://localhost:8080/api/dsc/databases/3/schemaNames?type=POSTGRES&catalog=postgres'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog Schema 所属 catalog。
     * @returns 返回 Promise 的 resolve() 的参数为 schema 名字的数组，reject() 的参数为错误原因。
     */
    static async findSchemaNames(type: DatabaseType, dbid: number, catalog: string | null): Promise<string[]> {
        return Rest.url(Urls.API_DATABASE_SCHEMA_NAMES)
            .params({ dbid })
            .data({ type, catalog })
            .get<string[]>()
            .then(({ data: catalogNames, message, success }) => {
                return Rest.normalize({ data: catalogNames, success, message });
            });
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 table 和 view 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tableViewNames
     * 参数:
     *     type    (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tableViewNames?type=MYSQL&catalog=meta_test_catalog'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @returns 返回 Promise 的 resolve() 的参数为 name+type 对的数组 (type 为 table 表示表，type 为 view 表示视图)，reject() 的参数为错误原因。
     */
    static async findTableAndViewNames(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null): Promise<NameTypePair[]> {
        return Rest.url(Urls.API_DATABASE_TABLE_VIEW_NAMES)
            .params({ dbid })
            .data({ type, catalog, schema })
            .get<NameTypePair[]>()
            .then(({ data: tableViewNames, success, message }) => {
                return Rest.normalize({ data: tableViewNames, success, message })
            });
    }

    /**
     * 获取数据库的表的列。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tableColumns
     * 参数:
     *    type    (必要): 数据库类型
     *    catalog [可选]: 根据数据库而定
     *    schema  [可选]: 根据数据库而定
     *    table   (必要): 表名
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tableColumns?type=MYSQL&catalog=meta_test_catalog&table=meta_test_table'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @param table 表名。
     * @return 返回 Promise 的 resolve() 的参数为表的列的数组，reject() 的参数为错误原因。
     */
    static async findTableColumns(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null, table: string): Promise<Array<TableColumnFull>> {
        return Rest.url(Urls.API_DATABASE_TABLE_COLUMNS)
            .params({ dbid })
            .data({ type, catalog, schema, table })
            .get<Array<TableColumnFull>>()
            .then(({ data: columns, success, message }) => {
                return Rest.normalize({ data: columns, success, message });
            });
    }

    /**
     * 获取多个表的列。
     *
     * 提示: 本应该使用 GET 执行获取请求，但是因为参数比较复杂需要放到请求体里才好处理，GET 实现起来很麻烦，于是使用 POST 来处理。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tablesColumns
     * 参数: type (必要): 数据库类型
     * 请求体:
     *    [{
     *        catalog [可选]: 根据数据库而定
     *        schema  [可选]: 根据数据库而定
     *        table   (必要): 表名
     *    }]
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tablesColumns?type=MYSQL'
     *          --data '[{"catalog": "test", "schema": null, "table": "sp_test"}]' --header 'Content-Type: application/json'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param tableCoordinators 表的坐标，由 catalog+schema+table 组成。
     * @returns 返回 Promise 的 resolve() 的参数为 TableColumns 的数组，reject() 的参数为错误原因。
     */
    static async findTablesColumns(type: DatabaseType, dbid: number, tableCoordinators: TableCoordinator[]): Promise<TableColumns[]> {
        return Rest.url(Urls.API_DATABASE_TABLES_COLUMNS)
            .params({ dbid, databaseType: type })
            .data(tableCoordinators)
            .useRequestBody()
            .create<TableColumns[]>()
            .then(({ data: nameAndTypes, success, message }) => {
                return Rest.normalize({ data: nameAndTypes, success, message });
            });
    }

    /**
     * 获取数据库的视图的列。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/viewColumns
     * 参数:
     *    type    (必要): 数据库类型
     *    catalog [可选]: 根据数据库而定
     *    schema  [可选]: 根据数据库而定
     *    view    (必要): 视图名
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/viewColumns?type=MYSQL&catalog=meta_test_catalog&view=meta_test_table'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @param view 视图名。
     * @return 返回 Promise 的 resolve() 的参数为视图的列的数组，reject() 的参数为错误原因。
     */
    static async findViewColumns(type: DatabaseType, dbid: number, catalog: string | null, schema: string | null, view: string): Promise<Array<TableColumn>> {
        return Rest.url(Urls.API_DATABASE_VIEW_COLUMNS)
            .params({ dbid })
            .data({ type, catalog, schema, view })
            .get<Array<TableColumn>>()
            .then(({ data: columns, success, message }) => {
                return Rest.normalize({ data: columns, success, message });
            });
    }
}
