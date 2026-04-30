import * as api from './api.js';
import { addItem } from './cart.js';

let categories = [];
let currentCategoryId = null;
let storeId = null;

export async function initMenu(sid) {
    storeId = sid;
    await loadCategories();
    await loadMenus();
}

async function loadCategories() {
    const categoryBar = document.getElementById('category-bar');
    try {
        categories = await api.getCategories(storeId);
        categoryBar.innerHTML = '';

        // 전체 칩
        const allChip = createCategoryChip('전체', null, true);
        categoryBar.appendChild(allChip);

        categories.forEach(cat => {
            const chip = createCategoryChip(cat.name, cat.id, false);
            categoryBar.appendChild(chip);
        });
    } catch (err) {
        console.error('카테고리 로드 실패:', err);
    }
}

function createCategoryChip(name, id, active) {
    const chip = document.createElement('button');
    chip.className = `category-chip${active ? ' active' : ''}`;
    chip.textContent = name;
    chip.setAttribute('role', 'tab');
    chip.setAttribute('aria-selected', active ? 'true' : 'false');
    chip.addEventListener('click', () => selectCategory(id, chip));
    return chip;
}

function selectCategory(categoryId, chip) {
    currentCategoryId = categoryId;
    document.querySelectorAll('.category-chip').forEach(c => {
        c.classList.remove('active');
        c.setAttribute('aria-selected', 'false');
    });
    chip.classList.add('active');
    chip.setAttribute('aria-selected', 'true');
    loadMenus();
}

async function loadMenus() {
    const menuGrid = document.getElementById('menu-grid');
    try {
        const menus = await api.getMenus(storeId, currentCategoryId);
        menuGrid.innerHTML = '';

        if (menus.length === 0) {
            menuGrid.innerHTML = '<div class="empty-state"><p>메뉴가 없습니다</p></div>';
            return;
        }

        menus.forEach(menu => {
            const card = createMenuCard(menu);
            menuGrid.appendChild(card);
        });
    } catch (err) {
        console.error('메뉴 로드 실패:', err);
    }
}

function createMenuCard(menu) {
    const card = document.createElement('div');
    card.className = 'menu-card';

    const imgDiv = document.createElement('div');
    imgDiv.className = 'menu-card-img';
    if (menu.imageUrl) {
        const img = document.createElement('img');
        img.src = menu.imageUrl;
        img.alt = menu.name;
        img.loading = 'lazy';
        imgDiv.appendChild(img);
    } else {
        imgDiv.textContent = '🍽️';
    }

    const body = document.createElement('div');
    body.className = 'menu-card-body';
    body.innerHTML = `
        <div class="menu-card-name">${escapeHtml(menu.name)}</div>
        <div class="menu-card-price">${formatPrice(menu.price)}</div>
        ${menu.description ? `<div class="menu-card-desc">${escapeHtml(menu.description)}</div>` : ''}
    `;

    const actions = document.createElement('div');
    actions.className = 'menu-card-actions';
    const addBtn = document.createElement('button');
    addBtn.className = 'btn btn-primary btn-sm btn-add-cart';
    addBtn.textContent = '담기';
    addBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        addItem(menu);
    });
    actions.appendChild(addBtn);

    card.appendChild(imgDiv);
    card.appendChild(body);
    card.appendChild(actions);
    return card;
}

function formatPrice(price) {
    return price.toLocaleString('ko-KR') + '원';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
