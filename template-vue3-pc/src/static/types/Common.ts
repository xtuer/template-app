interface NameTypePair {
    name: string;
    type: string;
}

/**
 * 加载中错误。
 */
class LoadingError extends Error {
    constructor(reason: string) {
        super(reason)
    }
}

/**
 * 加载状态。
 */
const LOAD_STATE_INIT    = 0; // 初始化。
const LOAD_STATE_LOADING = 1; // 加载中。
const LOAD_STATE_SUCCESS = 2; // 加载完成。

export {
    LoadingError,
    LOAD_STATE_INIT, LOAD_STATE_LOADING, LOAD_STATE_SUCCESS
}

export type { NameTypePair }
