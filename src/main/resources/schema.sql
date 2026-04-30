-- 테이블오더 서비스 DB 스키마

CREATE TABLE IF NOT EXISTS stores (
    id BIGSERIAL PRIMARY KEY,
    store_code VARCHAR(50) NOT NULL UNIQUE,
    store_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS admins (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, username)
);

CREATE TABLE IF NOT EXISTS store_tables (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    table_number INTEGER NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, table_number)
);

CREATE TABLE IF NOT EXISTS table_sessions (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL REFERENCES store_tables(id),
    start_time TIMESTAMP NOT NULL DEFAULT NOW(),
    end_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    name VARCHAR(50) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, name)
);

CREATE TABLE IF NOT EXISTS menus (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name VARCHAR(100) NOT NULL,
    price INTEGER NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    table_id BIGINT NOT NULL REFERENCES store_tables(id),
    session_id BIGINT NOT NULL REFERENCES table_sessions(id),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    total_amount INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menus(id),
    menu_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_history (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    order_number VARCHAR(20) NOT NULL,
    total_amount INTEGER NOT NULL,
    order_items TEXT NOT NULL,
    ordered_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_admins_store_username ON admins(store_id, username);
CREATE INDEX IF NOT EXISTS idx_store_tables_store_number ON store_tables(store_id, table_number);
CREATE INDEX IF NOT EXISTS idx_table_sessions_table_endtime ON table_sessions(table_id, end_time);
CREATE INDEX IF NOT EXISTS idx_categories_store_order ON categories(store_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menus_store_category_order ON menus(store_id, category_id, display_order);
CREATE INDEX IF NOT EXISTS idx_orders_session_created ON orders(session_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_store_status_created ON orders(store_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_history_table_completed ON order_history(table_id, completed_at);
CREATE INDEX IF NOT EXISTS idx_orders_session_deleted ON orders(session_id, deleted);
CREATE INDEX IF NOT EXISTS idx_orders_store_deleted_status ON orders(store_id, deleted, status);
CREATE INDEX IF NOT EXISTS idx_order_history_table_ordered ON order_history(table_id, ordered_at);
