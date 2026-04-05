-- 샘플 데이터

-- 판매자 사용자
INSERT INTO users (id, email, name, phone, role, auth_provider, kakao_id, created_at) VALUES
(1, 'seller@test.com', '김판매', '01012345678', 'SELLER', 'PHONE', null, NOW()),
(2, 'seller2@test.com', '이판매', '01087654321', 'SELLER', 'KAKAO', 'kakao_seller2', NOW());

-- 구매자 사용자
INSERT INTO users (id, email, name, phone, role, auth_provider, kakao_id, created_at) VALUES
(3, 'buyer@test.com', '박구매', '01011112222', 'BUYER', 'PHONE', null, NOW()),
(4, 'buyer2@test.com', '최구매', '01033334444', 'BUYER', 'KAKAO', 'kakao_buyer2', NOW());

-- 관리자 사용자
INSERT INTO users (id, email, name, phone, role, auth_provider, kakao_id, created_at) VALUES
(5, 'admin@test.com', '관리자', '01099999999', 'ADMIN', 'PHONE', null, NOW());

-- 매장
INSERT INTO stores (id, user_id, store_name, address, address_detail, phone, business_hours, description, active, created_at, updated_at) VALUES
(1, 1, '신선농산물', '서울시 송파구 가락동 600', '가락시장 A동 101호', '02-1234-5678', '새벽 4시 ~ 오후 2시', '가락시장 직영 신선 농산물 전문점입니다. 매일 새벽 산지에서 직송합니다.', true, NOW(), NOW()),
(2, 2, '해피팜마켓', '경기도 성남시 분당구 서현동 123', '서현역 2번 출구 앞', '031-123-4567', '오전 6시 ~ 오후 6시', '유기농 농산물 전문 도매점입니다.', true, NOW(), NOW());

-- 상품
INSERT INTO products (id, store_id, name, description, unit, image_url, detail_images, category, active, created_at, updated_at) VALUES
(1, 1, '사과 (부사)', '경북 영주산 부사 사과입니다. 당도 높고 아삭한 식감이 일품입니다. 산지에서 당일 수확하여 직송합니다.', 'box (10kg)', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1584306670957-acf935f5033c?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=600&h=400&fit=crop', '과일', true, NOW(), NOW()),
(2, 1, '배 (신고)', '나주산 신고배입니다. 과즙이 풍부하고 달콤합니다. 선물용으로도 최적입니다.', 'box (15kg)', 'https://images.unsplash.com/photo-1514756331096-242fdeb70d4a?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1589927986089-35812388d1f4?w=600&h=400&fit=crop', '과일', true, NOW(), NOW()),
(3, 1, '양파', '무안산 양파입니다. 육질이 단단하고 매운맛이 적어 다양한 요리에 활용할 수 있습니다.', '망 (20kg)', 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1580201092675-a0a6a6cafbb1?w=600&h=400&fit=crop', '채소', true, NOW(), NOW()),
(4, 1, '감자', '강원도 횡성산 감자입니다. 분질감자로 포슬포슬한 식감이 특징입니다. 찌개, 볶음, 구이 등 다양하게 활용하세요.', '박스 (10kg)', 'https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1635774855536-9728f2610245?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1518977956812-cd3dbadaaf31?w=600&h=400&fit=crop', '채소', true, NOW(), NOW()),
(5, 1, '딸기', '논산산 설향 딸기입니다. 향이 진하고 당도가 높습니다. 신선도 유지를 위해 새벽 수확 후 즉시 발송합니다.', '박스 (2kg)', 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1543528176-61b239494933?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1495570689269-d883b1224443?w=600&h=400&fit=crop', '과일', true, NOW(), NOW()),
(6, 2, '유기농 토마토', '무농약 인증 유기농 토마토입니다. 리코펜이 풍부하고 자연 그대로의 맛을 느낄 수 있습니다.', '박스 (5kg)', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1558818498-28c1e002b655?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1561136594-7f68413baa99?w=600&h=400&fit=crop', '채소', true, NOW(), NOW()),
(7, 2, '유기농 상추', '무농약 인증 유기농 상추입니다. 아삭하고 신선합니다. 쌈채소로 최적입니다.', '박스 (4kg)', 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&h=400&fit=crop', '채소', true, NOW(), NOW()),
(8, 2, '유기농 당근', '무농약 인증 유기농 당근입니다. 베타카로틴이 풍부하고 단맛이 강합니다. 주스, 샐러드에 적합합니다.', '박스 (10kg)', 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400&h=300&fit=crop', 'https://images.unsplash.com/photo-1447175008436-054170c2e979?w=600&h=400&fit=crop,https://images.unsplash.com/photo-1582515073490-39981397c445?w=600&h=400&fit=crop', '채소', true, NOW(), NOW());

-- 날짜별 재고 (오늘부터 7일간)
INSERT INTO product_inventories (product_id, available_date, price, stock_quantity, remaining_quantity, status, created_at, updated_at) VALUES
-- 신선농산물 - 오늘
(1, CURRENT_DATE, 35000, 50, 48, 'AVAILABLE', NOW(), NOW()),
(2, CURRENT_DATE, 45000, 30, 30, 'AVAILABLE', NOW(), NOW()),
(3, CURRENT_DATE, 15000, 100, 95, 'AVAILABLE', NOW(), NOW()),
(4, CURRENT_DATE, 18000, 80, 80, 'AVAILABLE', NOW(), NOW()),
(5, CURRENT_DATE, 25000, 40, 38, 'AVAILABLE', NOW(), NOW()),
-- 신선농산물 - 내일
(1, CURRENT_DATE + 1, 34000, 60, 60, 'AVAILABLE', NOW(), NOW()),
(2, CURRENT_DATE + 1, 44000, 35, 35, 'AVAILABLE', NOW(), NOW()),
(3, CURRENT_DATE + 1, 14000, 120, 120, 'AVAILABLE', NOW(), NOW()),
(5, CURRENT_DATE + 1, 24000, 45, 45, 'AVAILABLE', NOW(), NOW()),
-- 신선농산물 - 모레
(1, CURRENT_DATE + 2, 33000, 55, 55, 'AVAILABLE', NOW(), NOW()),
(3, CURRENT_DATE + 2, 13500, 100, 100, 'AVAILABLE', NOW(), NOW()),
(4, CURRENT_DATE + 2, 17000, 90, 90, 'AVAILABLE', NOW(), NOW()),
-- 해피팜마켓 - 오늘
(6, CURRENT_DATE, 28000, 40, 40, 'AVAILABLE', NOW(), NOW()),
(7, CURRENT_DATE, 12000, 60, 58, 'AVAILABLE', NOW(), NOW()),
(8, CURRENT_DATE, 22000, 50, 50, 'AVAILABLE', NOW(), NOW()),
-- 해피팜마켓 - 내일
(6, CURRENT_DATE + 1, 27000, 45, 45, 'AVAILABLE', NOW(), NOW()),
(7, CURRENT_DATE + 1, 11000, 70, 70, 'AVAILABLE', NOW(), NOW()),
(8, CURRENT_DATE + 1, 21000, 55, 55, 'AVAILABLE', NOW(), NOW());
