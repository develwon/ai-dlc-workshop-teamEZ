const STORAGE_KEYS = {
    TOKEN: 'table_token',
    STORE_ID: 'table_store_id',
    TABLE_ID: 'table_table_id',
    TABLE_NUMBER: 'table_table_number',
    SESSION_ID: 'table_session_id',
    STORE_CODE: 'table_store_code',
    PASSWORD: 'table_password'
};

export function saveLoginInfo(data, storeCode, tableNumber, password) {
    localStorage.setItem(STORAGE_KEYS.TOKEN, data.token);
    localStorage.setItem(STORAGE_KEYS.STORE_ID, data.storeId);
    localStorage.setItem(STORAGE_KEYS.TABLE_ID, data.tableId);
    localStorage.setItem(STORAGE_KEYS.TABLE_NUMBER, tableNumber);
    if (data.sessionId) {
        localStorage.setItem(STORAGE_KEYS.SESSION_ID, data.sessionId);
    }
    // 자동 로그인을 위해 저장
    localStorage.setItem(STORAGE_KEYS.STORE_CODE, storeCode);
    localStorage.setItem(STORAGE_KEYS.PASSWORD, password);
}

export function getAuthInfo() {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (!token) return null;

    return {
        token,
        storeId: Number(localStorage.getItem(STORAGE_KEYS.STORE_ID)),
        tableId: Number(localStorage.getItem(STORAGE_KEYS.TABLE_ID)),
        tableNumber: Number(localStorage.getItem(STORAGE_KEYS.TABLE_NUMBER)),
        sessionId: localStorage.getItem(STORAGE_KEYS.SESSION_ID)
            ? Number(localStorage.getItem(STORAGE_KEYS.SESSION_ID))
            : null
    };
}

export function getSavedCredentials() {
    const storeCode = localStorage.getItem(STORAGE_KEYS.STORE_CODE);
    const tableNumber = localStorage.getItem(STORAGE_KEYS.TABLE_NUMBER);
    const password = localStorage.getItem(STORAGE_KEYS.PASSWORD);
    if (storeCode && tableNumber && password) {
        return { storeCode, tableNumber: Number(tableNumber), password };
    }
    return null;
}

export function updateSessionId(sessionId) {
    localStorage.setItem(STORAGE_KEYS.SESSION_ID, sessionId);
}

export function clearAuth() {
    Object.values(STORAGE_KEYS).forEach(key => localStorage.removeItem(key));
}

export function isLoggedIn() {
    return !!localStorage.getItem(STORAGE_KEYS.TOKEN);
}
