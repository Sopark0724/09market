<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let products = [];
  let inventories = [];
  let selectedDate = new Date().toISOString().split('T')[0];
  let loading = true;
  let error = '';
  let success = '';

  // 새 재고 등록 폼
  let showBulkForm = false;
  let bulkItems = [];

  onMount(async () => {
    await loadProducts();
    await loadInventory();
  });

  async function loadProducts() {
    try {
      products = await api.get('/seller/products');
    } catch (e) {
      error = e.message;
    }
  }

  async function loadInventory() {
    loading = true;
    error = '';
    try {
      inventories = await api.get(`/seller/inventory?date=${selectedDate}`);
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  function addBulkItem() {
    bulkItems = [...bulkItems, {
      productId: products.length > 0 ? products[0].id : '',
      availableDate: selectedDate,
      price: '',
      stockQuantity: ''
    }];
  }

  function removeBulkItem(index) {
    bulkItems = bulkItems.filter((_, i) => i !== index);
  }

  async function saveBulk() {
    const validItems = bulkItems.filter(item => item.productId && item.price && item.stockQuantity);
    if (validItems.length === 0) {
      error = '등록할 재고 항목을 입력해주세요.';
      return;
    }

    error = '';
    success = '';
    try {
      const items = validItems.map(item => ({
        productId: Number(item.productId),
        availableDate: item.availableDate,
        price: Number(item.price),
        stockQuantity: Number(item.stockQuantity)
      }));

      await api.post('/seller/inventory', { items });
      success = `${items.length}개의 재고가 등록되었습니다.`;
      showBulkForm = false;
      bulkItems = [];
      await loadInventory();
    } catch (e) {
      error = e.message;
    }
  }

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  function getStatusLabel(status) {
    switch (status) {
      case 'AVAILABLE': return '판매중';
      case 'SOLD_OUT': return '품절';
      case 'CLOSED': return '마감';
      default: return status;
    }
  }

  function getStatusColor(status) {
    switch (status) {
      case 'AVAILABLE': return '#19be6b';
      case 'SOLD_OUT': return '#ed4014';
      case 'CLOSED': return '#999';
      default: return '#666';
    }
  }
</script>

<div class="container">
  <div class="page-header">
    <h1 class="page-title">📦 재고 관리</h1>
    <button class="btn btn-primary" on:click={() => { showBulkForm = true; if (bulkItems.length === 0) addBulkItem(); }}>
      + 재고 등록
    </button>
  </div>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}
  {#if success}
    <div class="alert alert-success">{success}</div>
  {/if}

  <!-- 날짜 선택 -->
  <div class="card">
    <div class="date-selector">
      <label>날짜 선택</label>
      <input type="date" class="form-control" bind:value={selectedDate}
             on:change={loadInventory} style="width:200px">
    </div>
  </div>

  <!-- 벌크 등록 폼 -->
  {#if showBulkForm}
    <div class="card">
      <h3>재고 일괄 등록</h3>

      {#each bulkItems as item, index}
        <div class="bulk-row">
          <select class="form-control" bind:value={item.productId}>
            {#each products as product}
              <option value={product.id}>{product.name} ({product.unit})</option>
            {/each}
          </select>
          <input type="date" class="form-control" bind:value={item.availableDate}>
          <input type="number" class="form-control" bind:value={item.price} placeholder="가격">
          <input type="number" class="form-control" bind:value={item.stockQuantity} placeholder="수량">
          <button class="btn btn-sm btn-danger" on:click={() => removeBulkItem(index)}>X</button>
        </div>
      {/each}

      <div class="bulk-actions">
        <button class="btn btn-outline btn-sm" on:click={addBulkItem}>+ 항목 추가</button>
        <div>
          <button class="btn btn-primary" on:click={saveBulk}>등록하기</button>
          <button class="btn btn-outline" on:click={() => { showBulkForm = false; bulkItems = []; }}>취소</button>
        </div>
      </div>
    </div>
  {/if}

  <!-- 재고 목록 -->
  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else if inventories.length === 0}
    <div class="card empty">
      <p>{selectedDate}에 등록된 재고가 없습니다.</p>
    </div>
  {:else}
    <div class="card">
      <table class="table">
        <thead>
          <tr>
            <th>상품명</th>
            <th>단위</th>
            <th>카테고리</th>
            <th>날짜</th>
            <th>가격</th>
            <th>전체수량</th>
            <th>잔여수량</th>
            <th>상태</th>
          </tr>
        </thead>
        <tbody>
          {#each inventories as inv}
            <tr>
              <td><strong>{inv.productName}</strong></td>
              <td>{inv.unit}</td>
              <td>{inv.category || '-'}</td>
              <td>{inv.availableDate}</td>
              <td>{formatPrice(inv.price)}원</td>
              <td>{inv.stockQuantity}</td>
              <td>{inv.remainingQuantity}</td>
              <td>
                <span class="status-badge" style="color:{getStatusColor(inv.status)}">
                  {getStatusLabel(inv.status)}
                </span>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<style>
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }

  .page-header .page-title {
    margin-bottom: 0;
  }

  .date-selector {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .date-selector label {
    font-weight: 500;
  }

  h3 {
    margin-bottom: 16px;
    color: #2c3e50;
  }

  .bulk-row {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr auto;
    gap: 8px;
    margin-bottom: 8px;
  }

  .bulk-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 16px;
  }

  .bulk-actions div {
    display: flex;
    gap: 8px;
  }

  .status-badge {
    font-weight: 600;
    font-size: 13px;
  }

  .empty {
    text-align: center;
    padding: 48px;
    color: #999;
  }

  @media (max-width: 640px) {
    .bulk-row {
      grid-template-columns: 1fr 1fr;
      gap: 6px;
    }

    .bulk-row select {
      grid-column: 1 / -1;
    }

    :global(.table) {
      display: block;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }

    .date-selector {
      flex-direction: column;
      align-items: stretch;
    }

    .date-selector input {
      width: 100% !important;
    }
  }
</style>
