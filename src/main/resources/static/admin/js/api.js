const BASE_URL = '/api';

function getToken() {
    return localStorage.getItem('admin_token');
}

async function request(url, options = {}) {
    const token = getToken();
    const headers = { ...options.headers };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${BASE_URL}${url}`, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('admin_token');
        window.location.reload();
        return;
    }

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: '요청 처리 중 오류가 발생했습니다.' }));
        throw new Error(error.message || `HTTP ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}

// Auth
export async function login(storeCode, username, password) {
    return request('/admin/login', {
        method: 'POST',
        body: JSON.stringify({ storeCode, username, password })
    });
}

// Orders
export async function getOrders(tableId) {
    const query = tableId ? `?tableId=${tableId}` : '';
    return request(`/admin/orders${query}`);
}

export async function getOrderDetail(orderId) {
    return request(`/admin/orders/${orderId}`);
}

export async function updateOrderStatus(orderId, status) {
    return request(`/admin/orders/${orderId}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status })
    });
}

export async function deleteOrder(orderId) {
    return request(`/admin/orders/${orderId}`, { method: 'DELETE' });
}

export function subscribeOrders() {
    const token = getToken();
    return new EventSource(`${BASE_URL}/admin/orders/stream?token=${token}`);
}

// Tables
export async function getTables() {
    return request('/admin/tables');
}

export async function createTable(tableNumber, password) {
    return request('/admin/tables', {
        method: 'POST',
        body: JSON.stringify({ tableNumber, password })
    });
}

export async function completeTable(tableId) {
    return request(`/admin/tables/${tableId}/complete`, { method: 'POST' });
}

export async function getTableHistory(tableId, date) {
    const query = date ? `?date=${date}` : '';
    return request(`/admin/tables/${tableId}/history${query}`);
}

// Menus
export async function getMenus(categoryId) {
    const query = categoryId ? `?categoryId=${categoryId}` : '';
    return request(`/admin/menus${query}`);
}

export async function createMenu(data) {
    return request('/admin/menus', {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

export async function updateMenu(menuId, data) {
    return request(`/admin/menus/${menuId}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

export async function deleteMenu(menuId) {
    return request(`/admin/menus/${menuId}`, { method: 'DELETE' });
}

// Categories
export async function getCategories() {
    return request('/admin/categories');
}

export async function createCategory(name, displayOrder) {
    return request('/admin/categories', {
        method: 'POST',
        body: JSON.stringify({ name, displayOrder })
    });
}

export async function deleteCategory(categoryId) {
    return request(`/admin/categories/${categoryId}`, { method: 'DELETE' });
}
