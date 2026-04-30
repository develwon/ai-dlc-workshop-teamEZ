import * as api from './api.js';
import { getItems, getTotalAmount, toOrderItems, clearCart } from './cart.js';
import { getAuthInfo, updateSessionId } from './auth.js';

export async function submitOrder() {
    const auth = getAuthInfo();
    if (!auth) throw new Error('로그인이 필요합니다.');

    const items = toOrderItems();
    if (items.length === 0) throw new Error('장바구니가 비어있습니다.');

    const response = await api.createOrder(
        auth.storeId,
        auth.tableId,
        null,
        items
    );

    // 세션 ID 업데이트
    if (response.sessionId) {
        updateSessionId(response.sessionId);
    }

    // 장바구니 비우기
    clearCart();

    return response;
}
