const DEFAULT_BASE_URL = 'http://localhost:8080';

async function getStored() {
  return chrome.storage.local.get([
    'accessToken', 'refreshToken', 'user', 'baseUrl', 'activeAccountId', 'accounts',
  ]);
}

async function setStored(obj) {
  await chrome.storage.local.set(obj);
}

async function clearSession() {
  await chrome.storage.local.remove(['accessToken', 'refreshToken', 'user']);
}

async function tryRefresh(stored, baseUrl) {
  try {
    const res = await fetch(`${baseUrl}/api/v1/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: stored.refreshToken }),
    });
    if (!res.ok) return false;
    const data = await res.json();
    await setStored({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      user: data.user || stored.user,
    });
    return true;
  } catch (err) {
    return false;
  }
}

async function apiFetch(path, { method = 'GET', body, needsAuth = true, allowRetry = true } = {}) {
  const stored = await getStored();
  const baseUrl = stored.baseUrl || DEFAULT_BASE_URL;
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (needsAuth && stored.accessToken) headers.Authorization = `Bearer ${stored.accessToken}`;

  const res = await fetch(`${baseUrl}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && needsAuth && allowRetry && stored.refreshToken) {
    const refreshed = await tryRefresh(stored, baseUrl);
    if (refreshed) {
      return apiFetch(path, { method, body, needsAuth, allowRetry: false });
    }
    await clearSession();
  }

  if (!res.ok) {
    let msg = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      msg = data.message || msg;
    } catch (err) {
      // ignore parse errors, keep default message
    }
    throw new Error(msg);
  }

  if (res.status === 204) return null;
  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('application/json')) return res.json();
  return res.text();
}

async function fetchAccounts(accessToken, baseUrl) {
  const res = await fetch(`${baseUrl}/api/v1/accounts`, {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  });
  if (!res.ok) return [];
  return res.json();
}

async function login({ baseUrl, email, password }) {
  const base = (baseUrl || DEFAULT_BASE_URL).replace(/\/$/, '');
  const res = await fetch(`${base}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    throw new Error('Login failed. Check your credentials and API URL.');
  }
  const data = await res.json();
  await setStored({
    baseUrl: base,
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    user: data.user,
    activeAccountId: null,
  });
  const accounts = await fetchAccounts(data.accessToken, base);
  await setStored({ accounts });
  return { user: data.user, accounts };
}

async function logTrade(payload) {
  const stored = await getStored();
  const today = new Date().toISOString().slice(0, 10);
  const query = stored.activeAccountId ? `?accountId=${stored.activeAccountId}` : '';

  const entries = await apiFetch(`/journal${query}`);
  const list = Array.isArray(entries) ? entries : [];
  let entry = list.find((e) => e.entryDate === today);

  if (!entry) {
    entry = await apiFetch(`/journal${query}`, { method: 'POST', body: { entryDate: today } });
  }

  const body = {
    journalEntryId: entry.journalEntryId,
    ticker: payload.ticker,
    positionType: payload.positionType,
    entryPrice: payload.entryPrice,
    quantity: payload.quantity,
    stopLoss: payload.stopLoss || null,
    checklistItemIds: [],
  };

  const trade = await apiFetch('/trades', { method: 'POST', body });
  return { trade };
}

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  (async () => {
    try {
      switch (message?.type) {
        case 'login':
          return { ok: true, ...(await login(message.payload)) };
        case 'logout': {
          await clearSession();
          return { ok: true };
        }
        case 'status': {
          const stored = await getStored();
          return {
            ok: true,
            loggedIn: !!stored.accessToken,
            user: stored.user || null,
            baseUrl: stored.baseUrl || DEFAULT_BASE_URL,
            activeAccountId: stored.activeAccountId || null,
            accounts: stored.accounts || [],
          };
        }
        case 'setAccount': {
          await setStored({ activeAccountId: message.payload.accountId || null });
          return { ok: true };
        }
        case 'refreshAccounts': {
          const stored = await getStored();
          const accounts = await fetchAccounts(stored.accessToken, stored.baseUrl || DEFAULT_BASE_URL);
          await setStored({ accounts });
          return { ok: true, accounts };
        }
        case 'logTrade': {
          const result = await logTrade(message.payload);
          return { ok: true, ...result };
        }
        default:
          throw new Error('Unknown message type');
      }
    } catch (err) {
      return { ok: false, error: err.message };
    }
  })().then(sendResponse).catch((err) => sendResponse({ ok: false, error: err.message }));
  return true;
});