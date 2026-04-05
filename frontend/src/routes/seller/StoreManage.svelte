<script>
  import { api } from '../../api/client.js';
  import { onMount } from 'svelte';

  export let navigate;

  let form = {
    storeName: '',
    address: '',
    addressDetail: '',
    phone: '',
    businessHours: '',
    description: ''
  };

  let loading = false;
  let saving = false;
  let error = '';
  let success = '';
  let isEdit = false;
  let storeId = null;
  let linkCopied = false;

  $: buyerLink = storeId ? `${window.location.origin}/#/buyer/store/${storeId}` : '';

  function copyLink() {
    if (!buyerLink) return;
    navigator.clipboard.writeText(buyerLink).then(() => {
      linkCopied = true;
      setTimeout(() => linkCopied = false, 2000);
    });
  }

  onMount(async () => {
    loading = true;
    try {
      const store = await api.get('/seller/store');
      storeId = store.id;
      form = {
        storeName: store.storeName || '',
        address: store.address || '',
        addressDetail: store.addressDetail || '',
        phone: store.phone || '',
        businessHours: store.businessHours || '',
        description: store.description || ''
      };
      isEdit = true;
    } catch (e) {
      // 매장이 없는 경우 - 신규 등록
      isEdit = false;
    } finally {
      loading = false;
    }
  });

  async function save() {
    if (!form.storeName || !form.address || !form.phone) {
      error = '매장명, 주소, 전화번호는 필수 항목입니다.';
      return;
    }
    saving = true;
    error = '';
    success = '';
    try {
      await api.post('/seller/store', form);
      success = isEdit ? '매장 정보가 수정되었습니다.' : '매장이 등록되었습니다.';
      isEdit = true;
    } catch (e) {
      error = e.message;
    } finally {
      saving = false;
    }
  }
</script>

<div class="container">
  <h1 class="page-title">🏪 매장 관리</h1>

  {#if loading}
    <div class="card"><p style="text-align:center">로딩중...</p></div>
  {:else}
    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}
    {#if success}
      <div class="alert alert-success">{success}</div>
    {/if}

    {#if isEdit && buyerLink}
      <div class="card link-card">
        <h3>구매자 주문 링크</h3>
        <p class="link-desc">아래 링크를 채팅방에 공유하면 구매자가 바로 주문할 수 있습니다.</p>
        <div class="link-row">
          <input type="text" class="form-control link-input" value={buyerLink} readonly>
          <button class="btn btn-primary btn-sm" on:click={copyLink}>
            {linkCopied ? '복사됨!' : '복사'}
          </button>
        </div>
      </div>
    {/if}

    <div class="card">
      <h3>{isEdit ? '매장 정보 수정' : '매장 등록'}</h3>

      <div class="form-group">
        <label>매장명 *</label>
        <input type="text" class="form-control" bind:value={form.storeName} placeholder="매장명">
      </div>

      <div class="form-group">
        <label>주소 *</label>
        <input type="text" class="form-control" bind:value={form.address} placeholder="주소">
      </div>

      <div class="form-group">
        <label>상세주소</label>
        <input type="text" class="form-control" bind:value={form.addressDetail} placeholder="상세주소">
      </div>

      <div class="form-group">
        <label>전화번호 *</label>
        <input type="tel" class="form-control" bind:value={form.phone} placeholder="02-1234-5678">
      </div>

      <div class="form-group">
        <label>영업시간</label>
        <input type="text" class="form-control" bind:value={form.businessHours} placeholder="새벽 4시 ~ 오후 2시">
      </div>

      <div class="form-group">
        <label>매장 소개</label>
        <textarea class="form-control" bind:value={form.description} rows="4"
                  placeholder="매장 소개를 입력하세요"></textarea>
      </div>

      <button class="btn btn-primary btn-lg" on:click={save} disabled={saving}>
        {saving ? '저장중...' : (isEdit ? '수정하기' : '등록하기')}
      </button>
    </div>
  {/if}
</div>

<style>
  h3 {
    margin-bottom: 20px;
    color: #2c3e50;
  }

  .link-card {
    border: 2px solid #2d8cf0;
    background: #f0f7ff;
  }

  .link-desc {
    font-size: 13px;
    color: #666;
    margin-bottom: 12px;
  }

  .link-row {
    display: flex;
    gap: 8px;
  }

  .link-input {
    flex: 1;
    background: white;
    font-size: 13px;
    color: #2d8cf0;
  }

  @media (max-width: 640px) {
    .link-row {
      flex-direction: column;
    }
  }
</style>
