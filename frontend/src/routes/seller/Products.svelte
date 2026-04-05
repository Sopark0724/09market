<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let products = [];
  let loading = true;
  let error = '';
  let showForm = false;
  let editingId = null;
  let uploading = false;
  let imagePreview = '';
  let detailPreviews = [];
  let detailUploading = false;

  let form = {
    name: '',
    description: '',
    unit: '',
    category: '',
    imageUrl: '',
    detailImages: ''
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
    form = { name: '', description: '', unit: '', category: '', imageUrl: '', detailImages: '' };
    imagePreview = '';
    detailPreviews = [];
    editingId = null;
    showForm = true;
  }

  function editProduct(product) {
    form = {
      name: product.name,
      description: product.description || '',
      unit: product.unit,
      category: product.category || '',
      imageUrl: product.imageUrl || '',
      detailImages: product.detailImages || ''
    };
    imagePreview = product.imageUrl || '';
    detailPreviews = product.detailImages ? product.detailImages.split(',') : [];
    editingId = product.id;
    showForm = true;
  }

  async function handleImageUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e) => imagePreview = e.target.result;
    reader.readAsDataURL(file);

    uploading = true;
    error = '';
    try {
      const res = await api.upload(file);
      form.imageUrl = res.url;
    } catch (e) {
      error = '이미지 업로드 실패: ' + e.message;
      imagePreview = '';
    } finally {
      uploading = false;
    }
  }

  function removeImage() {
    form.imageUrl = '';
    imagePreview = '';
  }

  async function handleDetailImageUpload(event) {
    const files = Array.from(event.target.files);
    if (files.length === 0) return;

    detailUploading = true;
    error = '';
    try {
      for (const file of files) {
        const res = await api.upload(file);
        detailPreviews = [...detailPreviews, res.url];
      }
      form.detailImages = detailPreviews.join(',');
    } catch (e) {
      error = '상세 이미지 업로드 실패: ' + e.message;
    } finally {
      detailUploading = false;
      event.target.value = '';
    }
  }

  function removeDetailImage(index) {
    detailPreviews = detailPreviews.filter((_, i) => i !== index);
    form.detailImages = detailPreviews.join(',');
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
      imagePreview = '';
      detailPreviews = [];
      await loadProducts();
    } catch (e) {
      error = e.message;
    }
  }
</script>

<div class="container">
  <div class="page-header">
    <h1 class="page-title">상품 관리</h1>
    <button class="btn btn-primary" on:click={resetForm}>+ 상품 등록</button>
  </div>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if showForm}
    <div class="card">
      <h3>{editingId ? '상품 수정' : '신규 상품 등록'}</h3>

      <!-- 대표 이미지 -->
      <div class="form-group">
        <label>대표 이미지 (썸네일)</label>
        <div class="image-upload-area">
          {#if imagePreview}
            <div class="image-preview">
              <img src={imagePreview} alt="미리보기" />
              <button class="remove-image-btn" on:click={removeImage}>X</button>
            </div>
          {:else}
            <label class="upload-placeholder" for="thumb-input">
              {#if uploading}
                <span class="upload-text">업로드 중...</span>
              {:else}
                <span class="upload-icon">+</span>
                <span class="upload-text">대표 이미지</span>
              {/if}
            </label>
          {/if}
          <input id="thumb-input" type="file" accept="image/*" on:change={handleImageUpload} style="display:none" />
        </div>
      </div>

      <!-- 상세 이미지 -->
      <div class="form-group">
        <label>상세보기 이미지 (여러장)</label>
        <div class="detail-images-area">
          {#each detailPreviews as url, i}
            <div class="detail-thumb">
              <img src={url} alt="상세 {i+1}" />
              <button class="remove-image-btn" on:click={() => removeDetailImage(i)}>X</button>
            </div>
          {/each}
          <label class="upload-placeholder detail-add" for="detail-input">
            {#if detailUploading}
              <span class="upload-text">업로드중...</span>
            {:else}
              <span class="upload-icon">+</span>
              <span class="upload-text">추가</span>
            {/if}
          </label>
          <input id="detail-input" type="file" accept="image/*" multiple on:change={handleDetailImageUpload} style="display:none" />
        </div>
      </div>

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
        <label>상품 설명</label>
        <textarea class="form-control" bind:value={form.description} rows="4" placeholder="상품에 대한 상세 설명을 입력하세요"></textarea>
      </div>

      <div class="form-actions">
        <button class="btn btn-primary" on:click={saveProduct}>
          {editingId ? '수정' : '등록'}
        </button>
        <button class="btn btn-outline" on:click={() => { showForm = false; imagePreview = ''; detailPreviews = []; }}>취소</button>
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
          {#if product.imageUrl}
            <div class="product-image">
              <img src={product.imageUrl} alt={product.name} />
              {#if product.detailImages}
                <span class="detail-count">{product.detailImages.split(',').length}장</span>
              {/if}
            </div>
          {:else}
            <div class="product-image product-image-empty">
              <span>No Image</span>
            </div>
          {/if}
          <div class="product-body">
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

  .page-header .page-title { margin-bottom: 0; }

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

  h3 { margin-bottom: 20px; color: #2c3e50; }

  /* Image Upload */
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 120px;
    height: 90px;
    border: 2px dashed #ddd;
    border-radius: 10px;
    cursor: pointer;
    background: #fafafa;
  }

  .upload-placeholder:hover { border-color: #2d8cf0; }

  .upload-icon { font-size: 28px; color: #ccc; line-height: 1; }
  .upload-text { font-size: 11px; color: #999; margin-top: 2px; }

  .image-preview {
    position: relative;
    width: 120px;
    height: 90px;
  }

  .image-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 10px;
  }

  .remove-image-btn {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    border: none;
    background: #ed4014;
    color: white;
    font-size: 11px;
    font-weight: bold;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* Detail Images */
  .detail-images-area {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .detail-thumb {
    position: relative;
    width: 90px;
    height: 68px;
  }

  .detail-thumb img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px;
  }

  .detail-add {
    width: 90px;
    height: 68px;
  }

  /* Product Grid */
  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }

  .product-card { padding: 0; overflow: hidden; }

  .product-image {
    width: 100%;
    height: 150px;
    overflow: hidden;
    position: relative;
  }

  .product-image img { width: 100%; height: 100%; object-fit: cover; }

  .product-image-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    color: #ccc;
    font-size: 14px;
  }

  .detail-count {
    position: absolute;
    bottom: 6px;
    right: 6px;
    background: rgba(0,0,0,0.6);
    color: white;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 10px;
  }

  .product-body { padding: 14px 18px 18px; }

  .product-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }

  .product-name { font-size: 16px; font-weight: 600; }
  .product-unit { font-size: 13px; color: #666; margin-bottom: 6px; }

  .product-desc {
    font-size: 13px;
    color: #999;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .product-actions { display: flex; gap: 8px; }
  .empty { text-align: center; padding: 48px; color: #999; }
  .empty p { margin-bottom: 16px; }

  @media (max-width: 640px) {
    .form-row { grid-template-columns: 1fr; }
    .product-grid { grid-template-columns: 1fr; }
  }
</style>
