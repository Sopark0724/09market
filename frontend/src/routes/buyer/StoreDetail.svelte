<script>
  import { api } from '../../api/client.js';
  import { isLoggedIn } from '../../stores/auth.js';
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
  let orderSuccess = false;
  let ordering = false;
  let notificationMessage = '';
  let selectedProduct = null;
  let paymentMethod = 'CARD';

  // 주문 수량 관리 (inventoryId → quantity)
  let orderQty = {};

  // 마감 임박 기준
  function isAlmostSoldOut(inv) {
    return inv.remainingQuantity > 0 &&
      (inv.remainingQuantity <= 3 || inv.remainingQuantity <= inv.stockQuantity * 0.2);
  }

  // 수량 변경
  function setQty(invId, qty, max) {
    if (qty <= 0) {
      const copy = { ...orderQty };
      delete copy[invId];
      orderQty = copy;
    } else {
      orderQty = { ...orderQty, [invId]: Math.min(qty, max) };
    }
  }

  function increase(inv) {
    const cur = orderQty[inv.id] || 0;
    if (cur < inv.remainingQuantity) {
      setQty(inv.id, cur + 1, inv.remainingQuantity);
    }
  }

  function decrease(inv) {
    const cur = orderQty[inv.id] || 0;
    setQty(inv.id, cur - 1, inv.remainingQuantity);
  }

  // 주문 항목 계산
  $: orderItems = inventories.filter(inv => (orderQty[inv.id] || 0) > 0);
  $: totalAmount = orderItems.reduce((sum, inv) => sum + inv.price * orderQty[inv.id], 0);
  $: totalCount = orderItems.reduce((sum, inv) => sum + orderQty[inv.id], 0);

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
    orderQty = {};
    try {
      inventories = await api.get(`/buyer/stores/${storeId}/inventory?date=${selectedDate}`);
    } catch (e) {
      error = e.message;
    } finally {
      inventoryLoading = false;
    }
  }

  async function placeOrder() {
    if (!$isLoggedIn) {
      navigate('#/login');
      return;
    }
    if (orderItems.length === 0) return;

    ordering = true;
    error = '';
    try {
      const orderRequest = {
        storeId: Number(storeId),
        orderDate: selectedDate,
        paymentMethod,
        items: orderItems.map(inv => ({
          inventoryId: inv.id,
          quantity: orderQty[inv.id]
        }))
      };
      const result = await api.post('/buyer/orders', orderRequest);
      notificationMessage = result.notificationMessage || '';
      orderSuccess = true;
      orderQty = {};
    } catch (e) {
      error = e.message;
    } finally {
      ordering = false;
    }
  }

  function openDetail(inv) {
    selectedProduct = inv;
  }

  function closeDetail() {
    selectedProduct = null;
  }

  function openKakaoMap() {
    if (!store) return;
    const addr = encodeURIComponent(store.address + ' ' + (store.addressDetail || ''));
    window.open(`https://map.kakao.com/?q=${addr}`, '_blank');
  }

  function openNaverMap() {
    if (!store) return;
    const addr = encodeURIComponent(store.address + ' ' + (store.addressDetail || ''));
    window.open(`https://map.naver.com/v5/search/${addr}`, '_blank');
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

<div class="page-wrap" class:has-bottom-bar={totalCount > 0}>
  <div class="container">
    {#if loading}
      <div class="card"><p style="text-align:center">로딩중...</p></div>
    {:else if error && !store}
      <div class="alert alert-error">{error}</div>
    {:else if store}

      <!-- 주문 완료 -->
      {#if orderSuccess}
        <div class="card order-success-card">
          <div class="success-icon">✓</div>
          <h2>주문이 완료되었습니다!</h2>
          <p>{store.storeName} · {formatDate(selectedDate)}</p>

          <!-- 카카오톡 알림 미리보기 -->
          {#if notificationMessage}
            <div class="kakao-preview">
              <div class="kakao-header">
                <span class="kakao-icon">💬</span>
                <span class="kakao-title">카카오톡 알림</span>
                <span class="kakao-badge">발송완료</span>
              </div>
              <div class="kakao-bubble">
                <pre class="kakao-text">{notificationMessage}</pre>
              </div>
            </div>
          {/if}

          <div class="success-actions">
            <button class="btn btn-primary" on:click={() => navigate('#/buyer/orders')}>주문 내역 보기</button>
            <button class="btn btn-outline" on:click={() => { orderSuccess = false; notificationMessage = ''; }}>계속 주문하기</button>
          </div>
        </div>
      {:else}

        <!-- 매장 정보 (접이식) -->
        <div class="card store-info-card">
          <div class="store-top">
            <h1>{store.storeName}</h1>
            <div class="map-buttons">
              <button class="map-chip" on:click={openKakaoMap}>카카오맵</button>
              <button class="map-chip" on:click={openNaverMap}>네이버맵</button>
            </div>
          </div>
          <div class="store-meta">
            <span>📍 {store.address} {store.addressDetail || ''}</span>
            <span>📞 {store.phone}</span>
            {#if store.businessHours}<span>🕐 {store.businessHours}</span>{/if}
          </div>
          {#if store.description}
            <p class="store-description">{store.description}</p>
          {/if}
        </div>

        {#if error}
          <div class="alert alert-error">{error}</div>
        {/if}

        <!-- 날짜 선택 -->
        <div class="date-bar">
          {#each availableDates as date}
            <button class="date-chip" class:active={selectedDate === date}
                    on:click={() => { selectedDate = date; loadInventory(); }}>
              {formatDate(date)}
            </button>
          {/each}
        </div>

        <!-- 주문서 상품 리스트 -->
        {#if inventoryLoading}
          <div class="card"><p style="text-align:center;padding:24px">로딩중...</p></div>
        {:else if inventories.length === 0}
          <div class="card"><p class="empty-text">해당 날짜에 판매 가능한 상품이 없습니다.</p></div>
        {:else}
          <div class="order-list">
            {#each inventories as inv}
              {@const qty = orderQty[inv.id] || 0}
              <div class="order-item" class:selected={qty > 0} class:sold-out={inv.remainingQuantity <= 0}>
                <!-- 이미지 -->
                <div class="item-image" on:click={() => openDetail(inv)} on:keypress={() => openDetail(inv)} role="button" tabindex="0">
                  {#if inv.imageUrl}
                    <img src={inv.imageUrl} alt={inv.productName} />
                  {:else}
                    <div class="no-image">No Image</div>
                  {/if}
                  {#if inv.remainingQuantity <= 0}
                    <div class="img-badge sold-out-badge">품절</div>
                  {:else if isAlmostSoldOut(inv)}
                    <div class="img-badge closing-badge">마감 임박</div>
                  {/if}
                </div>

                <!-- 상품 정보 -->
                <div class="item-info" on:click={() => openDetail(inv)} on:keypress={() => openDetail(inv)} role="button" tabindex="0">
                  <div class="item-name-row">
                    <span class="item-name">{inv.productName}</span>
                    {#if inv.category}
                      <span class="badge cat-badge">{inv.category}</span>
                    {/if}
                  </div>
                  <span class="item-unit">{inv.unit}</span>
                  <div class="item-price-row">
                    <span class="item-price">{formatPrice(inv.price)}원</span>
                    <span class="item-stock" class:stock-low={isAlmostSoldOut(inv)}>
                      잔여 {inv.remainingQuantity}개
                    </span>
                  </div>
                </div>

                <!-- 수량 선택기 -->
                <div class="item-qty">
                  {#if inv.remainingQuantity > 0}
                    {#if qty === 0}
                      <button class="qty-add-btn" on:click={() => increase(inv)}>
                        +담기
                      </button>
                    {:else}
                      <div class="qty-control">
                        <button class="qty-btn" on:click={() => decrease(inv)}>−</button>
                        <span class="qty-value">{qty}</span>
                        <button class="qty-btn" on:click={() => increase(inv)}
                                disabled={qty >= inv.remainingQuantity}>+</button>
                      </div>
                      <span class="item-subtotal">{formatPrice(inv.price * qty)}원</span>
                    {/if}
                  {:else}
                    <span class="sold-out-text">품절</span>
                  {/if}
                </div>
              </div>
            {/each}
          </div>
        {/if}

      {/if}
    {/if}
  </div>
</div>

<!-- 하단 고정 주문 바 -->
{#if totalCount > 0 && !orderSuccess}
  <div class="bottom-bar">
    <div class="bottom-bar-inner">
      {#if error}
        <div class="bottom-error">{error}</div>
      {/if}
      <!-- 주문서 헤더 -->
      <div class="bottom-header">
        <span class="bottom-title">주문서</span>
        <span class="bottom-store">{store?.storeName} · {formatDate(selectedDate)}</span>
      </div>
      <!-- 주문 요약 리스트 -->
      <div class="bottom-items">
        {#each orderItems as inv}
          <div class="bottom-item-row">
            <span class="bottom-item-name">{inv.productName} <span class="bottom-item-unit">{inv.unit}</span></span>
            <div class="bottom-qty-control">
              <button class="bottom-qty-btn" on:click={() => decrease(inv)}>−</button>
              <span class="bottom-qty-val">{orderQty[inv.id]}</span>
              <button class="bottom-qty-btn" on:click={() => increase(inv)} disabled={orderQty[inv.id] >= inv.remainingQuantity}>+</button>
            </div>
            <span class="bottom-item-price">{formatPrice(inv.price * orderQty[inv.id])}원</span>
          </div>
        {/each}
      </div>
      <!-- 합계 + 주문 -->
      <div class="bottom-footer">
        <div class="bottom-summary">
          <span class="bottom-count">{orderItems.length}종 · {totalCount}개</span>
          <span class="bottom-total">{formatPrice(totalAmount)}원</span>
        </div>
        <div class="bottom-actions">
          <select class="payment-select" bind:value={paymentMethod}>
            <option value="CARD">카드</option>
            <option value="TRANSFER">계좌이체</option>
            <option value="KAKAO_PAY">카카오페이</option>
          </select>
          <button class="order-btn" on:click={placeOrder} disabled={ordering}>
            {ordering ? '주문 처리중...' : '주문하기'}
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}

<!-- 상품 상세 모달 -->
{#if selectedProduct}
  <div class="modal-overlay" on:click={closeDetail} on:keypress={closeDetail} role="dialog" tabindex="-1">
    <div class="modal-content" on:click|stopPropagation role="document">
      <button class="modal-close" on:click={closeDetail}>✕</button>
      {#if selectedProduct.imageUrl}
        <div class="modal-image">
          <img src={selectedProduct.imageUrl} alt={selectedProduct.productName} />
          {#if selectedProduct.remainingQuantity <= 0}
            <div class="img-badge sold-out-badge modal-badge-pos">품절</div>
          {:else if isAlmostSoldOut(selectedProduct)}
            <div class="img-badge closing-badge modal-badge-pos">마감 임박</div>
          {/if}
        </div>
      {/if}
      <div class="modal-body">
        <div class="modal-name-row">
          <h2>{selectedProduct.productName}</h2>
          {#if selectedProduct.category}
            <span class="badge cat-badge">{selectedProduct.category}</span>
          {/if}
        </div>
        <p class="modal-unit">{selectedProduct.unit}</p>

        {#if selectedProduct.description}
          <p class="modal-desc">{selectedProduct.description}</p>
        {/if}

        <!-- 상세 이미지 -->
        {#if selectedProduct.detailImages}
          <div class="detail-gallery">
            {#each selectedProduct.detailImages.split(',') as imgUrl}
              <div class="detail-gallery-img">
                <img src={imgUrl} alt="상세 이미지" />
              </div>
            {/each}
          </div>
        {/if}

        <div class="modal-price-row">
          <span class="modal-price">{formatPrice(selectedProduct.price)}원</span>
          <span class="modal-stock" class:stock-low={isAlmostSoldOut(selectedProduct)}>
            잔여 {selectedProduct.remainingQuantity}개
            {#if isAlmostSoldOut(selectedProduct) && selectedProduct.remainingQuantity > 0}
              · 마감 임박
            {/if}
          </span>
        </div>
        {#if selectedProduct.remainingQuantity > 0}
          {@const modalQty = orderQty[selectedProduct.id] || 0}
          <div class="modal-qty-row">
            {#if modalQty === 0}
              <button class="btn btn-primary btn-lg" style="width:100%" on:click={() => { increase(selectedProduct); closeDetail(); }}>
                담기
              </button>
            {:else}
              <div class="modal-qty-control">
                <button class="qty-btn qty-btn-lg" on:click={() => decrease(selectedProduct)}>−</button>
                <span class="qty-value qty-value-lg">{modalQty}</span>
                <button class="qty-btn qty-btn-lg" on:click={() => increase(selectedProduct)}
                        disabled={modalQty >= selectedProduct.remainingQuantity}>+</button>
              </div>
              <div class="modal-subtotal">{formatPrice(selectedProduct.price * modalQty)}원</div>
              <button class="btn btn-outline" style="width:100%;margin-top:8px" on:click={closeDetail}>확인</button>
            {/if}
          </div>
        {:else}
          <button class="btn btn-outline btn-lg" style="width:100%;margin-top:20px" disabled>품절</button>
        {/if}
      </div>
    </div>
  </div>
{/if}

<style>
  /* Page Layout */
  .page-wrap {
    padding-bottom: 20px;
  }

  .page-wrap.has-bottom-bar {
    padding-bottom: 200px;
  }

  /* Store Info */
  .store-info-card {
    padding: 20px;
  }

  .store-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 10px;
  }

  .store-info-card h1 {
    font-size: 24px;
    color: #2c3e50;
    margin: 0;
  }

  .map-buttons {
    display: flex;
    gap: 6px;
    flex-shrink: 0;
  }

  .map-chip {
    padding: 4px 10px;
    border: 1px solid #ddd;
    border-radius: 16px;
    background: white;
    font-size: 11px;
    color: #666;
    cursor: pointer;
    font-family: inherit;
    white-space: nowrap;
  }

  .map-chip:hover {
    border-color: #2d8cf0;
    color: #2d8cf0;
  }

  .store-meta {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #666;
    flex-wrap: wrap;
  }

  .store-description {
    margin-top: 8px;
    font-size: 13px;
    color: #999;
  }

  /* Date Bar */
  .date-bar {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    padding: 12px 0;
    -webkit-overflow-scrolling: touch;
  }

  .date-chip {
    padding: 8px 18px;
    border: 2px solid #eee;
    border-radius: 24px;
    background: white;
    cursor: pointer;
    font-size: 14px;
    font-family: inherit;
    transition: all 0.2s;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .date-chip:hover {
    border-color: #2d8cf0;
  }

  .date-chip.active {
    border-color: #2d8cf0;
    background: #2d8cf0;
    color: white;
  }

  /* Order List */
  .order-list {
    display: flex;
    flex-direction: column;
    gap: 1px;
    background: #eee;
    border-radius: 12px;
    overflow: hidden;
  }

  .order-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: white;
    transition: background 0.2s;
  }

  .order-item.selected {
    background: #f0f7ff;
  }

  .order-item.sold-out {
    opacity: 0.5;
  }

  /* Item Image */
  .item-image {
    width: 64px;
    height: 64px;
    border-radius: 10px;
    overflow: hidden;
    flex-shrink: 0;
    cursor: pointer;
    position: relative;
  }

  .item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .no-image {
    width: 100%;
    height: 100%;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #ccc;
  }

  .img-badge {
    position: absolute;
    top: 4px;
    left: 4px;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 9px;
    font-weight: 700;
  }

  .closing-badge {
    background: #ff9900;
    color: white;
  }

  .sold-out-badge {
    background: rgba(0,0,0,0.6);
    color: white;
  }

  /* Item Info */
  .item-info {
    flex: 1;
    min-width: 0;
    cursor: pointer;
  }

  .item-name-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 2px;
  }

  .item-name {
    font-weight: 600;
    font-size: 15px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .cat-badge {
    background: #f0f0f0;
    color: #666;
    font-size: 10px;
    flex-shrink: 0;
  }

  .item-unit {
    font-size: 12px;
    color: #999;
  }

  .item-price-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
  }

  .item-price {
    font-size: 16px;
    font-weight: 700;
    color: #2d8cf0;
  }

  .item-stock {
    font-size: 11px;
    color: #bbb;
  }

  .stock-low {
    color: #ff9900 !important;
    font-weight: 600;
  }

  /* Quantity Controls */
  .item-qty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
    min-width: 80px;
  }

  .qty-add-btn {
    padding: 8px 16px;
    border: 2px solid #2d8cf0;
    border-radius: 8px;
    background: white;
    color: #2d8cf0;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    font-family: inherit;
    transition: all 0.2s;
  }

  .qty-add-btn:hover {
    background: #2d8cf0;
    color: white;
  }

  .qty-control {
    display: flex;
    align-items: center;
    gap: 0;
    border: 2px solid #2d8cf0;
    border-radius: 8px;
    overflow: hidden;
  }

  .qty-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: white;
    cursor: pointer;
    font-size: 18px;
    font-weight: 500;
    color: #2d8cf0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.15s;
  }

  .qty-btn:hover:not(:disabled) {
    background: #e8f4ff;
  }

  .qty-btn:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  .qty-value {
    width: 28px;
    text-align: center;
    font-weight: 700;
    font-size: 15px;
    color: #2d8cf0;
  }

  .item-subtotal {
    font-size: 12px;
    font-weight: 600;
    color: #333;
  }

  .sold-out-text {
    font-size: 13px;
    color: #999;
    font-weight: 600;
  }

  /* Bottom Fixed Bar */
  .bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: white;
    border-top: 1px solid #eee;
    box-shadow: 0 -4px 16px rgba(0,0,0,0.08);
    z-index: 150;
    padding: 0 16px;
  }

  .bottom-bar-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 12px 0;
  }

  .bottom-error {
    background: #ffeef0;
    color: #d32f2f;
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    margin-bottom: 8px;
    border: 1px solid #ffcdd2;
  }

  .bottom-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 8px;
    border-bottom: 2px solid #2d8cf0;
    margin-bottom: 8px;
  }

  .bottom-title {
    font-size: 15px;
    font-weight: 700;
    color: #2c3e50;
  }

  .bottom-store {
    font-size: 12px;
    color: #999;
  }

  .bottom-items {
    max-height: 120px;
    overflow-y: auto;
    border-bottom: 1px solid #eee;
    padding-bottom: 8px;
    margin-bottom: 8px;
  }

  .bottom-item-row {
    display: flex;
    align-items: center;
    padding: 3px 0;
    font-size: 13px;
    gap: 8px;
  }

  .bottom-item-name {
    flex: 1;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .bottom-item-unit {
    color: #999;
    font-size: 11px;
    font-weight: 400;
  }

  .bottom-qty-control {
    display: flex;
    align-items: center;
    gap: 0;
    border: 1px solid #ddd;
    border-radius: 6px;
    overflow: hidden;
    flex-shrink: 0;
  }

  .bottom-qty-btn {
    width: 26px;
    height: 24px;
    border: none;
    background: #f8f8f8;
    cursor: pointer;
    font-size: 14px;
    color: #2d8cf0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
  }

  .bottom-qty-btn:hover:not(:disabled) {
    background: #e8f4ff;
  }

  .bottom-qty-btn:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  .bottom-qty-val {
    width: 24px;
    text-align: center;
    font-size: 13px;
    font-weight: 700;
    color: #2d8cf0;
  }

  .bottom-item-price {
    color: #333;
    font-weight: 600;
    flex-shrink: 0;
    min-width: 80px;
    text-align: right;
  }

  .bottom-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .bottom-summary {
    display: flex;
    flex-direction: column;
  }

  .bottom-count {
    font-size: 12px;
    color: #999;
  }

  .bottom-total {
    font-size: 22px;
    font-weight: 800;
    color: #2c3e50;
  }

  .bottom-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .payment-select {
    padding: 10px 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 13px;
    font-family: inherit;
    background: white;
    cursor: pointer;
  }

  .order-btn {
    padding: 14px 32px;
    border: none;
    border-radius: 10px;
    background: #19be6b;
    color: white;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    font-family: inherit;
    transition: background 0.2s;
    white-space: nowrap;
  }

  .order-btn:hover:not(:disabled) {
    background: #15a35d;
  }

  .order-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  /* Order Success */
  .order-success-card {
    text-align: center;
    padding: 48px 24px;
  }

  .success-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: #19be6b;
    color: white;
    font-size: 32px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
  }

  .order-success-card h2 {
    font-size: 22px;
    color: #2c3e50;
    margin-bottom: 8px;
  }

  .order-success-card p {
    color: #999;
    font-size: 14px;
    margin-bottom: 24px;
  }

  .success-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
  }

  /* 카카오톡 알림 미리보기 */
  .kakao-preview {
    max-width: 360px;
    margin: 24px auto;
    text-align: left;
  }

  .kakao-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    font-size: 13px;
  }

  .kakao-icon {
    font-size: 18px;
  }

  .kakao-title {
    font-weight: 600;
    color: #333;
  }

  .kakao-badge {
    background: #19be6b;
    color: white;
    font-size: 10px;
    padding: 2px 8px;
    border-radius: 10px;
    font-weight: 600;
  }

  .kakao-bubble {
    background: #FEE500;
    border-radius: 12px;
    padding: 16px;
    position: relative;
  }

  .kakao-bubble::before {
    content: '';
    position: absolute;
    top: -6px;
    left: 20px;
    width: 12px;
    height: 12px;
    background: #FEE500;
    transform: rotate(45deg);
  }

  .kakao-text {
    font-family: 'Noto Sans KR', sans-serif;
    font-size: 13px;
    line-height: 1.7;
    color: #3C1E1E;
    white-space: pre-wrap;
    word-break: break-word;
    margin: 0;
  }

  /* Modal */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 200;
    padding: 20px;
  }

  .modal-content {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    max-width: 480px;
    width: 100%;
    max-height: 90vh;
    overflow-y: auto;
    position: relative;
  }

  .modal-close {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: none;
    background: rgba(0,0,0,0.5);
    color: white;
    font-size: 16px;
    cursor: pointer;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .modal-image {
    width: 100%;
    height: 280px;
    overflow: hidden;
    position: relative;
  }

  .modal-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .modal-badge-pos {
    top: 12px;
    left: 12px;
    font-size: 13px;
    padding: 4px 10px;
  }

  .modal-body {
    padding: 24px;
  }

  .modal-name-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .modal-name-row h2 {
    font-size: 22px;
    color: #2c3e50;
    margin: 0;
  }

  .modal-unit {
    font-size: 14px;
    color: #999;
    margin-bottom: 16px;
  }

  .modal-desc {
    font-size: 14px;
    color: #666;
    line-height: 1.7;
    margin-bottom: 16px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .detail-gallery {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
  }

  .detail-gallery-img {
    width: 100%;
    border-radius: 10px;
    overflow: hidden;
  }

  .detail-gallery-img img {
    width: 100%;
    height: auto;
    display: block;
  }

  .modal-price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .modal-price {
    font-size: 24px;
    font-weight: 700;
    color: #2d8cf0;
  }

  .modal-stock {
    font-size: 14px;
    color: #999;
  }

  .modal-qty-row {
    margin-top: 16px;
  }

  .modal-qty-control {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    border: 2px solid #2d8cf0;
    border-radius: 10px;
    overflow: hidden;
  }

  .qty-btn-lg {
    width: 48px;
    height: 44px;
    font-size: 22px;
  }

  .qty-value-lg {
    width: 60px;
    font-size: 20px;
  }

  .modal-subtotal {
    text-align: center;
    font-size: 18px;
    font-weight: 700;
    color: #333;
    margin-top: 12px;
  }

  .empty-text {
    text-align: center;
    color: #999;
    padding: 32px;
  }

  /* Mobile */
  @media (max-width: 640px) {
    .store-info-card h1 {
      font-size: 20px;
    }

    .store-top {
      flex-direction: column;
      gap: 8px;
    }

    .store-meta {
      flex-direction: column;
      gap: 2px;
      font-size: 12px;
    }

    .order-item {
      padding: 10px 12px;
      gap: 10px;
    }

    .item-image {
      width: 56px;
      height: 56px;
    }

    .item-name {
      font-size: 14px;
    }

    .item-price {
      font-size: 14px;
    }

    .item-qty {
      min-width: 70px;
    }

    .qty-add-btn {
      padding: 6px 12px;
      font-size: 12px;
    }

    .qty-btn {
      width: 28px;
      height: 28px;
      font-size: 16px;
    }

    .qty-value {
      width: 24px;
      font-size: 14px;
    }

    .bottom-total {
      font-size: 18px;
    }

    .order-btn {
      padding: 12px 20px;
      font-size: 14px;
    }

    .payment-select {
      padding: 8px 10px;
      font-size: 12px;
    }

    .date-chip {
      padding: 6px 14px;
      font-size: 13px;
    }

    .modal-image {
      height: 200px;
    }
  }
</style>
