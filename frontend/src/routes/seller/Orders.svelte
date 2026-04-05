<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let orders = [];
  let loading = true;
  let error = '';
  let selectedDate = '';
  let phoneSearch = '';

  onMount(loadOrders);

  async function loadOrders() {
    loading = true;
    error = '';
    try {
      const params = [];
      if (selectedDate) params.push(`date=${selectedDate}`);
      if (phoneSearch) params.push(`phone=${phoneSearch}`);
      const query = params.length > 0 ? `?${params.join('&')}` : '';
      orders = await api.get(`/seller/orders${query}`);
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  function getStatusLabel(status) {
    switch (status) {
      case 'PENDING': return '대기';
      case 'CONFIRMED': return '확인';
      case 'COMPLETED': return '픽업완료';
      case 'CANCELLED': return '취소';
      default: return status;
    }
  }

  function getStatusColor(status) {
    switch (status) {
      case 'PENDING': return '#ff9900';
      case 'CONFIRMED': return '#2d8cf0';
      case 'COMPLETED': return '#19be6b';
      case 'CANCELLED': return '#ed4014';
      default: return '#666';
    }
  }

  function formatDateTime(dt) {
    if (!dt) return '-';
    return new Date(dt).toLocaleString('ko-KR');
  }

  async function completeOrder(orderId) {
    if (!confirm('픽업 완료 처리하시겠습니까?')) return;
    error = '';
    try {
      await api.put(`/seller/orders/${orderId}/complete`);
      await loadOrders();
    } catch (e) {
      error = e.message;
    }
  }
</script>

<div class="container">
  <h1 class="page-title">📋 주문 현황</h1>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  <div class="card">
    <div class="filter-row">
      <div class="form-group" style="margin-bottom:0">
        <label>날짜별 조회</label>
        <input type="date" class="form-control" bind:value={selectedDate} style="width:160px">
      </div>
      <div class="form-group" style="margin-bottom:0">
        <label>핸드폰 뒷번호</label>
        <input type="text" class="form-control" bind:value={phoneSearch}
               placeholder="예: 2222" maxlength="4" style="width:120px"
               on:keypress={(e) => { if (e.key === 'Enter') loadOrders(); }}>
      </div>
      <button class="btn btn-primary" on:click={loadOrders}>조회</button>
      <button class="btn btn-outline" on:click={() => { selectedDate = ''; phoneSearch = ''; loadOrders(); }}>초기화</button>
    </div>
  </div>

  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else if orders.length === 0}
    <div class="card empty">
      <p>주문이 없습니다.</p>
    </div>
  {:else}
    {#each orders as order}
      <div class="card order-card">
        <div class="order-header">
          <div>
            <span class="order-id">주문 #{order.id}</span>
            <span class="order-date">{order.orderDate} | {formatDateTime(order.createdAt)}</span>
          </div>
          <span class="status-badge" style="background:{getStatusColor(order.status)}20;color:{getStatusColor(order.status)}">
            {getStatusLabel(order.status)}
          </span>
        </div>

        <div class="order-buyer">
          <span>주문자: <strong>{order.buyerName}</strong></span>
          <span>연락처: {order.buyerPhone || '-'}</span>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th>상품</th>
              <th>단위</th>
              <th>수량</th>
              <th>단가</th>
              <th>소계</th>
            </tr>
          </thead>
          <tbody>
            {#each order.items as item}
              <tr>
                <td>{item.productName}</td>
                <td>{item.unit}</td>
                <td>{item.quantity}</td>
                <td>{formatPrice(item.unitPrice)}원</td>
                <td><strong>{formatPrice(item.subtotal)}원</strong></td>
              </tr>
            {/each}
          </tbody>
        </table>

        <div class="order-footer">
          <div class="order-total">
            합계: <strong>{formatPrice(order.totalAmount)}원</strong>
          </div>
          {#if order.status === 'CONFIRMED' || order.status === 'PENDING'}
            <button class="btn btn-sm btn-success" on:click={() => completeOrder(order.id)}>픽업 완료</button>
          {/if}
        </div>
      </div>
    {/each}
  {/if}
</div>

<style>
  .filter-row {
    display: flex;
    align-items: flex-end;
    gap: 12px;
  }

  .order-card {
    padding: 20px;
  }

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .order-id {
    font-weight: 700;
    font-size: 16px;
    margin-right: 12px;
  }

  .order-date {
    font-size: 13px;
    color: #999;
  }

  .status-badge {
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 13px;
    font-weight: 600;
  }

  .order-buyer {
    display: flex;
    gap: 24px;
    font-size: 14px;
    color: #555;
    margin-bottom: 12px;
    padding: 8px 12px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 2px solid #eee;
    margin-top: 12px;
  }

  .order-total {
    font-size: 16px;
  }

  .empty {
    text-align: center;
    padding: 48px;
    color: #999;
  }

  @media (max-width: 640px) {
    .filter-row {
      flex-direction: column;
      align-items: stretch;
    }

    .filter-row input {
      width: 100% !important;
    }

    .order-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    .order-buyer {
      flex-direction: column;
      gap: 4px;
    }

    :global(.table) {
      display: block;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
  }
</style>
