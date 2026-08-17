(() => {
  if (window.__quantJournalInjected) return;
  window.__quantJournalInjected = true;

  const SYMBOL_SELECTORS = [
    '[class*="tabsTabsItemName"]',
    '[class*="symbol-title"]',
    '[data-name="header-toolbar-symbol-search"]',
    '[class*="tv-symbol-credit"]',
  ];

  function guessTicker() {
    for (const selector of SYMBOL_SELECTORS) {
      const el = document.querySelector(selector);
      if (el && el.textContent) {
        const text = el.textContent.trim();
        const match = text.match(/[A-Z0-9.:\-/]{2,20}/);
        if (match) return match[0];
      }
    }
    return '';
  }

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

  function createPanel() {
    const root = document.createElement('div');
    root.id = 'quant-journal-root';
    root.style.cssText = 'position:fixed;z-index:2147483647;right:16px;bottom:16px;font-family:-apple-system,Segoe UI,Roboto,sans-serif;';

    const toggle = document.createElement('button');
    toggle.textContent = 'Quant Journal';
    toggle.style.cssText = 'background:#6366f1;color:#fff;border:none;border-radius:10px;padding:10px 16px;font-weight:700;font-size:13px;cursor:pointer;box-shadow:0 4px 16px rgba(0,0,0,0.35);';

    const panel = document.createElement('div');
    panel.style.cssText = 'display:none;margin-top:10px;width:280px;background:#1e1f2b;color:#e6eef6;border:1px solid rgba(255,255,255,0.08);border-radius:12px;padding:14px;box-shadow:0 8px 30px rgba(0,0,0,0.5);';

    const statusLine = document.createElement('div');
    statusLine.style.cssText = 'font-size:12px;color:#94a3b8;margin-bottom:10px;';

    function label(text) {
      const el = document.createElement('label');
      el.textContent = text;
      el.style.cssText = 'display:block;font-size:11px;color:#94a3b8;margin:8px 0 4px;text-transform:uppercase;letter-spacing:0.04em;font-weight:600;';
      return el;
    }

    function input() {
      const el = document.createElement('input');
      el.style.cssText = 'width:100%;box-sizing:border-box;background:#232430;border:1px solid rgba(255,255,255,0.1);border-radius:8px;color:#e6eef6;padding:8px 10px;font-size:13px;';
      return el;
    }

    const tickerInput = input();
    const sideSelect = document.createElement('select');
    sideSelect.style.cssText = 'width:100%;background:#232430;border:1px solid rgba(255,255,255,0.1);border-radius:8px;color:#e6eef6;padding:8px 10px;font-size:13px;';
    ['LONG', 'SHORT'].forEach((v) => {
      const opt = document.createElement('option');
      opt.value = v;
      opt.textContent = v.charAt(0) + v.slice(1).toLowerCase();
      sideSelect.appendChild(opt);
    });
    const priceInput = input();
    const qtyInput = input();
    const stopInput = input();

    const message = document.createElement('div');
    message.style.cssText = 'font-size:12px;margin-top:10px;min-height:16px;';

    const logBtn = document.createElement('button');
    logBtn.textContent = 'Log Trade';
    logBtn.style.cssText = 'width:100%;background:#6366f1;color:#fff;border:none;border-radius:8px;padding:10px;font-weight:700;font-size:13px;cursor:pointer;margin-top:12px;';

    const accountLine = document.createElement('div');
    accountLine.style.cssText = 'font-size:11px;color:#94a3b8;margin-top:10px;';

    function setStatus() {
      sendMessage({ type: 'status' }).then((res) => {
        if (res && res.ok && res.loggedIn) {
          const account = (res.accounts || []).find((a) => a.id === res.activeAccountId);
          const name = res.activeAccountId ? (account ? account.name : 'Selected account') : 'All accounts';
          statusLine.textContent = `Logged in as ${res.user?.email || 'user'} · ${name}`;
          statusLine.style.color = '#4ade80';
          logBtn.disabled = false;
        } else {
          statusLine.textContent = 'Not logged in. Open the extension popup to sign in.';
          statusLine.style.color = '#f87171';
          logBtn.disabled = true;
        }
      });
    }

    logBtn.addEventListener('click', () => {
      const ticker = tickerInput.value.trim();
      const entryPrice = priceInput.value.trim();
      const quantity = qtyInput.value.trim();
      if (!ticker || !entryPrice || !quantity) {
        message.textContent = 'Ticker, price, and quantity are required.';
        message.style.color = '#f87171';
        return;
      }
      logBtn.disabled = true;
      message.textContent = 'Logging...';
      message.style.color = '#94a3b8';
      sendMessage({
        type: 'logTrade',
        payload: {
          ticker,
          positionType: sideSelect.value,
          entryPrice: entryPrice,
          quantity: quantity,
          stopLoss: stopInput.value.trim() || null,
        },
      }).then((res) => {
        if (res && res.ok) {
          message.textContent = `Logged ${ticker} ${sideSelect.value.toLowerCase()} at ${entryPrice}.`;
          message.style.color = '#4ade80';
          tickerInput.value = guessTicker();
          priceInput.value = '';
          qtyInput.value = '';
          stopInput.value = '';
        } else {
          message.textContent = res?.error || 'Failed to log trade.';
          message.style.color = '#f87171';
        }
        setStatus();
        logBtn.disabled = false;
      });
    });

    toggle.addEventListener('click', () => {
      const visible = panel.style.display !== 'none';
      panel.style.display = visible ? 'none' : 'block';
      if (!visible) setStatus();
    });

    panel.appendChild(statusLine);
    panel.appendChild(label('Ticker'));
    panel.appendChild(tickerInput);
    panel.appendChild(label('Side'));
    panel.appendChild(sideSelect);
    panel.appendChild(label('Entry Price'));
    panel.appendChild(priceInput);
    panel.appendChild(label('Quantity'));
    panel.appendChild(qtyInput);
    panel.appendChild(label('Stop Loss (optional)'));
    panel.appendChild(stopInput);
    panel.appendChild(logBtn);
    panel.appendChild(message);
    panel.appendChild(accountLine);

    root.appendChild(toggle);
    root.appendChild(panel);
    document.body.appendChild(root);

    tickerInput.value = guessTicker();
    setStatus();
  }

  function boot() {
    if (document.body) {
      createPanel();
    } else {
      window.setTimeout(boot, 500);
    }
  }

  boot();
})();