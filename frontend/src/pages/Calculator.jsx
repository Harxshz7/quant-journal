import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSettings, updateSettings } from '../api/auth';
import NavBar from '../components/NavBar';

function toNum(value) {
  const n = parseFloat(value);
  return Number.isFinite(n) ? n : 0;
}

function formatMoney(value) {
  return value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatQty(value) {
  return value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 4 });
}

export default function Calculator() {
  const navigate = useNavigate();

  const [accountSize, setAccountSize] = useState('');
  const [riskPercent, setRiskPercent] = useState('');
  const [entryPrice, setEntryPrice] = useState('');
  const [stopLoss, setStopLoss] = useState('');

  const [settings, setSettings] = useState({ accountSize: null, dailyLossLimitAmount: null, monthlyGoalPnl: null });
  const [saving, setSaving] = useState(false);
  const [saveMsg, setSaveMsg] = useState('');
  const [loadError, setLoadError] = useState('');

  useEffect(() => {
    let cancelled = false;
    getSettings()
      .then((data) => {
        if (cancelled) return;
        setSettings(data);
        if (data.accountSize != null && data.accountSize !== '') {
          setAccountSize(String(data.accountSize));
        }
      })
      .catch(() => { if (!cancelled) setLoadError('Failed to load settings'); });
    return () => { cancelled = true; };
  }, []);

  const saveAccountSize = async () => {
    setSaving(true);
    setSaveMsg('');
    try {
      const updated = await updateSettings({
        accountSize: accountSize === '' ? null : toNum(accountSize),
        dailyLossLimitAmount: settings.dailyLossLimitAmount ?? null,
        monthlyGoalPnl: settings.monthlyGoalPnl ?? null,
      });
      setSettings(updated);
      setSaveMsg('Account size saved.');
    } catch (err) {
      setSaveMsg(err.response?.data?.message || 'Failed to save account size');
    } finally {
      setSaving(false);
    }
  };

  const account = toNum(accountSize);
  const riskPct = toNum(riskPercent);
  const entry = toNum(entryPrice);
  const stop = toNum(stopLoss);

  const riskAmount = account * (riskPct / 100);
  const spread = Math.abs(entry - stop);
  const positionSize = spread > 0 ? riskAmount / spread : 0;
  const positionValue = positionSize * entry;

  const canUseValues = spread > 0 && positionSize > 0;

  const useValues = () => {
    if (!canUseValues) return;
    sessionStorage.setItem(
      'tradePrefill',
      JSON.stringify({
        entryPrice: entry,
        quantity: positionSize,
        stopLoss: stop,
      })
    );
    navigate('/journal');
  };

  return (
    <div className="container">
      <NavBar active="calculator" />

      <header className="header">
        <div>
          <h1>Position Size Calculator</h1>
          <p className="muted" style={{ margin: '0.25rem 0 0 0', fontSize: '0.9rem' }}>
            Risk-based position sizing — pure math, no trade is created
          </p>
        </div>
      </header>

      {loadError && <div className="error-banner">{loadError}</div>}

      <section className="section-card">
        <div className="calc-grid">
          <div className="form-group">
            <label htmlFor="accountSize">Account Size</label>
            <input
              id="accountSize"
              type="number"
              step="any"
              min="0"
              placeholder="e.g. 100000"
              value={accountSize}
              onChange={(e) => setAccountSize(e.target.value)}
            />
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={saveAccountSize}
              disabled={saving}
              style={{ alignSelf: 'flex-start', marginTop: '0.25rem' }}
            >
              {saving ? 'Saving...' : 'Save Account Size'}
            </button>
            {saveMsg && (
              <p className="muted" style={{ fontSize: '0.8rem', margin: 0 }}>
                {saveMsg}
              </p>
            )}
          </div>
          <div className="form-group">
            <label htmlFor="riskPercent">Risk % per Trade</label>
            <input
              id="riskPercent"
              type="number"
              step="any"
              min="0"
              placeholder="e.g. 1"
              value={riskPercent}
              onChange={(e) => setRiskPercent(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label htmlFor="entryPrice">Entry Price</label>
            <input
              id="entryPrice"
              type="number"
              step="any"
              min="0"
              placeholder="e.g. 185.50"
              value={entryPrice}
              onChange={(e) => setEntryPrice(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label htmlFor="stopLoss">Stop Loss</label>
            <input
              id="stopLoss"
              type="number"
              step="any"
              min="0"
              placeholder="e.g. 182.00"
              value={stopLoss}
              onChange={(e) => setStopLoss(e.target.value)}
            />
          </div>
        </div>

        <p className="calc-formula">
          Risk amount = account size × risk % · Position size = risk amount ÷ |entry − stop| · Position value = size × entry
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
          <div className="calc-result-card" style={{ marginBottom: 0 }}>
            <div className="calc-result-label">Risk Amount</div>
            <div className="calc-result-value" style={{ color: 'var(--danger)' }}>
              {formatMoney(riskAmount)}
            </div>
          </div>
          <div className="calc-result-card" style={{ marginBottom: 0 }}>
            <div className="calc-result-label">Position Size</div>
            <div className="calc-result-value">{formatQty(positionSize)}</div>
          </div>
          <div className="calc-result-card" style={{ marginBottom: 0 }}>
            <div className="calc-result-label">Position Value</div>
            <div className="calc-result-value">{formatMoney(positionValue)}</div>
          </div>
        </div>

        <div style={{ marginTop: '1.25rem' }}>
          <button
            type="button"
            className="btn btn-primary"
            onClick={useValues}
            disabled={!canUseValues}
          >
            Use these values →
          </button>
          <p className="muted" style={{ fontSize: '0.8rem', margin: '0.5rem 0 0 0' }}>
            Opens the journal so you can create a trade with the calculated quantity, entry, and stop loss prefilled.
          </p>
        </div>
      </section>
    </div>
  );
}
