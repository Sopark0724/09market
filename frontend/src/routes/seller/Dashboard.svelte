<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let stats = null;
  let loading = true;
  let error = '';
  let startDate = '';
  let endDate = '';

  onMount(async () => {
    const today = new Date();
    const thirtyDaysAgo = new Date(today);
    thirtyDaysAgo.setDate(today.getDate() - 30);

    startDate = thirtyDaysAgo.toISOString().split('T')[0];
    endDate = today.toISOString().split('T')[0];

    await loadStats();
  });

  async function loadStats() {
    loading = true;
    error = '';
    try {
      stats = await api.get(`/seller/stats?startDate=${startDate}&endDate=${endDate}`);
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }
</script>

<div class="container">
  <h1 class="page-title">📊 판매자 대시보드</h1>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  <!-- 기간 선택 -->
  <div class="card">
    <div class="date-filter">
      <div class="form-group" style="margin-bottom:0">
        <label>시작일</label>
        <input type="date" class="form-control" bind:value={startDate}>
      </div>
      <div class="form-group" style="margin-bottom:0">
        <label>종료일</label>
        <input type="date" class="form-control" bind:value={endDate}>
      </div>
      <button class="btn btn-primary" on:click={loadStats} disabled={loading}>
        {loading ? '조회중...' : '조회'}
      </button>
    </div>
  </div>

  {#if stats}
    <!-- 요약 카드 -->
    <div class="summary-grid">
      <div class="summary-card">
        <div class="summary-label">총 매출</div>
        <div class="summary-value">{formatPrice(stats.totalRevenue)}원</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">총 주문수</div>
        <div class="summary-value">{stats.totalOrders}건</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">평균 주문금액</div>
        <div class="summary-value">
          {stats.totalOrders > 0
            ? formatPrice(Math.round(stats.totalRevenue / stats.totalOrders))
            : 0}원
        </div>
      </div>
    </div>

    <!-- 일별 매출 -->
    <div class="card">
      <h3>📅 일별 매출</h3>
      {#if stats.dailyRevenue && Object.keys(stats.dailyRevenue).length > 0}
        <div class="chart-container">
          {#each Object.entries(stats.dailyRevenue) as [date, revenue]}
            <div class="chart-bar-row">
              <span class="chart-label">{date.substring(5)}</span>
              <div class="chart-bar-bg">
                <div class="chart-bar"
                     style="width: {Math.max(2, (Number(revenue) / Math.max(...Object.values(stats.dailyRevenue).map(Number), 1)) * 100)}%">
                </div>
              </div>
              <span class="chart-value">{formatPrice(revenue)}원</span>
            </div>
          {/each}
        </div>
      {:else}
        <p class="empty-text">해당 기간의 매출 데이터가 없습니다.</p>
      {/if}
    </div>

    <!-- 상품별 판매량 -->
    <div class="card">
      <h3>🏷️ 상품별 판매량</h3>
      {#if stats.productSales && Object.keys(stats.productSales).length > 0}
        <table class="table">
          <thead>
            <tr>
              <th>상품명</th>
              <th>판매 수량</th>
            </tr>
          </thead>
          <tbody>
            {#each Object.entries(stats.productSales).sort((a,b) => b[1] - a[1]) as [name, qty]}
              <tr>
                <td>{name}</td>
                <td><strong>{qty}</strong>개</td>
              </tr>
            {/each}
          </tbody>
        </table>
      {:else}
        <p class="empty-text">해당 기간의 판매 데이터가 없습니다.</p>
      {/if}
    </div>
  {/if}
</div>

<style>
  .date-filter {
    display: flex;
    gap: 16px;
    align-items: flex-end;
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 16px;
  }

  .summary-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  }

  .summary-label {
    font-size: 14px;
    color: #999;
    margin-bottom: 8px;
  }

  .summary-value {
    font-size: 28px;
    font-weight: 700;
    color: #2d8cf0;
  }

  .chart-container {
    margin-top: 16px;
  }

  .chart-bar-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .chart-label {
    width: 60px;
    font-size: 12px;
    color: #666;
    text-align: right;
  }

  .chart-bar-bg {
    flex: 1;
    height: 24px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
  }

  .chart-bar {
    height: 100%;
    background: linear-gradient(90deg, #2d8cf0, #667eea);
    border-radius: 4px;
    transition: width 0.5s;
  }

  .chart-value {
    width: 100px;
    font-size: 12px;
    color: #333;
    text-align: right;
  }

  .empty-text {
    text-align: center;
    color: #999;
    padding: 32px;
  }

  h3 {
    margin-bottom: 16px;
    color: #2c3e50;
  }
</style>
