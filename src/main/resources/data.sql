-- 시드 데이터: 샘플 매장, 관리자, 테이블, 카테고리, 메뉴
-- 비밀번호는 모두 bcrypt 해시 (원본: "password1234")
-- $2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4

-- 매장
INSERT INTO stores (store_code, store_name) VALUES ('STORE001', '맛있는 식당') ON CONFLICT DO NOTHING;

-- 관리자 (비밀번호: password1234)
INSERT INTO admins (store_id, username, password_hash, role)
SELECT 1, 'owner', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4', 'OWNER'
WHERE NOT EXISTS (SELECT 1 FROM admins WHERE store_id = 1 AND username = 'owner');

INSERT INTO admins (store_id, username, password_hash, role)
SELECT 1, 'manager', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4', 'MANAGER'
WHERE NOT EXISTS (SELECT 1 FROM admins WHERE store_id = 1 AND username = 'manager');

INSERT INTO admins (store_id, username, password_hash, role)
SELECT 1, 'staff', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4', 'STAFF'
WHERE NOT EXISTS (SELECT 1 FROM admins WHERE store_id = 1 AND username = 'staff');

-- 테이블 (비밀번호: 1234)
INSERT INTO store_tables (store_id, table_number, password_hash)
SELECT 1, 1, '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4'
WHERE NOT EXISTS (SELECT 1 FROM store_tables WHERE store_id = 1 AND table_number = 1);

INSERT INTO store_tables (store_id, table_number, password_hash)
SELECT 1, 2, '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4'
WHERE NOT EXISTS (SELECT 1 FROM store_tables WHERE store_id = 1 AND table_number = 2);

INSERT INTO store_tables (store_id, table_number, password_hash)
SELECT 1, 3, '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd6BZCfIgB1Tno6p.HsBXlSNew4'
WHERE NOT EXISTS (SELECT 1 FROM store_tables WHERE store_id = 1 AND table_number = 3);

-- 카테고리
INSERT INTO categories (store_id, name, display_order) VALUES (1, '메인 메뉴', 1) ON CONFLICT DO NOTHING;
INSERT INTO categories (store_id, name, display_order) VALUES (1, '사이드 메뉴', 2) ON CONFLICT DO NOTHING;
INSERT INTO categories (store_id, name, display_order) VALUES (1, '음료', 3) ON CONFLICT DO NOTHING;

-- 메뉴
INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 1, '김치찌개', 9000, '깊은 맛의 전통 김치찌개', '/images/kimchi-jjigae.svg', 1
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '김치찌개');

INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 1, '된장찌개', 8000, '구수한 된장찌개', '/images/doenjang-jjigae.svg', 2
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '된장찌개');

INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 1, '불고기', 15000, '달콤한 양념 불고기', '/images/bulgogi.svg', 3
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '불고기');

INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 2, '계란말이', 7000, '부드러운 계란말이', '/images/gyeran-mari.svg', 1
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '계란말이');

INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 2, '감자전', 6000, '바삭한 감자전', '/images/gamja-jeon.svg', 2
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '감자전');

INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 3, '콜라', 2000, '시원한 콜라', '/images/cola.svg', 1
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '콜라');

INSERT INTO menus (store_id, category_id, name, price, description, image_url, display_order)
SELECT 1, 3, '사이다', 2000, '청량한 사이다', '/images/cider.svg', 2
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE store_id = 1 AND name = '사이다');
