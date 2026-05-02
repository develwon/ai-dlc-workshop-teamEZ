import * as api from './api.js';
import { saveAuth, getAuth, clearAuth, isLoggedIn } from './auth.js';
import { initDashboard, destroyDashboard } from './dashboard.js';
import { showOrderDetail } from './orderManagement.js';
import { initTableManagement } from './tableManagement.js';
import { initMenuManagement } from './menuManagement.js';

// === DOM ===
const loginScreen = document.getElementById('login-screen');
const mainScreen = document.getElementById('main-screen');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const storeName = document.getElementById('store-name');

// === Init ===
document.addEventListener('DOMContentLoaded', () => {
    setupLogin();
    setupLogout();
    setupNavigation();
    setupModals();

    if (isLoggedIn()) {
        showMainScreen();
    } else {
        showLoginScreen();
    }
});

// === Login ===
function setupLogin() {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        loginError.hidden = true;

        const storeCode = document.getElementById('store-code').value.trim();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        try {
            const data = await api.login(storeCode, username, password);
            saveAuth(data);
            showMainScreen();
        } catch (err) {
            loginError.textContent = err.message || '로그인에 실패했습니다.';
            loginError.hidden = false;
        }
    });
}

function setupLogout() {
    document.getElementById('btn-logout').addEventListener('click', () => {
        destroyDashboard();
        clearAuth();
        showLoginScreen();
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

    const auth = getAuth();
    if (auth) {
        storeName.textContent = auth.storeName;
    }

    await initDashboard(showOrderDetail);
}

// === Navigation ===
function setupNavigation() {
    document.querySelectorAll('.admin-nav-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const section = btn.dataset.section;

            document.querySelectorAll('.admin-nav-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
            document.getElementById(`section-${section}`).classList.add('active');

            if (section === 'tables') await initTableManagement();
            if (section === 'menus') await initMenuManagement();
            if (section === 'dashboard') await initDashboard(showOrderDetail);
        });
    });
}

// === Modals ===
function setupModals() {
    document.getElementById('btn-close-detail').addEventListener('click', () => {
        document.getElementById('order-detail-modal').hidden = true;
    });

    // 모달 배경 클릭으로 닫기 (드래그 시 오닫힘 방지)
    document.querySelectorAll('.modal').forEach(modal => {
        let mouseDownTarget = null;
        modal.addEventListener('mousedown', (e) => {
            mouseDownTarget = e.target;
        });
        modal.addEventListener('click', (e) => {
            if (e.target === modal && mouseDownTarget === modal) {
                modal.hidden = true;
            }
        });
    });
}
