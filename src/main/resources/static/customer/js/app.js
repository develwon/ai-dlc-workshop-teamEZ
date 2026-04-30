import * as api from './api.js';
import { saveLoginInfo, getAuthInfo, getSavedCredentials, isLoggedIn } from './auth.js';
import { initCart, getItems, getItemCount, getTotalAmount, updateQuantity, removeItem, clearCart } from './cart.js';
import { initMenu } from './menu.js';
import { submitOrder } from './order.js';
import { loadOrderHistory } from './orderHistory.js';

// === DOM Elements ===
const loginScreen = document.getElementById('login-screen');
const mainScreen = document.getElementById('main-screen');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const headerTableInfo = document.getElementById('header-table-info');
const cartBadge = document.getElementById('cart-badge');
const orderSuccessModal = document.getElementById('order-success-modal');
const successOrderNumber = document.getElementById('success-order-number');

// === Init ===
document.addEventListener('DOMContentLoaded', async () => {
    initCart(onCartChange);
    setupNavigation();
    setupCartActions();
    setupLoginForm();
    await tryAutoLogin();
});

// === Auto Login ===
async function tryAutoLogin() {
    const saved = getSavedCredentials();
    if (saved) {
        try {
            const data = await api.login(saved.storeCode, saved.tableNumber, saved.password);
            saveLoginInfo(data, saved.storeCode, saved.tableNumber, saved.password);
            showMainScreen();
        } catch {
            showLoginScreen();
        }
    } else {
        showLoginScreen();
    }
}

// === Login ===
function setupLoginForm() {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        loginError.hidden = true;

        const storeCode = document.getElementById('store-code').value.trim();
        const tableNumber = parseInt(document.getElementById('table-number').value);
        const password = document.getElementById('table-password').value;

        try {
            const data = await api.login(storeCode, tableNumber, password);
            saveLoginInfo(data, storeCode, tableNumber, password);
            showMainScreen();
        } catch (err) {
            loginError.textContent = err.message || '로그인에 실패했습니다.';
            loginError.hidden = false;
        }
    });
}

// === Screens ===
function showLoginScreen() {
    loginScreen.hidden = false;
    mainScreen.hidden = true;
}

async function showMainScreen() {
    loginScreen.hidden = true;
    mainScreen.hidden = false;

    const auth = getAuthInfo();
    headerTableInfo.textContent = `테이블 ${auth.tableNumber}`;

    updateCartBadge();
    renderCart();
    await initMenu(auth.storeId);
}

// === Navigation ===
function setupNavigation() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
            document.getElementById(`tab-${tab}`).classList.add('active');

            if (tab === 'orders') loadOrderHistory();
            if (tab === 'cart') renderCart();
        });
    });
}

// === Cart ===
function onCartChange() {
    updateCartBadge();
    renderCart();
}

function updateCartBadge() {
    const count = getItemCount();
    cartBadge.textContent = count;
    cartBadge.hidden = count === 0;
}

function renderCart() {
    const items = getItems();
    const cartItemsEl = document.getElementById('cart-items');
    const cartEmpty = document.getElementById('cart-empty');
    const cartFooter = document.getElementById('cart-footer');
    const cartTotalAmount = document.getElementById('cart-total-amount');

    if (items.length === 0) {
        cartItemsEl.innerHTML = '';
        cartEmpty.hidden = false;
        cartFooter.hidden = true;
        return;
    }

    cartEmpty.hidden = true;
    cartFooter.hidden = false;
    cartTotalAmount.textContent = formatPrice(getTotalAmount());

    cartItemsEl.innerHTML = items.map(item => `
        <div class="cart-item">
            <div class="cart-item-info">
                <div class="cart-item-name">${escapeHtml(item.menuName)}</div>
                <div class="cart-item-price">${formatPrice(item.price * item.quantity)}</div>
            </div>
            <div class="cart-item-controls">
                <button aria-label="수량 감소" data-menu-id="${item.menuId}" data-action="decrease">−</button>
                <span class="cart-item-qty">${item.quantity}</span>
                <button aria-label="수량 증가" data-menu-id="${item.menuId}" data-action="increase">+</button>
            </div>
            <button class="cart-item-remove" aria-label="삭제" data-menu-id="${item.menuId}" data-action="remove">✕</button>
        </div>
    `).join('');

    // Event delegation
    cartItemsEl.querySelectorAll('button[data-action]').forEach(btn => {
        btn.addEventListener('click', () => {
            const menuId = Number(btn.dataset.menuId);
            const action = btn.dataset.action;
            if (action === 'increase') updateQuantity(menuId, 1);
            else if (action === 'decrease') updateQuantity(menuId, -1);
            else if (action === 'remove') removeItem(menuId);
        });
    });
}

function setupCartActions() {
    document.getElementById('btn-clear-cart').addEventListener('click', () => {
        if (confirm('장바구니를 비우시겠습니까?')) {
            clearCart();
        }
    });

    document.getElementById('btn-order').addEventListener('click', async () => {
        try {
            const response = await submitOrder();
            showOrderSuccess(response.orderNumber);
        } catch (err) {
            alert(err.message || '주문에 실패했습니다.');
        }
    });
}

function showOrderSuccess(orderNumber) {
    successOrderNumber.textContent = orderNumber;
    orderSuccessModal.hidden = false;

    setTimeout(() => {
        orderSuccessModal.hidden = true;
        // 메뉴 탭으로 이동
        document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
        document.querySelector('.nav-btn[data-tab="menu"]').classList.add('active');
        document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
        document.getElementById('tab-menu').classList.add('active');
    }, 5000);
}

// === Helpers ===
function formatPrice(price) {
    return price.toLocaleString('ko-KR') + '원';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
