<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let products = [];
  let loading = true;
  let error = '';
  let showForm = false;
  let editingId = null;

  let form = {
    name: '',
    description: '',
    unit: '',
    category: '',
    imageUrl: ''
  };

  const categories = ['과일', '채소', '축산', '수산', '가공식품', '기타'];

  onMount(loadProducts);

  async function loadProducts() {
    loading = true;
    try {
      products = await api.get('/seller/products');
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  function resetForm() {
    form = { name: '', description: '', unit: '', category: '', imageUrl: '' };
    editingId = null;
    showForm = true;
  }

  function editProduct(product) {
    form = {
      name: product.name,
      description: product.description || '',
      unit: product.unit,
      category: product.category || '',
      imageUrl: product.imageUrl || ''
    };
    editingId = product.id;
    showForm = true;
  }

  async function saveProduct() {
    if (!form.name || !form.unit) {
      error = '상품명과 단위는 필수입니다.';
      return;
    }
    error = '';
    try {
      if (editingId) {
        await api.put(`/seller/products/${editingId}`, form);
      } else {
        await api.post('/seller/products', form);
      }
      showForm = false;
      editingId = null;
      await loadProducts();
    } catch (e) {
      error = e.message;
    }
  }
</script>

<div class="container">
  <div class="page-header">
    <h1 class="page-title">🏷️ 상품 관리</h1>
    <button class="btn btn-primary" on:click={resetForm}>+ 상품 등록</button>
  </div>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if showForm}
    <div class="card">
      <h3>{editingId ? '상품 수정' : '신규 상품 등록'}</h3>

      <div class="form-row">
        <div class="form-group">
          <label>상품명 *</label>
          <input type="text" class="form-control" bind:value={form.name} placeholder="사과 (부사)">
        </div>
        <div class="form-group">
          <label>단위 *</label>
          <input type="text" class="form-control" bind:value={form.unit} placeholder="box (10kg)">
        </div>
      </div>

      <div class="form-group">
        <label>카테고리</label>
        <select class="form-control" bind:value={form.category}>
          <option value="">선택하세요</option>
          {#each categories as cat}
            <option value={cat}>{cat}</option>
          {/each}
        </select>
      </div>

      <div class="form-group">
        <label>설명</label>
        <textarea class="form-control" bind:value={form.description} rows="3" placeholder="상품 설명"></textarea>
      </div>

      <div class="form-actions">
        <button class="btn btn-primary" on:click={saveProduct}>
          {editingId ? '수정' : '등록'}
        </button>
        <button class="btn btn-outline" on:click={() => showForm = false}>취소</button>
      </div>
    </div>
  {/if}

  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else if products.length === 0}
    <div class="card empty">
      <p>등록된 상품이 없습니다.</p>
      <button class="btn btn-primary" on:click={resetForm}>첫 상품 등록하기</button>
    </div>
  {:else}
    <div class="product-grid">
      {#each products as product}
        <div class="card product-card">
          <div class="product-header">
            <span class="product-name">{product.name}</span>
            {#if product.category}
              <span class="badge" style="background:#e8f4ff;color:#2d8cf0">{product.category}</span>
            {/if}
          </div>
          <p class="product-unit">단위: {product.unit}</p>
          {#if product.description}
            <p class="product-desc">{product.description}</p>
          {/if}
          <div class="product-actions">
            <button class="btn btn-sm btn-outline" on:click={() => editProduct(product)}>수정</button>
          </div>
        </div>
      {/each}
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

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .form-actions {
    display: flex;
    gap: 8px;
    margin-top: 16px;
  }

  h3 {
    margin-bottom: 20px;
    color: #2c3e50;
  }

  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }

  .product-card {
    padding: 20px;
  }

  .product-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .product-name {
    font-size: 16px;
    font-weight: 600;
  }

  .product-unit {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
  }

  .product-desc {
    font-size: 13px;
    color: #999;
    margin-bottom: 12px;
  }

  .product-actions {
    display: flex;
    gap: 8px;
  }

  .empty {
    text-align: center;
    padding: 48px;
    color: #999;
  }

  .empty p {
    margin-bottom: 16px;
  }
</style>
