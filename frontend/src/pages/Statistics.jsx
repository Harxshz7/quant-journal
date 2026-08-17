import React, { useEffect, useState } from 'react';
import { getStatistics } from '../api/journal';
import NavBar from '../components/NavBar';

const CARDS = [
  { key: 'winRate', label: 'Win Rate', format: 'percent' },
  { key: 'profitFactor', label: 'Profit Factor', format: 'number' },
  { key: 'avgWin', label: 'Avg Win', format: 'money' },
  { key: 'avgLoss', label: 'Avg Loss', format: 'money' },
  { key: 'largestWin', label: 'Largest Win', format: 'money' },
  { key: 'largestLoss', label: 'Largest Loss', format: 'money' },
  { key: 'maxConsecutiveWins', label: 'Max Win Streak', format: 'count' },
  { key: 'maxConsecutiveLosses', label: 'Max Loss Streak', format: 'count' },
  { key: 'avgRiskReward', label: 'Avg R:R', format: 'number' },
];

export default function Statistics() {
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchStatistics = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await getStatistics();
        setStatistics(data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load statistics');
      } finally {
        setLoading(false);
      }
    };

    fetchStatistics();
  }, []);

  const formatValue = (value, format) => {
    if (value === null || value === undefined) {
      return '-';
    }

    if (format === 'count') {
      return Number(value).toString();
    }

    const numeric = Number(value);
    if (Number.isNaN(numeric)) {
      return '-';
    }

    if (format === 'percent') {
      return `${numeric.toFixed(2)}%`;
    }

    if (format === 'money') {
      return numeric.toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    }

    return numeric.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 4,
    });
  };

  return (
    <div className="container">
      <NavBar active="stats" />

      <header className="header">
        <div>
          <h1>Statistics</h1>
          <p className="muted section-subtitle">
            Cached per-user aggregate performance
          </p>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <section className="section-card">
        {loading ? (
          <p className="loading">Loading statistics...</p>
        ) : statistics && Object.values(statistics).every(v => v === null || v === 0) ? (
          <div className="empty-state">
            <p>No trade statistics yet. Close some trades to see your performance metrics.</p>
          </div>
        ) : (
          <div className="stats-grid">
            {CARDS.map((card) => (
              <article key={card.key} className="stat-card">
                <div className="stat-label">{card.label}</div>
                <div className="stat-value">{formatValue(statistics?.[card.key], card.format)}</div>
              </article>
            ))}
            <article className="stat-card stat-card-wide">
              <div className="stat-label">Total Trades</div>
              <div className="stat-value">{formatValue(statistics?.totalTrades, 'count')}</div>
            </article>
            <article className="stat-card stat-card-wide">
              <div className="stat-label">Break Even</div>
              <div className="stat-value">{formatValue(statistics?.breakEvenCount, 'count')}</div>
            </article>
          </div>
        )}
      </section>
    </div>
  );
}
