import * as api from './api.js';

export async function initTableManagement() {
    await loadTables();
    setupAddTable();
}

async function loadTables() {
    const grid = document.getElementById('table-grid');
    try {
        const tables = await api.getTables();

        if (!tables || tables.length === 0) {
            grid.innerHTML = '<div class="empty-state"><p>등록된 테이블이 없습니다</p></div>';
            return;
        }

        grid.innerHTML = tables.map(table => `
            <div class="table-card" data-table-id="${table.id}">
                <div class="table-card-header">
                    <span class="table-num">테이블 ${table.tableNumber}</span>
                    <span class="table-session-status ${table.hasActiveSession ? 'session-active' : 'session-inactive'}">
                        ${table.hasActiveSession ? '이용중' : '비어있음'}
                    </span>
                </div>
                <div class="table-card-info">
                    ${table.totalAmount ? `현재 주문액: ${formatPrice(table.totalAmount)}` : '주문 없음'}
                </div>
                <div class="table-card-actions">
                    ${table.hasActiveSession ? `
                        <button class="btn btn-warning btn-sm" data-action="complete" data-table-id="${table.id}">이용 완료</button>
                    ` : ''}
                    <button class="btn btn-secondary btn-sm" data-action="history" data-table-id="${table.id}">과거 내역</button>
                </div>
            </div>
        `).join('');

        // 이용 완료 버튼
        grid.querySelectorAll('[data-action="complete"]').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (!confirm('이 테이블의 이용을 완료하시겠습니까?\n현재 주문이 과거 내역으로 이동됩니다.')) return;
                try {
                    await api.completeTable(Number(btn.dataset.tableId));
                    await loadTables();
                } catch (err) {
                    alert('이용 완료 실패: ' + err.message);
                }
            });
        });

        // 과거 내역 버튼
        grid.querySelectorAll('[data-action="history"]').forEach(btn => {
            btn.addEventListener('click', () => {
                showHistory(Number(btn.dataset.tableId));
            });
        });
    } catch (err) {
        console.error('테이블 로드 실패:', err);
        grid.innerHTML = '<div class="empty-state"><p>테이블 로드 실패</p></div>';
    }
}

function setupAddTable() {
    const btn = document.getElementById('btn-add-table');
    btn.addEventListener('click', async () => {
        const tableNumber = prompt('테이블 번호를 입력하세요:');
        if (!tableNumber) return;
        const password = prompt('테이블 비밀번호를 입력하세요:');
        if (!password) return;

        try {
            await api.createTable(Number(tableNumber), password);
            await loadTables();
        } catch (err) {
            alert('테이블 추가 실패: ' + err.message);
        }
    });
}

async function showHistory(tableId) {
    const modal = document.getElementById('order-detail-modal');
    const title = document.getElementById('detail-title');
    const body = document.getElementById('detail-body');

    title.textContent = '과거 주문 내역';

    try {
        const history = await api.getTableHistory(tableId);

        if (!history || history.length === 0) {
            body.innerHTML = '<div class="empty-state"><p>과거 주문 내역이 없습니다</p></div>';
        } else {
            body.innerHTML = history.map(h => `
                <div style="padding:0.75rem 0;border-bottom:1px solid #f0f0f0;">
                    <div style="display:flex;justify-content:space-between;margin-bottom:0.25rem;">
                        <strong>#${escapeHtml(h.orderNumber)}</strong>
                        <span>${formatPrice(h.totalAmount)}</span>
                    </div>
                    <div style="font-size:0.8125rem;color:#718096;">
                        주문: ${formatDateTime(h.orderedAt)} | 완료: ${formatDateTime(h.completedAt)}
                    </div>
                </div>
            `).join('');
        }

        modal.hidden = false;
    } catch (err) {
        alert('과거 내역 조회 실패: ' + err.message);
    }
}

function formatPrice(price) {
    return (price || 0).toLocaleString('ko-KR') + '원';
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('ko-KR');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}
