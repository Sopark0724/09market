<script>
  export let navigate;
  import { isLoggedIn, isSeller } from '../stores/auth.js';

  $: if ($isLoggedIn) {
    navigate($isSeller ? '#/seller/dashboard' : '#/buyer/stores');
  }
</script>

<div class="landing">
  <section class="hero">
    <div class="hero-content">
      <h1>신선한 농산물,<br/>매일 새벽 산지에서 직송</h1>
      <p class="hero-sub">판매자와 구매자를 직접 연결하는 농산물 도매 플랫폼</p>
      <div class="hero-buttons">
        <button class="btn btn-primary btn-lg" on:click={() => navigate('#/login')}>
          시작하기
        </button>
      </div>
    </div>
  </section>

  <section class="features">
    <div class="container">
      <h2 class="section-title">우리마켓 서비스</h2>
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon">📦</div>
          <h3>판매자</h3>
          <p>날짜별로 상품과 재고를 등록하고 주문 현황을 실시간으로 확인하세요.</p>
          <ul>
            <li>매장 정보 등록</li>
            <li>날짜별 상품/재고 관리</li>
            <li>주문 현황 확인</li>
            <li>매출 통계 대시보드</li>
          </ul>
        </div>
        <div class="feature-card">
          <div class="feature-icon">����</div>
          <h3>구매자</h3>
          <p>원하는 날짜에 신선한 농산물을 편리하게 주문하세요.</p>
          <ul>
            <li>매장별 상품 조회</li>
            <li>날짜 선택 주문</li>
            <li>간편 결제</li>
            <li>주문 알림 수신</li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</div>

<style>
  .hero {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-align: center;
    padding: 80px 20px;
  }

  .hero h1 {
    font-size: 40px;
    font-weight: 700;
    line-height: 1.3;
    margin-bottom: 16px;
  }

  .hero-sub {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 32px;
  }

  .features {
    padding: 60px 0;
  }

  .section-title {
    text-align: center;
    font-size: 28px;
    margin-bottom: 40px;
    color: #2c3e50;
  }

  .feature-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;
    max-width: 800px;
    margin: 0 auto;
  }

  .feature-card {
    background: white;
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
    text-align: center;
  }

  .feature-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .feature-card h3 {
    font-size: 20px;
    margin-bottom: 12px;
    color: #2c3e50;
  }

  .feature-card p {
    color: #666;
    margin-bottom: 16px;
    font-size: 14px;
  }

  .feature-card ul {
    list-style: none;
    text-align: left;
    padding: 0;
  }

  .feature-card li {
    padding: 6px 0;
    font-size: 14px;
    color: #555;
  }

  .feature-card li::before {
    content: '✓ ';
    color: #19be6b;
    font-weight: bold;
  }
</style>
