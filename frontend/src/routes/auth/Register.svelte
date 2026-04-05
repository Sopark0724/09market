<script>
  import { api } from '../../api/client.js';
  import { user } from '../../stores/auth.js';

  export let navigate;

  const urlParams = new URLSearchParams(window.location.hash.split('?')[1] || '');

  let form = {
    name: urlParams.get('name') || '',
    email: urlParams.get('email') || '',
    phone: urlParams.get('phone') || '',
    role: 'BUYER',
    authProvider: urlParams.get('provider') || 'PHONE',
    kakaoId: urlParams.get('kakaoId') || ''
  };

  let loading = false;
  let error = '';

  async function register() {
    if (!form.name) { error = '이름을 입력해주세요.'; return; }
    loading = true;
    error = '';
    try {
      const res = await api.post('/auth/register', form);
      user.login(res.user, res.token);
      navigate(res.user.role === 'SELLER' ? '#/seller/store' : '#/buyer/orders');
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }
</script>

<div class="container">
  <div class="register-wrapper">
    <div class="card register-card">
      <h2 class="page-title">회원가입</h2>

      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      <div class="form-group">
        <label>이름 *</label>
        <input type="text" class="form-control" bind:value={form.name} placeholder="이름을 입력하세요">
      </div>

      <div class="form-group">
        <label>이메일</label>
        <input type="email" class="form-control" bind:value={form.email} placeholder="이메일">
      </div>

      {#if form.authProvider === 'PHONE'}
        <div class="form-group">
          <label>전화번호</label>
          <input type="tel" class="form-control" value={form.phone} disabled>
        </div>
      {/if}

      <div class="form-group">
        <label>가입 유형 *</label>
        <div class="role-selector">
          <button class="role-btn" class:active={form.role === 'BUYER'}
                  on:click={() => form.role = 'BUYER'}>
            <span class="role-icon">🛒</span>
            <span class="role-label">구매자</span>
            <span class="role-desc">상품을 주문하고 구매합니다</span>
          </button>
          <button class="role-btn" class:active={form.role === 'SELLER'}
                  on:click={() => form.role = 'SELLER'}>
            <span class="role-icon">📦</span>
            <span class="role-label">판매자</span>
            <span class="role-desc">상품을 등록하고 판매합니다</span>
          </button>
        </div>
      </div>

      <button class="btn btn-primary btn-lg" style="width:100%" on:click={register} disabled={loading}>
        {loading ? '가입 중...' : '가입하기'}
      </button>
    </div>
  </div>
</div>

<style>
  .register-wrapper {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  .register-card {
    width: 100%;
    max-width: 480px;
    padding: 40px;
  }

  .role-selector {
    display: flex;
    gap: 12px;
  }

  .role-btn {
    flex: 1;
    padding: 20px;
    border: 2px solid #eee;
    border-radius: 12px;
    background: white;
    cursor: pointer;
    text-align: center;
    transition: all 0.2s;
    font-family: inherit;
  }

  .role-btn.active {
    border-color: #2d8cf0;
    background: #e8f4ff;
  }

  .role-icon {
    font-size: 32px;
    display: block;
    margin-bottom: 8px;
  }

  .role-label {
    font-size: 16px;
    font-weight: 600;
    display: block;
    margin-bottom: 4px;
  }

  .role-desc {
    font-size: 12px;
    color: #999;
  }

  @media (max-width: 640px) {
    .register-card {
      padding: 24px 16px;
    }

    .role-selector {
      flex-direction: column;
    }
  }
</style>
