import * as api from './api.js';
import { getAuthInfo } from './auth.js';

export async function loadOrderHistory() {
    const auth = getAuthInfo();
    if (!auth) return [];

    try {
        const orders = await api.getTableOrders(auth.tableId, auth.sessionId);
        renderOrders(orders);
        return orders;
    } catch (err) {
        console.error('주문 내역 로드 실패:', err);
        return [];
    }
}

function renderOrders(orders) {
    const orderList = document.getElementById('order-list');
    const emptyState = document.getElementById('orders-empty');

    if (orders.length === 0) {
        orderList.innerHTML = '';
        emptyState.hidden = false;
        return;
    }

    emptyState.hidden = true;
    orderList.innerHTML = orders.map(order => `
        <div class="order-card">
            <div class="order-card-header">
                <span class="order-number">#${escapeHtml(order.orderNumber)}</span>
                <span class="order-status status-${order.status}">${getStatusLabel(order.status)}</span>
            </div>
            <div class="order-card-items">
                ${order.items.map(item => `${escapeHtml(item.menuName)} x${item.quantity}`).join(', ')}
            </div>
            <div class="order-card-footer">
                <span>${formatTime(order.createdAt)}</span>
                <span class="order-card-total">${formatPrice(order.totalAmount)}</span>
            </div>
        </div>
    `).join('');
}

function getStatusLabel(status) {
    const labels = { PENDING: '대기중', PREPARING: '준비중', COMPLETED: '완료' };
    return labels[status] || status;
}

function formatPrice(price) {
    return price.toLocaleString('ko-KR') + '원';
}

function formatTime(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
