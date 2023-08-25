// URL 集中管理。
const Urls = {
    API_DEMO_USERS_NAME          : '/api/demo/users/{userId}/username',          // 演示用的 API。
    API_DATABASE_CONFIGS         : '/api/dsc/databases/configs',                 // 数据库元数据配置。
    API_DATABASE_CATALOG_NAMES   : '/api/dsc/databases/{dbid}/catalogNames',     // 数据库的 catalog。
    API_DATABASE_SCHEMA_NAMES    : '/api/dsc/databases/{dbid}/schemaNames',      // 数据库的 schema。
    API_DATABASE_TABLE_VIEW_NAMES: '/api/dsc/databases/{dbid}/tableViewNames',   // 数据库的表和视图。
    API_DATABASE_PROCEDURE_NAMES : '/api/dsc/databases/{dbid}/procedureNames',   // 存储过程的名字。
    API_DATABASE_FUNCTION_NAMES  : '/api/dsc/databases/{dbid}/functionNames',    // 存储函数的名字。
    API_DATABASE_TABLE_COLUMNS   : '/api/dsc/databases/{dbid}/tableColumns',     // 数据库表的列。
    API_DATABASE_TABLES_COLUMNS  : '/api/dsc/databases/{dbid}/tablesColumns?type={databaseType}', // 数据库多个表的列。
    API_DATABASE_VIEW_COLUMNS    : '/api/dsc/databases/{dbid}/viewColumns',      // 数据库视图的列。
    API_DATABASE_TABLE_DDLS      : '/api/dsc/databases/{dbid}/tableDdls',        // 表的创建语句。
    API_DATABASE_VIEW_DDLS       : '/api/dsc/databases/{dbid}/viewDdls',         // 视图的创建语句。
    API_DATABASE_PROCEDURE_DDLS  : '/api/dsc/databases/{dbid}/procedureDdls',    // 存储过程的创建语句。
    API_DATABASE_FUNCTION_DDLS   : '/api/dsc/databases/{dbid}/functionDdls',     // 存储函数的创建语句。

    API_MOC_DATABASE_INSTANCES: '/api/moc/dsc/databases/instances', // 获取模拟的数据库实例。
};

export { Urls };
