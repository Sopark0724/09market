<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let stores = [];
  let loading = true;
  let error = '';

  onMount(async () => {
    try {
      stores = await api.get('/buyer/stores');
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  });
</script>

<div class="container">
  <h1 class="page-title">🏪 매장 목록</h1>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else if stores.length === 0}
    <div class="card empty">
      <p>현재 운영중인 매장이 없습니다.</p>
    </div>
  {:else}
    <div class="store-grid">
      {#each stores as store}
        <div class="card store-card" on:click={() => navigate(`#/buyer/store/${store.id}`)}
             on:keypress={() => navigate(`#/buyer/store/${store.id}`)} role="button" tabindex="0">
          <div class="store-header">
            <h3>{store.storeName}</h3>
            <span class="badge" style="background:#e8f5e9;color:#2e7d32">영업중</span>
          </div>
          <div class="store-info">
            <p>📍 {store.address} {store.addressDetail || ''}</p>
            <p>📞 {store.phone}</p>
            {#if store.businessHours}
              <p>🕐 {store.businessHours}</p>
            {/if}
          </div>
          {#if store.description}
            <p class="store-desc">{store.description}</p>
          {/if}
          <div class="store-action">
            <span class="btn btn-primary btn-sm">상품 보기 →</span>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .store-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 16px;
  }

  .store-card {
    cursor: pointer;
    transition: all 0.2s;
    padding: 24px;
  }

  .store-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  }

  .store-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .store-header h3 {
    font-size: 18px;
    color: #2c3e50;
  }

  .store-info p {
    font-size: 14px;
    color: #666;
    margin-bottom: 4px;
  }

  .store-desc {
    font-size: 13px;
    color: #999;
    margin-top: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .store-action {
    margin-top: 16px;
    text-align: right;
  }

  .empty {
    text-align: center;
    padding: 48px;
    color: #999;
  }
</style>
