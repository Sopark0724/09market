<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let orders = [];
  let loading = true;
  let error = '';

  onMount(async () => {
    try {
      orders = await api.get('/buyer/orders');
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  });

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  function getStatusLabel(status) {
    switch (status) {
      case 'PENDING': return '대기중';
      case 'CONFIRMED': return '주문확인';
      case 'COMPLETED': return '픽업완료';
      case 'CANCELLED': return '취소됨';
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

  async function cancelOrder(orderId) {
    if (!confirm('주문을 취소하시겠습니까?')) return;
    error = '';
    try {
      await api.put(`/buyer/orders/${orderId}/cancel`);
      orders = await api.get('/buyer/orders');
    } catch (e) {
      error = e.message;
    }
  }
</script>

<div class="container">
  <h1 class="page-title">📦 주문 내역</h1>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else if orders.length === 0}
    <div class="card empty">
      <p>주문 내역이 없습니다.</p>
      <button class="btn btn-primary" on:click={() => navigate('#/buyer/stores')}>매장 둘러보기</button>
    </div>
  {:else}
    {#each orders as order}
      <div class="card order-card">
        <div class="order-header">
          <div>
            <span class="order-id">주문 #{order.id}</span>
            <span class="order-store">{order.storeName}</span>
          </div>
          <span class="status-badge" style="background:{getStatusColor(order.status)}20;color:{getStatusColor(order.status)}">
            {getStatusLabel(order.status)}
          </span>
        </div>

        <div class="order-meta">
          <span>📅 주문일: {order.orderDate}</span>
          <span>⏱ {formatDateTime(order.createdAt)}</span>
        </div>

        <div class="order-items">
          {#each order.items as item}
            <div class="order-item-row">
              <span class="item-name">{item.productName}</span>
              <span class="item-detail">{item.unit} × {item.quantity}</span>
              <span class="item-price">{formatPrice(item.subtotal)}원</span>
            </div>
          {/each}
        </div>

        <div class="order-footer">
          <div class="order-total">
            합계: <strong>{formatPrice(order.totalAmount)}원</strong>
          </div>
          {#if order.status !== 'CANCELLED' && order.status !== 'COMPLETED'}
            <button class="btn btn-sm btn-danger" on:click={() => cancelOrder(order.id)}>주문 취소</button>
          {/if}
        </div>
      </div>
    {/each}
  {/if}
</div>

<style>
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

  .order-store {
    font-size: 14px;
    color: #2d8cf0;
  }

  .status-badge {
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 13px;
    font-weight: 600;
  }

  .order-meta {
    display: flex;
    gap: 24px;
    font-size: 13px;
    color: #999;
    margin-bottom: 16px;
  }

  .order-items {
    border-top: 1px solid #f0f0f0;
    padding-top: 12px;
  }

  .order-item-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    font-size: 14px;
  }

  .item-name {
    flex: 1;
    font-weight: 500;
  }

  .item-detail {
    color: #999;
    margin: 0 16px;
  }

  .item-price {
    font-weight: 600;
    min-width: 100px;
    text-align: right;
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

  .empty p {
    margin-bottom: 16px;
  }

  @media (max-width: 640px) {
    .order-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    .order-meta {
      flex-direction: column;
      gap: 4px;
    }

    .order-item-row {
      flex-wrap: wrap;
    }

    .item-detail {
      margin: 0;
    }

    .item-price {
      min-width: auto;
    }
  }
</style>
