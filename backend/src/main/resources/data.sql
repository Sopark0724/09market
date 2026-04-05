-- 샘플 데이터

-- 판매자 사용자
INSERT INTO users (id, email, name, phone, role, auth_provider, kakao_id, created_at) VALUES
(1, 'seller@test.com', '김판매', '01012345678', 'SELLER', 'PHONE', null, NOW()),
(2, 'seller2@test.com', '이판매', '01087654321', 'SELLER', 'KAKAO', 'kakao_seller2', NOW());

-- 구매자 사용자
INSERT INTO users (id, email, name, phone, role, auth_provider, kakao_id, created_at) VALUES
(3, 'buyer@test.com', '박구매', '01011112222', 'BUYER', 'PHONE', null, NOW()),
(4, 'buyer2@test.com', '최구매', '01033334444', 'BUYER', 'KAKAO', 'kakao_buyer2', NOW());

-- 매장
INSERT INTO stores (id, user_id, store_name, address, address_detail, phone, business_hours, description, active, created_at, updated_at) VALUES
(1, 1, '신선농산물', '서울시 송파구 가락동 600', '가락시장 A동 101호', '02-1234-5678', '새벽 4시 ~ 오후 2시', '가락시장 직영 신선 농산물 전문점입니다. 매일 새벽 산지에서 직송합니다.', true, NOW(), NOW()),
(2, 2, '해피팜마켓', '경기도 성남시 분당구 서현동 123', '서현역 2번 출구 앞', '031-123-4567', '오전 6시 ~ 오후 6시', '유기농 농산물 전문 도매점입니다.', true, NOW(), NOW());

-- 상품
INSERT INTO products (id, store_id, name, description, unit, category, active, created_at, updated_at) VALUES
(1, 1, '사과 (부사)', '경북 영주산 부사 사과', 'box (10kg)', '과일', true, NOW(), NOW()),
(2, 1, '배 (신고)', '나주산 신고배', 'box (15kg)', '과일', true, NOW(), NOW()),
(3, 1, '양파', '무안산 양파', '망 (20kg)', '채소', true, NOW(), NOW()),
(4, 1, '감자', '강원도 횡성산 감자', '박스 (10kg)', '채소', true, NOW(), NOW()),
(5, 1, '딸기', '논산산 설향 딸기', '박스 (2kg)', '과일', true, NOW(), NOW()),
(6, 2, '유기농 토마토', '무농약 인증 토마토', '박스 (5kg)', '채소', true, NOW(), NOW()),
(7, 2, '유기농 상추', '무농약 인증 상추', '박스 (4kg)', '채소', true, NOW(), NOW()),
(8, 2, '유기농 당근', '무농약 인증 당근', '박스 (10kg)', '채소', true, NOW(), NOW());

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
