<script>
  import { user, isLoggedIn, isSeller, isBuyer, isAdmin } from './stores/auth.js';
  import { cartItemCount } from './stores/cart.js';
  import Login from './routes/auth/Login.svelte';
  import Register from './routes/auth/Register.svelte';
  import SellerDashboard from './routes/seller/Dashboard.svelte';
  import SellerStore from './routes/seller/StoreManage.svelte';
  import SellerProducts from './routes/seller/Products.svelte';
  import SellerInventory from './routes/seller/Inventory.svelte';
  import SellerOrders from './routes/seller/Orders.svelte';
  import AdminDashboard from './routes/admin/Dashboard.svelte';
  import AdminStores from './routes/buyer/Stores.svelte';
  import BuyerStoreDetail from './routes/buyer/StoreDetail.svelte';
  import BuyerCart from './routes/buyer/Cart.svelte';
  import BuyerOrders from './routes/buyer/Orders.svelte';
  import Landing from './routes/Landing.svelte';

  let currentHash = window.location.hash || '#/';
  let mobileMenuOpen = false;

  function navigate(hash) {
    window.location.hash = hash;
    currentHash = hash;
    mobileMenuOpen = false;
  }

  window.addEventListener('hashchange', () => {
    currentHash = window.location.hash || '#/';
  });

  function logout() {
    user.logout();
    navigate('#/');
  }

  function toggleMenu() {
    mobileMenuOpen = !mobileMenuOpen;
  }

  $: route = currentHash.split('?')[0];
  $: params = new URLSearchParams(currentHash.includes('?') ? currentHash.split('?')[1] : '');

  // 구매자가 매장 링크로 접근한 경우 로고 클릭 시 해당 매장으로 이동
  let buyerStoreRoute = '';
  $: if (route.startsWith('#/buyer/store/')) {
    buyerStoreRoute = route;
  }

  function logoClick() {
    if (buyerStoreRoute && (!$isLoggedIn || $isBuyer)) {
      navigate(buyerStoreRoute);
    } else {
      navigate('#/');
    }
  }
</script>

<div class="app">
  <!-- Header -->
  <header class="header">
    <div class="header-content">
      <a href="#/" class="logo" on:click|preventDefault={logoClick}>
        <span class="logo-icon">🥬</span>
        <span class="logo-text">우리마켓</span>
      </a>

      {#if $isLoggedIn}
        <button class="menu-toggle" on:click={toggleMenu}>☰</button>
      {/if}

      <nav class="nav" class:open={mobileMenuOpen}>
        {#if $isLoggedIn}
          {#if $isAdmin}
            <a href="#/admin/dashboard" class="nav-link" class:active={route === '#/admin/dashboard'} on:click|preventDefault={() => navigate('#/admin/dashboard')}>판매자 관리</a>
            <a href="#/admin/stores" class="nav-link" class:active={route === '#/admin/stores'} on:click|preventDefault={() => navigate('#/admin/stores')}>매장 목록</a>
          {:else if $isSeller}
            <a href="#/seller/dashboard" class="nav-link" class:active={route === '#/seller/dashboard'} on:click|preventDefault={() => navigate('#/seller/dashboard')}>대시보드</a>
            <a href="#/seller/store" class="nav-link" class:active={route === '#/seller/store'} on:click|preventDefault={() => navigate('#/seller/store')}>매장관리</a>
            <a href="#/seller/products" class="nav-link" class:active={route === '#/seller/products'} on:click|preventDefault={() => navigate('#/seller/products')}>상품관리</a>
            <a href="#/seller/inventory" class="nav-link" class:active={route === '#/seller/inventory'} on:click|preventDefault={() => navigate('#/seller/inventory')}>재고관리</a>
            <a href="#/seller/orders" class="nav-link" class:active={route === '#/seller/orders'} on:click|preventDefault={() => navigate('#/seller/orders')}>주문현황</a>
          {:else}
            <a href="#/buyer/orders" class="nav-link" class:active={route === '#/buyer/orders'} on:click|preventDefault={() => navigate('#/buyer/orders')}>주문내역</a>
            <a href="#/buyer/cart" class="nav-link cart-link" class:active={route === '#/buyer/cart'} on:click|preventDefault={() => navigate('#/buyer/cart')}>
              장바구니
              {#if $cartItemCount > 0}
                <span class="cart-badge">{$cartItemCount}</span>
              {/if}
            </a>
          {/if}

          <div class="user-info">
            <span class="user-name">{$user?.name}님</span>
            <span class="user-role badge">{$user?.role === 'ADMIN' ? '관리자' : $user?.role === 'SELLER' ? '판매자' : '구매자'}</span>
            <button class="btn btn-sm btn-outline" on:click={logout}>로그아웃</button>
          </div>
        {:else}
          <a href="#/login" class="nav-link" on:click|preventDefault={() => navigate('#/login')}>로그인</a>
        {/if}
      </nav>
    </div>
  </header>

  <!-- Mobile menu backdrop -->
  <div class="menu-backdrop" class:open={mobileMenuOpen}
       on:click={() => mobileMenuOpen = false}
       on:keypress={() => mobileMenuOpen = false}
       role="button" tabindex="-1"></div>

  <!-- Main Content -->
  <main class="main">
    {#if route === '#/' || route === '#'}
      <Landing {navigate} />
    {:else if route === '#/login'}
      <Login {navigate} />
    {:else if route === '#/register'}
      <Register {navigate} />
    {:else if route === '#/seller/dashboard'}
      <SellerDashboard {navigate} />
    {:else if route === '#/seller/store'}
      <SellerStore {navigate} />
    {:else if route === '#/seller/products'}
      <SellerProducts {navigate} />
    {:else if route === '#/seller/inventory'}
      <SellerInventory {navigate} />
    {:else if route === '#/seller/orders'}
      <SellerOrders {navigate} />
    {:else if route === '#/admin/dashboard'}
      <AdminDashboard {navigate} />
    {:else if route === '#/admin/stores'}
      <AdminStores {navigate} />
    {:else if route.startsWith('#/buyer/store/')}
      <BuyerStoreDetail {navigate} {params} storeId={route.split('/')[3]} />
    {:else if route === '#/buyer/cart'}
      <BuyerCart {navigate} />
    {:else if route === '#/buyer/orders'}
      <BuyerOrders {navigate} />
    {:else}
      <div class="container">
        <h2>페이지를 찾을 수 없습니다</h2>
        <button class="btn btn-primary" on:click={() => navigate('#/')}>홈으로</button>
      </div>
    {/if}
  </main>

  <!-- Footer -->
  <footer class="footer">
    <p>&copy; 2024 우리마켓 - 신선한 농산물 도매 플랫폼</p>
  </footer>
</div>

<style>
  :global(*) {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  :global(body) {
    font-family: 'Noto Sans KR', sans-serif;
    background-color: #f5f7fa;
    color: #333;
    line-height: 1.6;
  }

  :global(.container) {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  :global(.btn) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
    text-decoration: none;
  }

  :global(.btn:hover) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  }

  :global(.btn-primary) {
    background: #2d8cf0;
    color: white;
  }

  :global(.btn-primary:hover) {
    background: #2578d4;
  }

  :global(.btn-success) {
    background: #19be6b;
    color: white;
  }

  :global(.btn-success:hover) {
    background: #15a35d;
  }

  :global(.btn-danger) {
    background: #ed4014;
    color: white;
  }

  :global(.btn-warning) {
    background: #ff9900;
    color: white;
  }

  :global(.btn-outline) {
    background: transparent;
    border: 1px solid #ddd;
    color: #666;
  }

  :global(.btn-outline:hover) {
    border-color: #2d8cf0;
    color: #2d8cf0;
  }

  :global(.btn-sm) {
    padding: 6px 12px;
    font-size: 12px;
  }

  :global(.btn-lg) {
    padding: 14px 28px;
    font-size: 16px;
  }

  :global(.badge) {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: 600;
  }

  :global(.card) {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    padding: 24px;
    margin-bottom: 16px;
  }

  :global(.form-group) {
    margin-bottom: 16px;
  }

  :global(.form-group label) {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    font-size: 14px;
    color: #555;
  }

  :global(.form-control) {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 14px;
    font-family: inherit;
    transition: border-color 0.2s;
  }

  :global(.form-control:focus) {
    outline: none;
    border-color: #2d8cf0;
    box-shadow: 0 0 0 3px rgba(45,140,240,0.1);
  }

  :global(select.form-control) {
    appearance: auto;
  }

  :global(.table) {
    width: 100%;
    border-collapse: collapse;
    margin-top: 12px;
  }

  :global(.table th),
  :global(.table td) {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #eee;
  }

  :global(.table th) {
    background: #f8f9fa;
    font-weight: 600;
    font-size: 13px;
    color: #666;
  }

  :global(.alert) {
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 14px;
  }

  :global(.alert-success) {
    background: #e8f5e9;
    color: #2e7d32;
    border: 1px solid #c8e6c9;
  }

  :global(.alert-error) {
    background: #ffeef0;
    color: #d32f2f;
    border: 1px solid #ffcdd2;
  }

  :global(.alert-info) {
    background: #e3f2fd;
    color: #1565c0;
    border: 1px solid #bbdefb;
  }

  :global(.page-title) {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 24px;
    color: #2c3e50;
  }

  .app {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  .header {
    background: white;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 64px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    text-decoration: none;
    color: inherit;
  }

  .logo-icon {
    font-size: 28px;
  }

  .logo-text {
    font-size: 20px;
    font-weight: 700;
    color: #2d8cf0;
  }

  .nav {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .nav-link {
    padding: 8px 14px;
    text-decoration: none;
    color: #666;
    font-size: 14px;
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.2s;
  }

  .nav-link:hover, .nav-link.active {
    color: #2d8cf0;
    background: #e8f4ff;
  }

  .cart-link {
    position: relative;
  }

  .cart-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    background: #ed4014;
    color: white;
    font-size: 10px;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 16px;
    padding-left: 16px;
    border-left: 1px solid #eee;
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
  }

  .user-role {
    background: #e8f4ff;
    color: #2d8cf0;
  }

  .main {
    flex: 1;
    padding: 24px 0;
  }

  .footer {
    background: #2c3e50;
    color: #aaa;
    text-align: center;
    padding: 20px;
    font-size: 13px;
    margin-top: auto;
  }

  .menu-toggle {
    display: none;
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    padding: 4px 8px;
    color: #333;
  }

  .menu-backdrop {
    display: none;
  }

  @media (max-width: 768px) {
    .menu-toggle {
      display: block;
    }

    .nav {
      display: none;
      position: absolute;
      top: 64px;
      left: 0;
      right: 0;
      background: white;
      flex-direction: column;
      align-items: stretch;
      gap: 0;
      padding: 8px 0;
      box-shadow: 0 8px 24px rgba(0,0,0,0.12);
      z-index: 101;
    }

    .nav.open {
      display: flex;
    }

    .menu-backdrop.open {
      display: block;
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.3);
      z-index: 99;
    }

    .nav-link {
      padding: 14px 20px;
      border-radius: 0;
      border-bottom: 1px solid #f5f5f5;
    }

    .user-info {
      margin-left: 0;
      padding-left: 0;
      border-left: none;
      padding: 14px 20px;
      border-top: 1px solid #eee;
    }

    :global(.container) {
      padding: 0 12px;
    }

    :global(.page-title) {
      font-size: 20px;
    }
  }
</style>
