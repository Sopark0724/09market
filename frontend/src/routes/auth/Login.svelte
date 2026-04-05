<script>
  import { api } from '../../api/client.js';
  import { user } from '../../stores/auth.js';

  export let navigate;

  let authMethod = 'phone'; // 'phone' or 'kakao'
  let phone = '';
  let code = '';
  let codeSent = false;
  let loading = false;
  let error = '';
  let mockCode = '';

  async function sendCode() {
    if (!phone) { error = '전화번호를 입력해주세요.'; return; }
    loading = true;
    error = '';
    try {
      const res = await api.post('/auth/phone/send', { phone });
      codeSent = true;
      // Mock에서 인증번호 표시
      mockCode = res.message;
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  async function verifyCode() {
    if (!code) { error = '인증번호를 입력해주세요.'; return; }
    loading = true;
    error = '';
    try {
      const res = await api.post('/auth/phone/verify', { phone, code });
      if (res.registered) {
        user.login(res.user, res.token);
        navigate(res.user.role === 'SELLER' ? '#/seller/dashboard' : '#/buyer/stores');
      } else {
        navigate('#/register?phone=' + phone + '&provider=PHONE');
      }
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  async function kakaoLogin() {
    loading = true;
    error = '';
    try {
      // Mock 카카오 로그인
      const mockToken = 'mock_kakao_token_' + Date.now();
      const res = await api.post('/auth/kakao', { accessToken: mockToken });
      if (res.registered) {
        user.login(res.user, res.token);
        navigate(res.user.role === 'SELLER' ? '#/seller/dashboard' : '#/buyer/stores');
      } else {
        navigate('#/register?kakaoId=' + res.kakaoId + '&email=' + res.email + '&name=' + res.name + '&provider=KAKAO');
      }
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  // 테스트 빠른 로그인
  async function quickLogin(role) {
    loading = true;
    error = '';
    try {
      const testPhone = role === 'SELLER' ? '01012345678' : '01011112222';
      await api.post('/auth/phone/send', { phone: testPhone });
      const res = await api.post('/auth/phone/verify', { phone: testPhone, code: '123456' });
      if (res.registered) {
        user.login(res.user, res.token);
        navigate(res.user.role === 'SELLER' ? '#/seller/dashboard' : '#/buyer/stores');
      }
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }
</script>

<div class="container">
  <div class="login-wrapper">
    <div class="login-card card">
      <h2 class="login-title">🥬 우리마켓 로그인</h2>

      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      <!-- 테스트 빠른 로그인 -->
      <div class="quick-login">
        <p class="quick-label">테스트 빠른 로그인</p>
        <div class="quick-buttons">
          <button class="btn btn-outline" on:click={() => quickLogin('SELLER')} disabled={loading}>
            판매자 (김판매)
          </button>
          <button class="btn btn-outline" on:click={() => quickLogin('BUYER')} disabled={loading}>
            구매자 (박구매)
          </button>
        </div>
      </div>

      <div class="divider"><span>또는</span></div>

      <!-- 인증 방식 선택 -->
      <div class="auth-tabs">
        <button class="auth-tab" class:active={authMethod === 'phone'}
                on:click={() => { authMethod = 'phone'; error = ''; }}>
          📱 핸드폰 인증
        </button>
        <button class="auth-tab" class:active={authMethod === 'kakao'}
                on:click={() => { authMethod = 'kakao'; error = ''; }}>
          💬 카카오 로그인
        </button>
      </div>

      {#if authMethod === 'phone'}
        <div class="form-group">
          <label>핸드폰 번호</label>
          <div class="input-group">
            <input type="tel" class="form-control" bind:value={phone}
                   placeholder="01012345678" disabled={codeSent}>
            {#if !codeSent}
              <button class="btn btn-primary" on:click={sendCode} disabled={loading}>
                {loading ? '발송중...' : '인증번호 발송'}
              </button>
            {/if}
          </div>
        </div>

        {#if codeSent}
          {#if mockCode}
            <div class="alert alert-info">{mockCode}</div>
          {/if}
          <div class="form-group">
            <label>인증번호</label>
            <div class="input-group">
              <input type="text" class="form-control" bind:value={code}
                     placeholder="인증번호 6자리">
              <button class="btn btn-success" on:click={verifyCode} disabled={loading}>
                {loading ? '확인중...' : '확인'}
              </button>
            </div>
          </div>
        {/if}
      {:else}
        <button class="btn kakao-btn" on:click={kakaoLogin} disabled={loading}>
          💬 카카오 계정으로 로그인 (Mock)
        </button>
      {/if}
    </div>
  </div>
</div>

<style>
  .login-wrapper {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  .login-card {
    width: 100%;
    max-width: 440px;
    padding: 40px;
  }

  .login-title {
    text-align: center;
    font-size: 24px;
    margin-bottom: 32px;
    color: #2c3e50;
  }

  .quick-login {
    text-align: center;
    margin-bottom: 20px;
  }

  .quick-label {
    font-size: 12px;
    color: #999;
    margin-bottom: 8px;
  }

  .quick-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
  }

  .divider {
    text-align: center;
    margin: 20px 0;
    position: relative;
  }

  .divider::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #eee;
  }

  .divider span {
    position: relative;
    background: white;
    padding: 0 12px;
    color: #999;
    font-size: 13px;
  }

  .auth-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
  }

  .auth-tab {
    flex: 1;
    padding: 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background: white;
    cursor: pointer;
    font-size: 14px;
    font-family: inherit;
    transition: all 0.2s;
  }

  .auth-tab.active {
    border-color: #2d8cf0;
    color: #2d8cf0;
    background: #e8f4ff;
  }

  .input-group {
    display: flex;
    gap: 8px;
  }

  .input-group .form-control {
    flex: 1;
  }

  .kakao-btn {
    width: 100%;
    padding: 14px;
    background: #FEE500;
    color: #3C1E1E;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    font-family: inherit;
  }

  .kakao-btn:hover {
    background: #FDD835;
  }
</style>
