const CART_KEY = 'table_cart';

let cartItems = [];
let onChangeCallback = null;

export function initCart(onChange) {
    onChangeCallback = onChange;
    const saved = localStorage.getItem(CART_KEY);
    if (saved) {
        try { cartItems = JSON.parse(saved); } catch { cartItems = []; }
    }
}

function save() {
    localStorage.setItem(CART_KEY, JSON.stringify(cartItems));
    if (onChangeCallback) onChangeCallback(cartItems);
}

export function addItem(menu) {
    const existing = cartItems.find(item => item.menuId === menu.id);
    if (existing) {
        existing.quantity += 1;
    } else {
        cartItems.push({
            menuId: menu.id,
            menuName: menu.name,
            price: menu.price,
            quantity: 1,
            imageUrl: menu.imageUrl
        });
    }
    save();
}

export function updateQuantity(menuId, delta) {
    const item = cartItems.find(i => i.menuId === menuId);
    if (!item) return;
    item.quantity += delta;
    if (item.quantity <= 0) {
        cartItems = cartItems.filter(i => i.menuId !== menuId);
    }
    save();
}

export function removeItem(menuId) {
    cartItems = cartItems.filter(i => i.menuId !== menuId);
    save();
}

export function clearCart() {
    cartItems = [];
    save();
}

export function getItems() {
    return [...cartItems];
}

export function getItemCount() {
    return cartItems.reduce((sum, item) => sum + item.quantity, 0);
}

export function getTotalAmount() {
    return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
}

export function toOrderItems() {
    return cartItems.map(item => ({
        menuId: item.menuId,
        quantity: item.quantity
    }));
}
