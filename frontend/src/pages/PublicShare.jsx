import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceLine,
} from 'recharts';
import { getPublicShare } from '../api/share';

const COLORS = {
  accent: '#6366f1',
  positive: '#4ade80',
  negative: '#f87171',
  muted: '#94a3b8',
  grid: 'rgba(255,255,255,0.06)',
  text: '#e6eef6',
  surface: '#232430',
};

const AXIS_STYLE = { fontSize: 11, fill: COLORS.muted };
const GRID_STYLE = { strokeDasharray: '3 3', stroke: COLORS.grid };

function formatMoney(v) {
  if (v === null || v === undefined) return '-';
  return Number(v).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatPct(v) {
  if (v === null || v === undefined) return '-';
  return `${Number(v).toFixed(2)}%`;
}

function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div style={{
      background: COLORS.surface,
      border: `1px solid ${COLORS.grid}`,
      borderRadius: 8,
      padding: '0.6rem 0.85rem',
      fontSize: '0.82rem',
      boxShadow: '0 4px 20px rgba(0,0,0,0.4)',
    }}>
      <div style={{ color: COLORS.muted, marginBottom: 4 }}>{new Date(label).toLocaleDateString()}</div>
      {payload.map((p, i) => (
        <div key={i} style={{ color: p.color || COLORS.text }}>
          {p.name}: {typeof p.value === 'number'
            ? p.value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            : p.value}
        </div>
      ))}
    </div>
  );
}

export default function PublicShare() {
  const { shareToken } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    getPublicShare(shareToken)
      .then((result) => { if (!cancelled) setData(result); })
      .catch((err) => {
        if (!cancelled) setError(err.response?.status === 404 ? 'This share link is invalid or has been disabled.' : 'Failed to load shared performance.');
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [shareToken]);

  return (
    <div className="container">
      <header className="header">
        <h1>Quant Journal</h1>
        <p className="muted" style={{ margin: '0.25rem 0 0 0', fontSize: '0.9rem' }}>
          Public performance snapshot
        </p>
      </header>

      {loading && <div className="chart-loading">Loading shared performance...</div>}

      {!loading && error && (
        <div className="auth-card">
          <div className="auth-header">
            <h2>Link unavailable</h2>
            <p>{error}</p>
          </div>
          <Link to="/" className="btn btn-secondary">Back to Quant Journal</Link>
        </div>
      )}

      {!loading && !error && data && (
        <>
          <div className="dashboard-grid">
            <article className="stat-card">
              <div className="stat-label">Win Rate</div>
              <div className="stat-value">{formatPct(data.winRate)}</div>
            </article>
            <article className="stat-card">
              <div className="stat-label">Profit Factor</div>
              <div className="stat-value">{data.profitFactor != null ? Number(data.profitFactor).toFixed(2) : '-'}</div>
            </article>
          </div>

          <div className="chart-card">
            <div className="chart-card-header">
              <h2>Equity Curve</h2>
            </div>
            {(!data.equityCurve || data.equityCurve.length === 0) ? (
              <div className="chart-empty">No closed trades yet</div>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data.equityCurve}>
                  <CartesianGrid {...GRID_STYLE} />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(v) => new Date(v).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                    tick={AXIS_STYLE}
                    tickLine={false}
                  />
                  <YAxis tick={AXIS_STYLE} tickLine={false} />
                  <Tooltip content={<CustomTooltip />} />
                  <ReferenceLine y={0} stroke={COLORS.muted} strokeDasharray="4 4" strokeWidth={1} />
                  <Line
                    type="monotone"
                    dataKey="cumulativePnl"
                    name="Cumulative P&L"
                    stroke={COLORS.accent}
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="chart-card">
            <div className="chart-card-header">
              <h2>Monthly Breakdown</h2>
            </div>
            {(!data.monthly || data.monthly.length === 0) ? (
              <div className="chart-empty">No monthly data yet</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Period</th>
                    <th>Trades</th>
                    <th>Net P&amp;L</th>
                    <th>Win Rate</th>
                  </tr>
                </thead>
                <tbody>
                  {data.monthly.map((row) => (
                    <tr key={row.period}>
                      <td>{row.period}</td>
                      <td>{row.tradeCount}</td>
                      <td className={Number(row.netPnl) >= 0 ? 'pnl-positive' : 'pnl-negative'}>
                        {formatMoney(row.netPnl)}
                      </td>
                      <td>{formatPct(row.winRate)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
}