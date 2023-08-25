/**
 * 类型使用说明:
 * 1. 在各自类型文件里定义类型。
 * 2. 尽量在 Types.ts 中从各类型文件里导入类型集中管理，然后再导出 (注意处理冲突的类型)。
 * 3. 业务代码再从 Types.ts 中导入需要用到的类型。
 */

import { DATABASE_TYPES, CATALOG, SCHEMA, TABLE, VIEW, COLUMN } from '@/static/types/DatabaseTypes';
import type { DatabaseType, DatabaseInstance, TableColumn, TableColumns, TableColumnFull, TableCoordinator } from '@/static/types/DatabaseTypes';

import { LoadingError, LOAD_STATE_INIT, LOAD_STATE_LOADING, LOAD_STATE_SUCCESS } from './Common';
import type { NameTypePair } from './Common';

export {
    LoadingError,
    LOAD_STATE_INIT, LOAD_STATE_LOADING, LOAD_STATE_SUCCESS,
    DATABASE_TYPES, CATALOG, SCHEMA, TABLE, VIEW, COLUMN
}

export type {
    DatabaseType,
    DatabaseInstance,
    NameTypePair,
    TableColumn,
    TableColumnFull,
    TableColumns,
    TableCoordinator,
}
