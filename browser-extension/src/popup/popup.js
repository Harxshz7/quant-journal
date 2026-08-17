const DEFAULT_BASE_URL = 'http://localhost:8080';

const loginView = document.getElementById('login-view');
const mainView = document.getElementById('main-view');
const loginForm = document.getElementById('login-form');
const baseUrlInput = document.getElementById('base-url');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const loginBtn = document.getElementById('login-btn');
const loginMessage = document.getElementById('login-message');
const userLine = document.getElementById('user-line');
const accountSelect = document.getElementById('account-select');
const accountMessage = document.getElementById('account-message');
const refreshBtn = document.getElementById('refresh-btn');
const logoutBtn = document.getElementById('logout-btn');

function sendMessage(payload) {
  return new Promise((resolve) => {
    chrome.runtime.sendMessage(payload, (response) => {
      if (chrome.runtime.lastError) {
        resolve({ ok: false, error: chrome.runtime.lastError.message });
      } else {
        resolve(response);
      }
    });
  });
}

function showLogin() {
  loginView.style.display = 'block';
  mainView.style.display = 'none';
}

function showMain(user, baseUrl, accounts, activeAccountId) {
  loginView.style.display = 'none';
  mainView.style.display = 'block';
  userLine.textContent = `Signed in as ${user?.email || 'user'} · ${baseUrl}`;

  accountSelect.innerHTML = '';
  const all = document.createElement('option');
  all.value = '';
  all.textContent = 'All accounts';
  accountSelect.appendChild(all);

  (accounts || []).forEach((account) => {
    const opt = document.createElement('option');
    opt.value = account.id;
    opt.textContent = `${account.name}${account.isDefault ? ' (default)' : ''}`;
    accountSelect.appendChild(opt);
  });
  accountSelect.value = activeAccountId || '';
}

loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  loginMessage.textContent = '';
  loginMessage.className = 'message';
  loginBtn.disabled = true;
  const baseUrl = (baseUrlInput.value.trim() || DEFAULT_BASE_URL).replace(/\/$/, '');
  try {
    const res = await sendMessage({
      type: 'login',
      payload: { baseUrl, email: emailInput.value.trim(), password: passwordInput.value },
    });
    if (!res.ok) throw new Error(res.error || 'Login failed.');
    baseUrlInput.value = baseUrl;
    showMain(res.user, baseUrl, res.accounts, null);
  } catch (err) {
    loginMessage.textContent = err.message;
  } finally {
    loginBtn.disabled = false;
  }
});

accountSelect.addEventListener('change', async () => {
  accountMessage.textContent = '';
  accountMessage.className = 'message';
  const res = await sendMessage({ type: 'setAccount', payload: { accountId: accountSelect.value } });
  if (res && res.ok) {
    accountMessage.textContent = 'Account updated.';
    accountMessage.className = 'message ok';
  }
});

refreshBtn.addEventListener('click', async () => {
  accountMessage.textContent = '';
  accountMessage.className = 'message';
  const res = await sendMessage({ type: 'refreshAccounts' });
  if (res && res.ok) {
    accountSelect.value = res.activeAccountId || '';
    accountMessage.textContent = 'Accounts refreshed.';
    accountMessage.className = 'message ok';
  } else {
    accountMessage.textContent = res?.error || 'Failed to refresh.';
  }
});

logoutBtn.addEventListener('click', async () => {
  await sendMessage({ type: 'logout' });
  passwordInput.value = '';
  showLogin();
});

(async () => {
  const res = await sendMessage({ type: 'status' });
  if (res && res.ok && res.loggedIn) {
    baseUrlInput.value = res.baseUrl;
    showMain(res.user, res.baseUrl, res.accounts, res.activeAccountId);
  } else {
    baseUrlInput.value = DEFAULT_BASE_URL;
    showLogin();
  }
})();