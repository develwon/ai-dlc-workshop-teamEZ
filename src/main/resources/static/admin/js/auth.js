const KEYS = {
    TOKEN: 'admin_token',
    STORE_NAME: 'admin_store_name',
    ROLE: 'admin_role'
};

export function saveAuth(data) {
    localStorage.setItem(KEYS.TOKEN, data.token);
    localStorage.setItem(KEYS.STORE_NAME, data.storeName || '');
    localStorage.setItem(KEYS.ROLE, data.role || '');
}

export function getAuth() {
    const token = localStorage.getItem(KEYS.TOKEN);
    if (!token) return null;
    return {
        token,
        storeName: localStorage.getItem(KEYS.STORE_NAME),
        role: localStorage.getItem(KEYS.ROLE)
    };
}

export function clearAuth() {
    Object.values(KEYS).forEach(k => localStorage.removeItem(k));
}

export function isLoggedIn() {
    return !!localStorage.getItem(KEYS.TOKEN);
}
