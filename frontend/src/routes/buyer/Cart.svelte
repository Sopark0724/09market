<script>
  import { api } from '../../api/client.js';
  import { cart, cartTotal } from '../../stores/cart.js';

  export let navigate;

  let loading = false;
  let error = '';
  let paymentMethod = 'CARD';

  function formatPrice(num) {
    return Number(num).toLocaleString();
  }

  async function checkout() {
    const $cart = getCurrentCart();
    if ($cart.items.length === 0) {
      error = '장바구니가 비어있습니다.';
      return;
    }

    loading = true;
    error = '';
    try {
      const orderRequest = {
        storeId: $cart.storeId,
        orderDate: $cart.orderDate,
        paymentMethod: paymentMethod,
        items: $cart.items.map(item => ({
          inventoryId: item.inventoryId,
          quantity: item.quantity
        }))
      };

      await api.post('/buyer/orders', orderRequest);
      cart.clear();
      navigate('#/buyer/orders');
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  function getCurrentCart() {
    let value;
    cart.subscribe(v => value = v)();
    return value;
  }
</script>

<div class="container">
  <h1 class="page-title">🛒 장바구니</h1>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if $cart.items.length === 0}
    <div class="card empty">
      <p>장바구니가 비어있습니다.</p>
      <button class="btn btn-primary" on:click={() => navigate('#/buyer/stores')}>매장 둘러보기</button>
    </div>
  {:else}
    <div class="card">
      <div class="cart-header">
        <h3>{$cart.storeName}</h3>
        <span class="order-date">주문 날짜: {$cart.orderDate}</span>
      </div>

      <div class="cart-items">
        {#each $cart.items as item}
          <div class="cart-item">
            <div class="item-info">
              <span class="item-name">{item.productName}</span>
              <span class="item-unit">{item.unit}</span>
              <span class="item-price">{formatPrice(item.price)}원</span>
            </div>
            <div class="item-controls">
              <button class="qty-btn" on:click={() => cart.updateQuantity(item.inventoryId, item.quantity - 1)}
                      disabled={item.quantity <= 1}>-</button>
              <span class="qty-value">{item.quantity}</span>
              <button class="qty-btn" on:click={() => cart.updateQuantity(item.inventoryId, item.quantity + 1)}
                      disabled={item.quantity >= item.maxQuantity}>+</button>
            </div>
            <div class="item-subtotal">
              {formatPrice(item.price * item.quantity)}원
            </div>
            <button class="btn btn-sm btn-danger" on:click={() => cart.removeItem(item.inventoryId)}>삭제</button>
          </div>
        {/each}
      </div>

      <div class="cart-total">
        <span>합계</span>
        <span class="total-amount">{formatPrice($cartTotal)}원</span>
      </div>
    </div>

    <!-- 결제 -->
    <div class="card">
      <h3>💳 결제</h3>

      <div class="form-group">
        <label>결제 방법</label>
        <select class="form-control" bind:value={paymentMethod}>
          <option value="CARD">카드 결제</option>
          <option value="TRANSFER">계좌이체</option>
          <option value="KAKAO_PAY">카카오페이</option>
        </select>
      </div>

      <div class="alert alert-info">
        Mock 결제입니다. 실제 결제가 이루어지지 않습니다.
      </div>

      <button class="btn btn-success btn-lg" style="width:100%" on:click={checkout} disabled={loading}>
        {loading ? '결제 처리중...' : `${formatPrice($cartTotal)}원 결제하기`}
      </button>
    </div>
  {/if}
</div>

<style>
  .cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #eee;
  }

  .cart-header h3 {
    margin-bottom: 0;
    color: #2c3e50;
  }

  .order-date {
    font-size: 14px;
    color: #666;
  }

  h3 {
    margin-bottom: 16px;
    color: #2c3e50;
  }

  .cart-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    gap: 16px;
  }

  .item-info {
    flex: 1;
  }

  .item-name {
    font-weight: 600;
    display: block;
  }

  .item-unit {
    font-size: 12px;
    color: #999;
    margin-right: 8px;
  }

  .item-price {
    font-size: 13px;
    color: #666;
  }

  .item-controls {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .qty-btn {
    width: 32px;
    height: 32px;
    border: 1px solid #ddd;
    border-radius: 6px;
    background: white;
    cursor: pointer;
    font-size: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .qty-btn:hover:not(:disabled) {
    border-color: #2d8cf0;
    color: #2d8cf0;
  }

  .qty-btn:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  .qty-value {
    width: 40px;
    text-align: center;
    font-weight: 600;
    font-size: 16px;
  }

  .item-subtotal {
    font-weight: 700;
    font-size: 16px;
    min-width: 100px;
    text-align: right;
  }

  .cart-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20px;
    margin-top: 16px;
    border-top: 2px solid #333;
    font-size: 18px;
    font-weight: 600;
  }

  .total-amount {
    font-size: 24px;
    color: #2d8cf0;
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
    .cart-item {
      flex-wrap: wrap;
      gap: 8px;
    }

    .item-info {
      width: 100%;
    }

    .item-subtotal {
      min-width: auto;
    }

    .cart-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 4px;
    }

    .total-amount {
      font-size: 20px;
    }
  }
</style>
