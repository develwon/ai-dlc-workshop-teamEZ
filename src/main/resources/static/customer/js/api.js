const BASE_URL = '/api';

function getToken() {
    return localStorage.getItem('table_token');
}

async function request(url, options = {}) {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${BASE_URL}${url}`, { ...options, headers });

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: '요청 처리 중 오류가 발생했습니다.' }));
        throw new Error(error.message || `HTTP ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}

export async function login(storeCode, tableNumber, password) {
    return request('/tables/login', {
        method: 'POST',
        body: JSON.stringify({ storeCode, tableNumber, password })
    });
}

export async function getCategories(storeId) {
    return request(`/stores/${storeId}/categories`);
}

export async function getMenus(storeId, categoryId) {
    const query = categoryId ? `?categoryId=${categoryId}` : '';
    return request(`/stores/${storeId}/menus${query}`);
}

export async function createOrder(storeId, tableId, sessionId, items) {
    return request('/orders', {
        method: 'POST',
        body: JSON.stringify({ storeId, tableId, sessionId, items })
    });
}

export async function getTableOrders(tableId, sessionId) {
    const query = sessionId ? `?sessionId=${sessionId}` : '';
    return request(`/tables/${tableId}/orders${query}`);
}
