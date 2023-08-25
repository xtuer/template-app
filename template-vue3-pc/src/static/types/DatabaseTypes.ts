/**
 * 数据库类型。
 */
type DatabaseType = 'MYSQL' | 'ORACLE' | 'POSTGRES' | 'DB2' | 'SQL_SERVER';

/**
 * 数据库类型集合。
 */
const DATABASE_TYPES = {
    MYSQL     : 'MYSQL',
    ORACLE    : 'ORACLE',
    POSTGRES  : 'POSTGRES',
    DB2       : 'DB2',
    SQL_SERVER: 'SQL_SERVER',
}

const CATALOG = 'catalog';
const SCHEMA  = 'schema';
const TABLE   = 'table';
const VIEW    = 'view';
const COLUMN  = 'column';

/**
 * 数据库实例。
 */
interface DatabaseInstance {
    type: DatabaseType; // 数据库类型。
    dbid: number;       // 数据库 ID。
}

/**
 * 表或者视图的列 (简要信息)。
 */
interface TableColumn {
    name    : string; // 列名
    typeName: string; // 列的数据类型名，例如 INT。
}

/**
 * 表或者视图的列 (简要信息)。
 */
interface TableColumns {
    catalog: string | null; // 表所属 catalog。
    schema : string | null; // 表所属 schema。
    table  : string;        // 表名。
    columns: TableColumn[]; // 列的数组。
}

/**
 * 表的坐标。
 */
interface TableCoordinator {
    catalog: string | null; // 表所属 catalog。
    schema : string | null; // 表所属 schema。
    table  : string;        // 表名。
    alias? : string;        // 表的别名。
}

/**
 * 表或者视图的列 (完整信息)。
 */
interface TableColumnFull {
    TABLE_CAT         : string | null;  // "gateway"
    TABLE_SCHEM       : string | null;  // null
    TABLE_NAME        : string;         // "WORKER_NODE"
    COLUMN_NAME       : string;         // "ID"
    DATA_TYPE         : number;         // -5
    TYPE_NAME         : string;         // "BIGINT"
    COLUMN_SIZE       : number;         // 19
    BUFFER_LENGTH     : number;         // 65535
    DECIMAL_DIGITS    : number;         // 0
    NUM_PREC_RADIX    : number;         // 10
    NULLABLE          : number;         // 0
    REMARKS           : string | null;  // "auto increment id"
    COLUMN_DEF        : string | null;  // null
    SQL_DATA_TYPE     : number;         // 0
    SQL_DATETIME_SUB  : number;         // 0
    CHAR_OCTET_LENGTH : number | null;  // null
    ORDINAL_POSITION  : number;         // 1
    IS_NULLABLE       : string;         // "NO"
    IS_AUTOINCREMENT  : string;         // "YES"
    IS_GENERATEDCOLUMN: string;         // "NO"
}

/**
 * 导出数据。
 */
export { DATABASE_TYPES, CATALOG, SCHEMA, TABLE, VIEW, COLUMN }
export type { DatabaseType, DatabaseInstance, TableColumn, TableColumns, TableColumnFull, TableCoordinator }
