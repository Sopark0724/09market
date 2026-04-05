<script>
  import { api } from '../../api/client.js';
  import { onMount, tick } from 'svelte';

  export let navigate;

  let stats = null;
  let loading = true;
  let error = '';
  let startDate = '';
  let endDate = '';
  let chartView = 'bar'; // 'bar' or 'line'
  let selectedDate = null;
  let dailyDetail = null;
  let dailyDetailLoading = false;
  const shareColors = ['#2d8cf0', '#19be6b', '#ff9900', '#ed4014', '#764ba2', '#667eea', '#f5a623', '#50e3c2'];

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
      await tick();
      if (chartView === 'line') drawLineChart();
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  // 매출 있는 날만 필터
  $: chartData = stats?.dailyRevenue
    ? Object.entries(stats.dailyRevenue)
        .map(([date, revenue]) => ({ date, revenue: Number(revenue), orders: Number(stats.dailyOrderCount?.[date] || 0) }))
        .filter(d => d.revenue > 0)
    : [];

  $: maxRevenue = Math.max(...chartData.map(d => d.revenue), 1);

  // Canvas 라인 차트
  let canvasEl;

  function switchView(view) {
    chartView = view;
    if (view === 'line') {
      tick().then(() => drawLineChart());
    }
  }

  async function selectDate(date) {
    if (selectedDate === date) {
      selectedDate = null;
      dailyDetail = null;
      return;
    }
    selectedDate = date;
    dailyDetailLoading = true;
    try {
      dailyDetail = await api.get(`/seller/stats/daily?date=${date}`);
    } catch (e) {
      dailyDetail = null;
    } finally {
      dailyDetailLoading = false;
    }
  }

  function drawLineChart() {
    if (!canvasEl || chartData.length === 0) return;

    const canvas = canvasEl;
    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    const W = rect.width;
    const H = rect.height;
    const pad = { top: 20, right: 20, bottom: 40, left: 60 };
    const cW = W - pad.left - pad.right;
    const cH = H - pad.top - pad.bottom;

    ctx.clearRect(0, 0, W, H);

    const max = maxRevenue * 1.1;
    const data = chartData;
    const n = data.length;
    if (n === 0) return;

    const stepX = n > 1 ? cW / (n - 1) : cW / 2;

    // Grid lines
    ctx.strokeStyle = '#eee';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = pad.top + cH - (cH * i / 4);
      ctx.beginPath();
      ctx.moveTo(pad.left, y);
      ctx.lineTo(pad.left + cW, y);
      ctx.stroke();

      ctx.fillStyle = '#999';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(formatPrice(Math.round(max * i / 4)), pad.left - 8, y + 4);
    }

    // X labels
    ctx.fillStyle = '#999';
    ctx.font = '11px sans-serif';
    ctx.textAlign = 'center';
    const labelStep = Math.max(1, Math.floor(n / 8));
    data.forEach((d, i) => {
      if (i % labelStep === 0 || i === n - 1) {
        const x = pad.left + (n > 1 ? stepX * i : cW / 2);
        ctx.fillText(d.date.substring(5), x, H - pad.bottom + 18);
      }
    });

    // Area fill
    ctx.beginPath();
    data.forEach((d, i) => {
      const x = pad.left + (n > 1 ? stepX * i : cW / 2);
      const y = pad.top + cH - (d.revenue / max) * cH;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    const lastX = pad.left + (n > 1 ? stepX * (n - 1) : cW / 2);
    ctx.lineTo(lastX, pad.top + cH);
    ctx.lineTo(pad.left, pad.top + cH);
    ctx.closePath();
    ctx.fillStyle = 'rgba(45, 140, 240, 0.1)';
    ctx.fill();

    // Line
    ctx.beginPath();
    data.forEach((d, i) => {
      const x = pad.left + (n > 1 ? stepX * i : cW / 2);
      const y = pad.top + cH - (d.revenue / max) * cH;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.strokeStyle = '#2d8cf0';
    ctx.lineWidth = 2.5;
    ctx.lineJoin = 'round';
    ctx.stroke();

    // Dots
    data.forEach((d, i) => {
      const x = pad.left + (n > 1 ? stepX * i : cW / 2);
      const y = pad.top + cH - (d.revenue / max) * cH;
      ctx.beginPath();
      ctx.arc(x, y, 4, 0, Math.PI * 2);
      ctx.fillStyle = '#2d8cf0';
      ctx.fill();
      ctx.strokeStyle = 'white';
      ctx.lineWidth = 2;
      ctx.stroke();
    });
  }
</script>

<div class="container">
  <h1 class="page-title">판매자 대시보드</h1>

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

    <!-- 일별 매출 차트 -->
    <div class="card">
      <div class="chart-header">
        <h3>일별 매출</h3>
        <div class="chart-toggle">
          <button class="toggle-btn" class:active={chartView === 'bar'} on:click={() => switchView('bar')}>막대</button>
          <button class="toggle-btn" class:active={chartView === 'line'} on:click={() => switchView('line')}>라인</button>
        </div>
      </div>

      {#if chartData.length > 0}
        {#if chartView === 'bar'}
          <!-- 세로 막대 차트 -->
          <div class="vbar-chart">
            <div class="vbar-body">
              {#each chartData as d}
                <div class="vbar-col clickable" class:selected={selectedDate === d.date} on:click={() => selectDate(d.date)}>
                  <div class="vbar-value">{formatPrice(d.revenue)}</div>
                  <div class="vbar-track">
                    <div class="vbar-fill" style="height: {Math.max(4, (d.revenue / maxRevenue) * 100)}%"></div>
                  </div>
                  <div class="vbar-label">{d.date.substring(5)}</div>
                  {#if d.orders > 0}
                    <div class="vbar-orders">{d.orders}건</div>
                  {/if}
                </div>
              {/each}
            </div>
          </div>
        {:else}
          <!-- 라인 차트 -->
          <div class="line-chart-wrap">
            <canvas bind:this={canvasEl} class="line-canvas"></canvas>
          </div>
        {/if}
      {:else}
        <p class="empty-text">해당 기간의 매출 데이터가 없습니다.</p>
      {/if}
    </div>

    <!-- 날짜별 상세 -->
    {#if selectedDate}
      <div class="card daily-detail-card">
        <div class="detail-header">
          <h3>{selectedDate} 판매 상세</h3>
          <button class="close-btn" on:click={() => { selectedDate = null; dailyDetail = null; }}>&times;</button>
        </div>

        {#if dailyDetailLoading}
          <p class="empty-text">로딩중...</p>
        {:else if dailyDetail}
          <div class="detail-summary">
            <div class="detail-summary-item">
              <span class="detail-label">주문수</span>
              <span class="detail-value">{dailyDetail.totalOrders}건</span>
            </div>
            <div class="detail-summary-item">
              <span class="detail-label">총 매출</span>
              <span class="detail-value highlight">{formatPrice(dailyDetail.totalRevenue)}원</span>
            </div>
          </div>

          {#if dailyDetail.products && dailyDetail.products.length > 0}
            <div class="detail-table-wrap">
              <table class="detail-table">
                <thead>
                  <tr>
                    <th>품목</th>
                    <th>단위</th>
                    <th>카테고리</th>
                    <th class="right">등록수량</th>
                    <th class="right">판매수량</th>
                    <th class="right">단가</th>
                    <th class="right">매출액</th>
                  </tr>
                </thead>
                <tbody>
                  {#each dailyDetail.products as p}
                    <tr>
                      <td class="product-name">{p.productName}</td>
                      <td>{p.unit}</td>
                      <td><span class="category-badge">{p.category || '-'}</span></td>
                      <td class="right">{p.stockQuantity}개</td>
                      <td class="right sold">{p.soldQuantity}개</td>
                      <td class="right">{formatPrice(p.unitPrice)}원</td>
                      <td class="right revenue">{formatPrice(p.revenue)}원</td>
                    </tr>
                  {/each}
                </tbody>
                <tfoot>
                  <tr>
                    <td colspan="4">합계</td>
                    <td class="right sold">{dailyDetail.products.reduce((s, p) => s + p.soldQuantity, 0)}개</td>
                    <td></td>
                    <td class="right revenue">{formatPrice(dailyDetail.totalRevenue)}원</td>
                  </tr>
                </tfoot>
              </table>
            </div>
          {:else}
            <p class="empty-text">해당 날짜의 판매 데이터가 없습니다.</p>
          {/if}
        {/if}
      </div>
    {/if}

    <!-- 상품별 매출 분석 -->
    <div class="card">
      <h3>상품별 매출 분석</h3>
      {#if stats.productSales && Object.keys(stats.productSales).length > 0}
        {@const totalProductRevenue = Object.values(stats.productRevenue || {}).reduce((s, v) => s + Number(v), 0)}
        {@const sortedProducts = Object.entries(stats.productSales).sort((a,b) => Number(stats.productRevenue?.[b[0]] || 0) - Number(stats.productRevenue?.[a[0]] || 0))}

        <!-- 비중 바 (간이 도넛 대체) -->
        <div class="share-bar">
          {#each sortedProducts as [name, qty], i}
            {@const rev = Number(stats.productRevenue?.[name] || 0)}
            {@const pct = totalProductRevenue > 0 ? (rev / totalProductRevenue * 100) : 0}
            {#if pct >= 1}
              <div class="share-segment" style="width:{pct}%;background:{shareColors[i % shareColors.length]}"
                   title="{name} {pct.toFixed(1)}%"></div>
            {/if}
          {/each}
        </div>

        <!-- 범례 + 상세 -->
        <div class="product-sales">
          {#each sortedProducts as [name, qty], i}
            {@const rev = Number(stats.productRevenue?.[name] || 0)}
            {@const pct = totalProductRevenue > 0 ? (rev / totalProductRevenue * 100) : 0}
            <div class="sale-row">
              <span class="sale-color" style="background:{shareColors[i % shareColors.length]}"></span>
              <span class="sale-rank">#{i + 1}</span>
              <span class="sale-name">{name}</span>
              <div class="sale-detail">
                <span class="sale-revenue">{formatPrice(rev)}원</span>
                <span class="sale-pct">{pct.toFixed(1)}%</span>
              </div>
              <span class="sale-qty">{qty}개</span>
            </div>
          {/each}
        </div>
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

  h3 {
    margin-bottom: 0;
    color: #2c3e50;
  }

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .chart-toggle {
    display: flex;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
  }

  .toggle-btn {
    padding: 6px 14px;
    border: none;
    background: white;
    font-size: 13px;
    cursor: pointer;
    font-family: inherit;
    color: #666;
    transition: all 0.2s;
  }

  .toggle-btn.active {
    background: #2d8cf0;
    color: white;
  }

  /* 세로 막대 차트 */
  .vbar-chart {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .vbar-body {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    min-height: 220px;
    padding-top: 20px;
  }

  .vbar-col {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    min-width: 48px;
  }

  .vbar-value {
    font-size: 10px;
    color: #666;
    margin-bottom: 4px;
    white-space: nowrap;
  }

  .vbar-track {
    width: 32px;
    height: 160px;
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
    transition: height 0.5s ease;
  }

  .vbar-label {
    font-size: 11px;
    color: #666;
    margin-top: 6px;
  }

  .vbar-orders {
    font-size: 10px;
    color: #19be6b;
    font-weight: 600;
  }

  /* 라인 차트 */
  .line-chart-wrap {
    width: 100%;
    height: 280px;
  }

  .line-canvas {
    width: 100%;
    height: 100%;
  }

  /* 비중 바 */
  .share-bar {
    display: flex;
    height: 28px;
    border-radius: 8px;
    overflow: hidden;
    margin: 16px 0;
    gap: 2px;
  }

  .share-segment {
    transition: width 0.5s;
    min-width: 4px;
  }

  /* 상품별 판매량 */
  .product-sales {
    margin-top: 8px;
  }

  .sale-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;
  }

  .sale-row:last-child {
    border-bottom: none;
  }

  .sale-color {
    width: 12px;
    height: 12px;
    border-radius: 3px;
    flex-shrink: 0;
  }

  .sale-rank {
    font-size: 13px;
    font-weight: 700;
    color: #999;
    width: 24px;
    text-align: center;
    flex-shrink: 0;
  }

  .sale-name {
    font-size: 14px;
    font-weight: 500;
    flex: 1;
    min-width: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .sale-detail {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .sale-revenue {
    font-size: 14px;
    font-weight: 700;
    color: #2c3e50;
  }

  .sale-pct {
    font-size: 11px;
    color: #999;
  }

  .sale-qty {
    font-size: 13px;
    color: #666;
    min-width: 40px;
    text-align: right;
    flex-shrink: 0;
  }

  .empty-text {
    text-align: center;
    color: #999;
    padding: 32px;
  }

  /* 클릭 가능한 막대 */
  .vbar-col.clickable {
    cursor: pointer;
    border-radius: 8px;
    padding: 4px 2px;
    transition: background 0.2s;
  }

  .vbar-col.clickable:hover {
    background: rgba(45, 140, 240, 0.06);
  }

  .vbar-col.selected {
    background: rgba(45, 140, 240, 0.12);
  }

  .vbar-col.selected .vbar-fill {
    background: linear-gradient(180deg, #ff9900, #ed4014);
  }

  .vbar-col.selected .vbar-label {
    color: #2d8cf0;
    font-weight: 700;
  }

  /* 날짜별 상세 카드 */
  .daily-detail-card {
    border: 2px solid #2d8cf0;
    animation: slideDown 0.25s ease;
  }

  @keyframes slideDown {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
  }

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .detail-header h3 {
    margin: 0;
    color: #2d8cf0;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 24px;
    color: #999;
    cursor: pointer;
    padding: 0 4px;
    line-height: 1;
  }

  .close-btn:hover {
    color: #333;
  }

  .detail-summary {
    display: flex;
    gap: 24px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .detail-summary-item {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .detail-label {
    font-size: 13px;
    color: #999;
  }

  .detail-value {
    font-size: 16px;
    font-weight: 700;
    color: #2c3e50;
  }

  .detail-value.highlight {
    color: #2d8cf0;
    font-size: 18px;
  }

  .detail-table-wrap {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .detail-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
  }

  .detail-table th {
    background: #f8f9fa;
    padding: 10px 12px;
    font-weight: 600;
    color: #666;
    border-bottom: 2px solid #e9ecef;
    white-space: nowrap;
  }

  .detail-table td {
    padding: 10px 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .detail-table .right {
    text-align: right;
  }

  .detail-table .product-name {
    font-weight: 600;
    color: #2c3e50;
  }

  .detail-table .sold {
    color: #19be6b;
    font-weight: 600;
  }

  .detail-table .revenue {
    color: #2d8cf0;
    font-weight: 700;
  }

  .detail-table tfoot td {
    font-weight: 700;
    border-top: 2px solid #e9ecef;
    background: #f8f9fa;
  }

  .category-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    background: #f0f0f0;
    font-size: 12px;
    color: #666;
  }

  @media (max-width: 640px) {
    .date-filter {
      flex-direction: column;
      align-items: stretch;
    }

    .summary-grid {
      grid-template-columns: 1fr;
    }

    .summary-value {
      font-size: 22px;
    }

    .vbar-track {
      height: 120px;
      width: 24px;
    }

    .vbar-col {
      min-width: 36px;
    }

    .line-chart-wrap {
      height: 200px;
    }

    .sale-name {
      min-width: 60px;
      font-size: 13px;
    }
  }
</style>
