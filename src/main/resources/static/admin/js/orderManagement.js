import * as api from './api.js';

export async function showOrderDetail(orderId) {
    const modal = document.getElementById('order-detail-modal');
    const body = document.getElementById('detail-body');

    try {
        const order = await api.getOrderDetail(orderId);

        body.innerHTML = `
            <div>
                <p><strong>주문번호:</strong> ${escapeHtml(order.orderNumber)}</p>
                <p><strong>테이블:</strong> ${order.tableNumber || '?'}번</p>
                <p><strong>상태:</strong> <span class="order-status-badge badge-${order.status}">${getStatusLabel(order.status)}</span></p>
                <p><strong>주문시각:</strong> ${formatDateTime(order.createdAt)}</p>
            </div>
            <div class="detail-items">
                ${order.items.map(item => `
                    <div class="detail-item">
                        <span>${escapeHtml(item.menuName)} x${item.quantity}</span>
                        <span>${formatPrice(item.subtotal)}</span>
                    </div>
                `).join('')}
            </div>
            <div class="detail-total">
                <span>총 금액</span>
                <span>${formatPrice(order.totalAmount)}</span>
            </div>
            <div class="detail-actions">
                ${getDetailActions(order)}
            </div>
        `;

        // 상태 변경 버튼 이벤트
        body.querySelectorAll('[data-action="status"]').forEach(btn => {
            btn.addEventListener('click', async () => {
                try {
                    await api.updateOrderStatus(orderId, btn.dataset.status);
                    modal.hidden = true;
                } catch (err) {
                    alert(err.message);
                }
            });
        });

        // 삭제 버튼 이벤트
        const deleteBtn = body.querySelector('[data-action="delete"]');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', async () => {
                if (!confirm('이 주문을 삭제하시겠습니까?')) return;
                try {
                    await api.deleteOrder(orderId);
                    modal.hidden = true;
                } catch (err) {
                    alert(err.message);
                }
            });
        }

        modal.hidden = false;
    } catch (err) {
        alert('주문 상세 조회 실패: ' + err.message);
    }
}

function getDetailActions(order) {
    if (order.status === 'PENDING') {
        return `
            <button class="btn btn-primary" data-action="status" data-status="PREPARING">준비 시작</button>
            <button class="btn btn-danger" data-action="delete">주문 삭제</button>
        `;
    }
    if (order.status === 'PREPARING') {
        return `<button class="btn btn-success" data-action="status" data-status="COMPLETED">완료 처리</button>`;
    }
    return '<p style="color:#a0aec0;text-align:center;">완료된 주문입니다</p>';
}

function getStatusLabel(status) {
    return { PENDING: '대기중', PREPARING: '준비중', COMPLETED: '완료' }[status] || status;
}

function formatPrice(price) {
    return (price || 0).toLocaleString('ko-KR') + '원';
}

function formatDateTime(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('ko-KR');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}
