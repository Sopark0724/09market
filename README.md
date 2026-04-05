# 우리마켓 (Woori Market)

농산물 도매 플랫폼 - 판매자와 구매자를 직접 연결하는 날짜별 상품 주문 시스템

## 기술 스택

- **Backend**: Spring Boot 2.7.18, Spring Security, Spring Data JPA, JWT
- **Database**: H2 (In-Memory)
- **Frontend**: Svelte 4 + Vite 5
- **Build**: Gradle 7.6 / Maven (대체)

## 프로젝트 구조

```
woori-market/
├── backend/          # Spring Boot 백엔드
│   ├── build.gradle.kts   # Gradle 빌드 파일
│   ├── pom.xml            # Maven 빌드 파일 (대체)
│   └── src/main/java/com/woorimarket/
│       ├── auth/          # 인증 (카카오/핸드폰)
│       ├── config/        # Security, JWT, CORS
│       └── domain/        # 도메인별 엔티티, 서비스, API
├── frontend/         # Svelte SPA 프론트엔드
│   └── src/
│       ├── routes/        # 페이지 컴포넌트
│       ├── stores/        # 상태관리
│       └── api/           # API 클라이언트
└── README.md
```

## 실행 방법

### 1. 백엔드 실행

**Gradle 사용:**
```bash
cd backend
../gradlew bootRun
```

**Maven 사용 (대체):**
```bash
cd backend
mvn spring-boot:run
```

**또는 Maven Wrapper 설치 후:**
```bash
cd backend
mvn -N wrapper:wrapper
./mvnw spring-boot:run
```

백엔드가 `http://localhost:8080`에서 실행됩니다.

### 2. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드가 `http://localhost:5173`에서 실행됩니다.

### 3. H2 콘솔 접속

`http://localhost:8080/h2-console` 에서 DB 데이터를 확인할 수 있습니다.
- JDBC URL: `jdbc:h2:mem:woorimarket`
- Username: `sa`
- Password: (비워둡니다)

## 테스트 계정

| 역할 | 이름 | 전화번호 | 인증방식 |
|------|------|----------|---------|
| 판매자 | 김판매 | 01012345678 | 핸드폰 |
| 판매자 | 이판매 | 01087654321 | 카카오 |
| 구매자 | 박구매 | 01011112222 | 핸드폰 |
| 구매자 | 최구매 | 01033334444 | 카카오 |

**Mock 인증번호**: `123456` (모든 핸드폰 인증)

로그인 페이지에서 "테스트 빠른 로그인" 버튼으로 즉시 로그인 가능합니다.

## 주요 기능

### 판매자
- 매장 정보 등록/수정 (매장명, 주소, 전화번호, 영업시간)
- 상품 등록/수정 (상품명, 단위, 카테고리)
- 날짜별 재고 관리 (가격, 수량 설정)
- 주문 현황 확인 (날짜별 필터)
- 판매 통계 대시보드 (일별 매출, 상품별 판매량)

### 구매자
- 매장 목록 조회
- 날짜 선택하여 상품 조회
- 장바구니 + 수량 조절
- 결제 (Mock)
- 주문 내역 확인

### 인증
- 핸드폰 번호 인증 (Mock - 인증번호 123456)
- 카카오톡 OAuth2 로그인 (Mock)
- JWT 기반 Stateless 인증

### 알림
- 주문 시 구매자/판매자에게 알림 (Mock - 콘솔 로그)
- 카카오 사용자: 카카오톡 알림 (Mock)
- 핸드폰 사용자: SMS 알림 (Mock)

## API 엔드포인트

### 인증
- `POST /api/auth/phone/send` - 인증번호 발송
- `POST /api/auth/phone/verify` - 인증번호 확인
- `POST /api/auth/kakao` - 카카오 로그인
- `POST /api/auth/register` - 회원가입
- `GET /api/auth/me` - 현재 사용자 정보

### 판매자
- `POST/GET /api/seller/store` - 매장 등록/조회
- `POST/PUT/GET /api/seller/products` - 상품 CRUD
- `POST/PUT/GET /api/seller/inventory` - 재고 관리
- `GET /api/seller/orders` - 주문 현황
- `GET /api/seller/stats` - 통계

### 구매자
- `GET /api/buyer/stores` - 매장 목록
- `GET /api/buyer/stores/{id}/dates` - 주문 가능 날짜
- `GET /api/buyer/stores/{id}/inventory` - 날짜별 상품
- `POST /api/buyer/orders` - 주문 생성
- `GET /api/buyer/orders` - 주문 내역
