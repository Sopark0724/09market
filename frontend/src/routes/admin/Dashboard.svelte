<script>
  import { api } from '../../api/client.js';
  import { onMount, tick } from 'svelte';

  export let navigate;

  let sellers = [];
  let loading = true;
  let error = '';
  let selectedSeller = null;
  let sellerStats = null;
  let statsLoading = false;
  let startDate = '';
  let endDate = '';

  const shareColors = ['#2d8cf0', '#19be6b', '#ff9900', '#ed4014', '#764ba2', '#667eea', '#f5a623', '#50e3c2'];

  onMount(async () => {
    const today = new Date();
    const thirtyDaysAgo = new Date(today);
    thirtyDaysAgo.setDate(today.getDate() - 30);
    startDate = thirtyDaysAgo.toISOString().split('T')[0];
    endDate = today.toISOString().split('T')[0];

    await loadSellers();
  });

  async function loadSellers() {
    loading = true;
    try {
      sellers = await api.get('/admin/sellers');
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  async function selectSeller(seller) {
    selectedSeller = seller;
    await loadSellerStats();
  }

  async function loadSellerStats() {
    if (!selectedSeller) return;
    statsLoading = true;
    try {
      sellerStats = await api.get(`/admin/sellers/${selectedSeller.id}/stats?startDate=${startDate}&endDate=${endDate}`);
    } catch (e) {
      error = e.message;
    } finally {
      statsLoading = false;
    }
  }

  function goToSellerStore(storeId) {
    navigate(`#/buyer/store/${storeId}`);
  }

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  $: chartData = sellerStats?.dailyRevenue
    ? Object.entries(sellerStats.dailyRevenue)
        .map(([date, revenue]) => ({ date, revenue: Number(revenue) }))
        .filter(d => d.revenue > 0)
    : [];

  $: maxRevenue = Math.max(...chartData.map(d => d.revenue), 1);
</script>

<div class="container">
  <h1 class="page-title">관리자 대시보드</h1>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else}
    <!-- 판매자 목록 -->
    <div class="card">
      <h3>판매자 관리</h3>
      <div class="seller-grid">
        {#each sellers as seller}
          <div class="seller-card" class:active={selectedSeller?.id === seller.id}
               on:click={() => selectSeller(seller)} on:keypress={() => selectSeller(seller)} role="button" tabindex="0">
            <div class="seller-card-top">
              <span class="seller-name">{seller.name}</span>
              {#if seller.storeActive}
                <span class="badge" style="background:#e8f5e9;color:#2e7d32">운영중</span>
              {:else if seller.storeName}
                <span class="badge" style="background:#f5f5f5;color:#999">비활성</span>
              {:else}
                <span class="badge" style="background:#ffeef0;color:#d32f2f">미등록</span>
              {/if}
            </div>
            <div class="seller-meta">
              {#if seller.storeName}
                <span>🏪 {seller.storeName}</span>
              {/if}
              <span>📞 {seller.phone || '-'}</span>
              <span>📧 {seller.email || '-'}</span>
            </div>
            {#if seller.storeId}
              <button class="btn btn-sm btn-outline" style="margin-top:8px"
                      on:click|stopPropagation={() => goToSellerStore(seller.storeId)}>
                매장 보기 →
              </button>
            {/if}
          </div>
        {/each}
      </div>
    </div>

    <!-- 선택된 판매자 통계 -->
    {#if selectedSeller}
      <div class="card">
        <div class="stats-header">
          <h3>{selectedSeller.name} ({selectedSeller.storeName || '매장 미등록'}) 통계</h3>
          <div class="date-filter">
            <input type="date" class="form-control" bind:value={startDate} style="width:140px">
            <span>~</span>
            <input type="date" class="form-control" bind:value={endDate} style="width:140px">
            <button class="btn btn-sm btn-primary" on:click={loadSellerStats}>조회</button>
          </div>
        </div>

        {#if statsLoading}
          <p style="text-align:center;padding:24px">로딩중...</p>
        {:else if sellerStats}
          <!-- 요약 -->
          <div class="summary-grid">
            <div class="summary-item">
              <span class="summary-label">총 매출</span>
              <span class="summary-value">{formatPrice(sellerStats.totalRevenue)}원</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">총 주문</span>
              <span class="summary-value">{sellerStats.totalOrders}건</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">평균 주문</span>
              <span class="summary-value">
                {sellerStats.totalOrders > 0 ? formatPrice(Math.round(sellerStats.totalRevenue / sellerStats.totalOrders)) : 0}원
              </span>
            </div>
          </div>

          <!-- 일별 매출 차트 -->
          {#if chartData.length > 0}
            <h4>일별 매출</h4>
            <div class="vbar-chart">
              <div class="vbar-body">
                {#each chartData as d}
                  <div class="vbar-col">
                    <div class="vbar-value">{formatPrice(d.revenue)}</div>
                    <div class="vbar-track">
                      <div class="vbar-fill" style="height: {Math.max(4, (d.revenue / maxRevenue) * 100)}%"></div>
                    </div>
                    <div class="vbar-label">{d.date.substring(5)}</div>
                  </div>
                {/each}
              </div>
            </div>
          {/if}

          <!-- 상품별 매출 -->
          {#if sellerStats.productRevenue && Object.keys(sellerStats.productRevenue).length > 0}
            {@const totalProdRev = Object.values(sellerStats.productRevenue).reduce((s, v) => s + Number(v), 0)}
            {@const sorted = Object.entries(sellerStats.productSales || {}).sort((a,b) => Number(sellerStats.productRevenue?.[b[0]] || 0) - Number(sellerStats.productRevenue?.[a[0]] || 0))}

            <h4>상품별 매출</h4>
            <div class="share-bar">
              {#each sorted as [name], i}
                {@const rev = Number(sellerStats.productRevenue?.[name] || 0)}
                {@const pct = totalProdRev > 0 ? (rev / totalProdRev * 100) : 0}
                {#if pct >= 1}
                  <div class="share-segment" style="width:{pct}%;background:{shareColors[i % shareColors.length]}" title="{name} {pct.toFixed(1)}%"></div>
                {/if}
              {/each}
            </div>
            <div class="product-list">
              {#each sorted as [name, qty], i}
                {@const rev = Number(sellerStats.productRevenue?.[name] || 0)}
                {@const pct = totalProdRev > 0 ? (rev / totalProdRev * 100) : 0}
                <div class="prod-row">
                  <span class="prod-color" style="background:{shareColors[i % shareColors.length]}"></span>
                  <span class="prod-name">{name}</span>
                  <span class="prod-rev">{formatPrice(rev)}원</span>
                  <span class="prod-pct">{pct.toFixed(1)}%</span>
                  <span class="prod-qty">{qty}개</span>
                </div>
              {/each}
            </div>
          {/if}
        {/if}
      </div>
    {/if}
  {/if}
</div>

<style>
  h3 {
    margin-bottom: 16px;
    color: #2c3e50;
  }

  h4 {
    margin: 20px 0 12px;
    color: #2c3e50;
    font-size: 15px;
  }

  .seller-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 12px;
  }

  .seller-card {
    border: 2px solid #eee;
    border-radius: 12px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .seller-card:hover {
    border-color: #2d8cf0;
  }

  .seller-card.active {
    border-color: #2d8cf0;
    background: #f0f7ff;
  }

  .seller-card-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .seller-name {
    font-size: 16px;
    font-weight: 600;
  }

  .seller-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    font-size: 13px;
    color: #666;
  }

  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 16px;
  }

  .stats-header h3 {
    margin-bottom: 0;
  }

  .date-filter {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #999;
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .summary-item {
    background: #f8f9fa;
    border-radius: 10px;
    padding: 16px;
    text-align: center;
  }

  .summary-label {
    display: block;
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
  }

  .summary-value {
    font-size: 20px;
    font-weight: 700;
    color: #2d8cf0;
  }

  /* 차트 */
  .vbar-chart {
    overflow-x: auto;
  }

  .vbar-body {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    min-height: 180px;
    padding-top: 16px;
  }

  .vbar-col {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    min-width: 40px;
  }

  .vbar-value {
    font-size: 10px;
    color: #666;
    margin-bottom: 4px;
    white-space: nowrap;
  }

  .vbar-track {
    width: 28px;
    height: 120px;
    background: #f0f0f0;
    border-radius: 6px 6px 0 0;
    display: flex;
    align-items: flex-end;
    overflow: hidden;
  }

  .vbar-fill {
    width: 100%;
    background: linear-gradient(180deg, #2d8cf0, #667eea);
    border-radius: 6px 6px 0 0;
    transition: height 0.5s;
  }

  .vbar-label {
    font-size: 11px;
    color: #666;
    margin-top: 4px;
  }

  /* 비중 바 */
  .share-bar {
    display: flex;
    height: 24px;
    border-radius: 6px;
    overflow: hidden;
    gap: 2px;
  }

  .share-segment {
    min-width: 4px;
    transition: width 0.5s;
  }

  .product-list {
    margin-top: 8px;
  }

  .prod-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 0;
    border-bottom: 1px solid #f5f5f5;
    font-size: 13px;
  }

  .prod-color {
    width: 10px;
    height: 10px;
    border-radius: 2px;
    flex-shrink: 0;
  }

  .prod-name {
    flex: 1;
    font-weight: 500;
  }

  .prod-rev {
    font-weight: 700;
    color: #2c3e50;
  }

  .prod-pct {
    color: #999;
    min-width: 40px;
    text-align: right;
  }

  .prod-qty {
    color: #666;
    min-width: 36px;
    text-align: right;
  }

  @media (max-width: 640px) {
    .seller-grid {
      grid-template-columns: 1fr;
    }

    .stats-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .date-filter {
      flex-wrap: wrap;
    }

    .date-filter input {
      width: 120px !important;
    }

    .summary-grid {
      grid-template-columns: 1fr;
    }

    .summary-value {
      font-size: 18px;
    }
  }
</style>
