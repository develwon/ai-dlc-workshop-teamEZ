import * as api from './api.js';

let categories = [];
let currentCategoryId = null;

export async function initMenuManagement() {
    await loadCategories();
    await loadMenus();
    setupAddMenu();
}

async function loadCategories() {
    const tabs = document.getElementById('category-tabs');
    try {
        categories = await api.getCategories();
        tabs.innerHTML = '';

        const allTab = createTab('전체', null, true);
        tabs.appendChild(allTab);

        categories.forEach(cat => {
            const tab = createTab(cat.name, cat.id, false);
            tabs.appendChild(tab);
        });

        // 카테고리 추가 버튼
        const addBtn = document.createElement('button');
        addBtn.className = 'category-tab';
        addBtn.textContent = '+ 추가';
        addBtn.addEventListener('click', addCategory);
        tabs.appendChild(addBtn);
    } catch (err) {
        console.error('카테고리 로드 실패:', err);
    }
}

function createTab(name, id, active) {
    const tab = document.createElement('button');
    tab.className = `category-tab${active ? ' active' : ''}`;
    tab.textContent = name;
    tab.addEventListener('click', () => {
        currentCategoryId = id;
        document.querySelectorAll('.category-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        loadMenus();
    });
    return tab;
}

async function addCategory() {
    const name = prompt('카테고리 이름을 입력하세요:');
    if (!name) return;
    try {
        await api.createCategory(name, categories.length);
        await loadCategories();
    } catch (err) {
        alert('카테고리 추가 실패: ' + err.message);
    }
}

async function loadMenus() {
    const list = document.getElementById('admin-menu-list');
    try {
        const menus = await api.getMenus(currentCategoryId);

        if (!menus || menus.length === 0) {
            list.innerHTML = '<div class="empty-state"><p>메뉴가 없습니다</p></div>';
            return;
        }

        list.innerHTML = menus.map(menu => `
            <div class="admin-menu-item">
                <div class="admin-menu-img">
                    ${menu.imageUrl ? `<img src="${menu.imageUrl}" alt="${escapeHtml(menu.name)}" style="width:100%;height:100%;object-fit:cover;border-radius:8px;">` : '🍽️'}
                </div>
                <div class="admin-menu-info">
                    <div class="admin-menu-name">${escapeHtml(menu.name)}</div>
                    <div class="admin-menu-price">${formatPrice(menu.price)}</div>
                </div>
                <div class="admin-menu-actions">
                    <button class="btn btn-secondary btn-sm" data-action="edit" data-menu-id="${menu.id}">수정</button>
                    <button class="btn btn-danger btn-sm" data-action="delete" data-menu-id="${menu.id}">삭제</button>
                </div>
            </div>
        `).join('');

        // 수정 버튼
        list.querySelectorAll('[data-action="edit"]').forEach(btn => {
            btn.addEventListener('click', () => editMenu(Number(btn.dataset.menuId), menus));
        });

        // 삭제 버튼
        list.querySelectorAll('[data-action="delete"]').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (!confirm('이 메뉴를 삭제하시겠습니까?')) return;
                try {
                    await api.deleteMenu(Number(btn.dataset.menuId));
                    await loadMenus();
                } catch (err) {
                    alert('메뉴 삭제 실패: ' + err.message);
                }
            });
        });
    } catch (err) {
        console.error('메뉴 로드 실패:', err);
    }
}

function setupAddMenu() {
    document.getElementById('btn-add-menu').addEventListener('click', () => {
        showMenuForm(null);
    });
}

function editMenu(menuId, menus) {
    const menu = menus.find(m => m.id === menuId);
    if (menu) showMenuForm(menu);
}

function showMenuForm(menu) {
    const modal = document.getElementById('order-detail-modal');
    const title = document.getElementById('detail-title');
    const body = document.getElementById('detail-body');

    title.textContent = menu ? '메뉴 수정' : '메뉴 추가';

    const categoryOptions = categories.map(c =>
        `<option value="${c.id}" ${menu && menu.categoryId === c.id ? 'selected' : ''}>${escapeHtml(c.name)}</option>`
    ).join('');

    body.innerHTML = `
        <form id="menu-form" style="display:flex;flex-direction:column;gap:1rem;">
            <div class="form-group">
                <label>메뉴명</label>
                <input type="text" id="mf-name" value="${menu ? escapeHtml(menu.name) : ''}" required style="width:100%;padding:0.75rem;border:1px solid #ddd;border-radius:8px;">
            </div>
            <div class="form-group">
                <label>가격</label>
                <input type="number" id="mf-price" value="${menu ? menu.price : ''}" required min="0" style="width:100%;padding:0.75rem;border:1px solid #ddd;border-radius:8px;">
            </div>
            <div class="form-group">
                <label>카테고리</label>
                <select id="mf-category" required style="width:100%;padding:0.75rem;border:1px solid #ddd;border-radius:8px;">
                    <option value="">선택하세요</option>
                    ${categoryOptions}
                </select>
            </div>
            <div class="form-group">
                <label>설명</label>
                <textarea id="mf-desc" rows="3" style="width:100%;padding:0.75rem;border:1px solid #ddd;border-radius:8px;">${menu && menu.description ? escapeHtml(menu.description) : ''}</textarea>
            </div>
            <div class="form-group">
                <label>이미지 URL</label>
                <input type="url" id="mf-image" value="${menu && menu.imageUrl ? menu.imageUrl : ''}" style="width:100%;padding:0.75rem;border:1px solid #ddd;border-radius:8px;">
            </div>
            <button type="submit" class="btn btn-primary btn-full">${menu ? '수정' : '추가'}</button>
        </form>
    `;

    document.getElementById('menu-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            name: document.getElementById('mf-name').value,
            price: Number(document.getElementById('mf-price').value),
            categoryId: Number(document.getElementById('mf-category').value),
            description: document.getElementById('mf-desc').value || null,
            imageUrl: document.getElementById('mf-image').value || null
        };

        try {
            if (menu) {
                await api.updateMenu(menu.id, data);
            } else {
                await api.createMenu(data);
            }
            modal.hidden = true;
            await loadMenus();
        } catch (err) {
            alert((menu ? '수정' : '추가') + ' 실패: ' + err.message);
        }
    });

    modal.hidden = false;
}

function formatPrice(price) {
    return (price || 0).toLocaleString('ko-KR') + '원';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}
