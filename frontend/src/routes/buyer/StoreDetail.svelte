<script>
  import { api } from '../../api/client.js';
  import { cart } from '../../stores/cart.js';
  import { onMount } from 'svelte';

  export let navigate;
  export let storeId;
  export let params;

  let store = null;
  let availableDates = [];
  let inventories = [];
  let selectedDate = '';
  let loading = true;
  let inventoryLoading = false;
  let error = '';
  let addedMessage = '';

  onMount(async () => {
    try {
      const [storeData, dates] = await Promise.all([
        api.get(`/buyer/stores/${storeId}`),
        api.get(`/buyer/stores/${storeId}/dates`)
      ]);
      store = storeData;
      availableDates = dates;

      if (dates.length > 0) {
        selectedDate = dates[0];
        await loadInventory();
      }
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  });

  async function loadInventory() {
    if (!selectedDate) return;
    inventoryLoading = true;
    error = '';
    try {
      inventories = await api.get(`/buyer/stores/${storeId}/inventory?date=${selectedDate}`);
      cart.setStore(Number(storeId), store?.storeName || '', selectedDate);
    } catch (e) {
      error = e.message;
    } finally {
      inventoryLoading = false;
    }
  }

  function addToCart(inv) {
    cart.addItem(inv);
    addedMessage = `${inv.productName} 추가됨!`;
    setTimeout(() => addedMessage = '', 2000);
  }

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  function formatDate(dateStr) {
    const d = new Date(dateStr);
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return `${d.getMonth()+1}/${d.getDate()} (${days[d.getDay()]})`;
  }
</script>

<div class="container">
  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else if error}
    <div class="alert alert-error">{error}</div>
  {:else if store}
    <!-- 매장 정보 -->
    <div class="card store-info-card">
      <button class="back-btn" on:click={() => navigate('#/buyer/stores')}>← 매장 목록</button>
      <h1>{store.storeName}</h1>
      <div class="store-meta">
        <span>📍 {store.address} {store.addressDetail || ''}</span>
        <span>📞 {store.phone}</span>
        {#if store.businessHours}
          <span>🕐 {store.businessHours}</span>
        {/if}
      </div>
      {#if store.description}
        <p class="store-description">{store.description}</p>
      {/if}
    </div>

    <!-- 날짜 선택 -->
    <div class="card">
      <h3>📅 날짜 선택</h3>
      {#if availableDates.length === 0}
        <p class="empty-text">현재 주문 가능한 날짜가 없습니다.</p>
      {:else}
        <div class="date-chips">
          {#each availableDates as date}
            <button class="date-chip" class:active={selectedDate === date}
                    on:click={() => { selectedDate = date; loadInventory(); }}>
              {formatDate(date)}
            </button>
          {/each}
        </div>
      {/if}
    </div>

    <!-- 상품 목록 -->
    {#if selectedDate}
      <div class="card">
        <div class="section-header">
          <h3>🛒 {formatDate(selectedDate)} 상품 목록</h3>
          {#if addedMessage}
            <span class="added-toast">{addedMessage}</span>
          {/if}
        </div>

        {#if inventoryLoading}
          <p style="text-align:center;padding:24px">로딩중...</p>
        {:else if inventories.length === 0}
          <p class="empty-text">해당 날짜에 판매 가능한 상품이 없습니다.</p>
        {:else}
          <div class="product-list">
            {#each inventories as inv}
              <div class="product-row">
                <div class="product-info">
                  <span class="product-name">{inv.productName}</span>
                  <span class="product-unit">{inv.unit}</span>
                  {#if inv.category}
                    <span class="badge" style="background:#f0f0f0;color:#666;font-size:11px">{inv.category}</span>
                  {/if}
                </div>
                <div class="product-price">
                  <span class="price">{formatPrice(inv.price)}원</span>
                  <span class="stock">잔여 {inv.remainingQuantity}개</span>
                </div>
                <button class="btn btn-primary btn-sm" on:click={() => addToCart(inv)}
                        disabled={inv.remainingQuantity <= 0}>
                  {inv.remainingQuantity > 0 ? '담기' : '품절'}
                </button>
              </div>
            {/each}
          </div>

          <div class="cart-bar">
            <button class="btn btn-success btn-lg" on:click={() => navigate('#/buyer/cart')}>
              장바구니 보기 →
            </button>
          </div>
        {/if}
      </div>
    {/if}
  {/if}
</div>

<style>
  .store-info-card h1 {
    font-size: 28px;
    margin-bottom: 12px;
    color: #2c3e50;
  }

  .back-btn {
    background: none;
    border: none;
    color: #2d8cf0;
    cursor: pointer;
    font-size: 14px;
    padding: 0;
    margin-bottom: 12px;
    font-family: inherit;
  }

  .store-meta {
    display: flex;
    gap: 24px;
    font-size: 14px;
    color: #666;
    flex-wrap: wrap;
  }

  .store-description {
    margin-top: 12px;
    font-size: 14px;
    color: #999;
  }

  h3 {
    margin-bottom: 16px;
    color: #2c3e50;
  }

  .date-chips {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .date-chip {
    padding: 10px 20px;
    border: 2px solid #eee;
    border-radius: 24px;
    background: white;
    cursor: pointer;
    font-size: 14px;
    font-family: inherit;
    transition: all 0.2s;
  }

  .date-chip:hover {
    border-color: #2d8cf0;
  }

  .date-chip.active {
    border-color: #2d8cf0;
    background: #2d8cf0;
    color: white;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .added-toast {
    background: #19be6b;
    color: white;
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 13px;
    animation: fadeIn 0.3s;
  }

  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
  }

  .product-list {
    margin-top: 12px;
  }

  .product-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
  }

  .product-row:last-child {
    border-bottom: none;
  }

  .product-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .product-name {
    font-weight: 600;
    font-size: 15px;
  }

  .product-unit {
    font-size: 13px;
    color: #999;
  }

  .product-price {
    text-align: right;
    margin-right: 16px;
  }

  .price {
    display: block;
    font-size: 16px;
    font-weight: 700;
    color: #2d8cf0;
  }

  .stock {
    font-size: 12px;
    color: #999;
  }

  .cart-bar {
    margin-top: 24px;
    text-align: center;
  }

  .empty-text {
    text-align: center;
    color: #999;
    padding: 32px;
  }
</style>
