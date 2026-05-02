import * as api from './api.js';

let orders = [];
let eventSource = null;

export async function initDashboard(onOrderClick) {
    await loadOrders(onOrderClick);
    connectSSE(onOrderClick);
}

export function destroyDashboard() {
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }
}

async function loadOrders(onOrderClick) {
    try {
        orders = await api.getOrders();
        renderOrders(onOrderClick);
    } catch (err) {
        console.error('주문 로드 실패:', err);
    }
}

function connectSSE(onOrderClick) {
    const statusEl = document.getElementById('sse-status');

    eventSource = api.subscribeOrders();

    eventSource.onopen = () => {
        statusEl.textContent = '실시간 연결됨';
        statusEl.className = 'sse-status sse-connected';
    };

    eventSource.addEventListener('order-created', (e) => {
        const data = JSON.parse(e.data);
        handleNewOrder(data, onOrderClick);
    });

    eventSource.addEventListener('order-status-changed', (e) => {
        const data = JSON.parse(e.data);
        handleStatusChanged(data, onOrderClick);
    });

    eventSource.addEventListener('order-deleted', (e) => {
        const data = JSON.parse(e.data);
        handleOrderDeleted(data, onOrderClick);
    });

    eventSource.onerror = () => {
        statusEl.textContent = '연결 끊김';
        statusEl.className = 'sse-status sse-disconnected';
    };
}

function handleNewOrder(data, onOrderClick) {
    // 기존 목록에 추가하고 다시 렌더링
    const newOrder = {
        id: data.orderId,
        orderNumber: data.orderNumber,
        tableId: data.tableId,
        tableNumber: data.tableNumber,
        totalAmount: data.totalAmount,
        status: data.status,
        createdAt: data.timestamp,
        isNew: true
    };
    orders.unshift(newOrder);
    renderOrders(onOrderClick);
}

function handleStatusChanged(data, onOrderClick) {
    const order = orders.find(o => o.id === data.orderId);
    if (order) {
        order.status = data.status;
        renderOrders(onOrderClick);
    }
}

function handleOrderDeleted(data, onOrderClick) {
    orders = orders.filter(o => o.id !== data.orderId);
    renderOrders(onOrderClick);
}

function renderOrders(onOrderClick) {
    const grid = document.getElementById('order-grid');
    const empty = document.getElementById('dashboard-empty');

    const activeOrders = orders.filter(o => o.status !== 'COMPLETED');

    if (activeOrders.length === 0) {
        grid.innerHTML = '';
        empty.hidden = false;
        return;
    }

    empty.hidden = true;
    grid.innerHTML = activeOrders.map(order => `
        <div class="order-card status-${order.status} ${order.isNew ? 'new-order' : ''}"
             data-order-id="${order.id}">
            <div class="order-card-top">
                <span class="order-table-num">테이블 ${order.tableNumber || '?'}</span>
                <span class="order-status-badge badge-${order.status}">${getStatusLabel(order.status)}</span>
            </div>
            <div class="order-card-items">#${escapeHtml(order.orderNumber)}</div>
            <div class="order-card-bottom">
                <span class="order-amount">${formatPrice(order.totalAmount)}</span>
                <span class="order-time">${formatTime(order.createdAt)}</span>
            </div>
            <div class="order-card-actions">
                ${getActionButtons(order)}
            </div>
        </div>
    `).join('');

    // 카드 클릭 → 상세
    grid.querySelectorAll('.order-card').forEach(card => {
        card.addEventListener('click', (e) => {
            if (e.target.closest('.btn')) return;
            onOrderClick(Number(card.dataset.orderId));
        });
    });

    // 상태 변경 버튼
    grid.querySelectorAll('[data-action="status"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            try {
                await api.updateOrderStatus(Number(btn.dataset.orderId), btn.dataset.status);
                location.reload();
            } catch (err) {
                alert(err.message);
            }
        });
    });

    // 삭제 버튼
    grid.querySelectorAll('[data-action="delete"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (!confirm('이 주문을 삭제하시겠습니까?')) return;
            try {
                await api.deleteOrder(Number(btn.dataset.orderId));
                location.reload();
            } catch (err) {
                alert(err.message);
            }
        });
    });

    // 새 주문 플래그 제거
    orders.forEach(o => { o.isNew = false; });
}

function getActionButtons(order) {
    if (order.status === 'PENDING') {
        return `
            <button class="btn btn-primary btn-sm" data-action="status" data-order-id="${order.id}" data-status="PREPARING">준비 시작</button>
            <button class="btn btn-danger btn-sm" data-action="delete" data-order-id="${order.id}">삭제</button>
        `;
    }
    if (order.status === 'PREPARING') {
        return `<button class="btn btn-success btn-sm" data-action="status" data-order-id="${order.id}" data-status="COMPLETED">완료</button>`;
    }
    return '';
}

function getStatusLabel(status) {
    return { PENDING: '대기중', PREPARING: '준비중', COMPLETED: '완료' }[status] || status;
}

function formatPrice(price) {
    return (price || 0).toLocaleString('ko-KR') + '원';
}

function formatTime(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}
