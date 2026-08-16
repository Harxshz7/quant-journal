import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceLine
} from 'recharts';
import { getStatistics, getTrades } from '../api/journal';
import { getEquityCurve } from '../api/analytics';
import { getRulesStatus } from '../api/rules';
import NavBar from '../components/NavBar';
import RulesWidget from '../components/RulesWidget';
import '../styles/Dashboard.css';

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

function toISODate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function getPresetRange(preset) {
  const now = new Date();
  const today = toISODate(now);
  switch (preset) {
    case '7D': {
      const d = new Date(now); d.setDate(d.getDate() - 7);
      return { fromDate: toISODate(d), toDate: today };
    }
    case '30D': {
      const d = new Date(now); d.setDate(d.getDate() - 30);
      return { fromDate: toISODate(d), toDate: today };
    }
    case '90D': {
      const d = new Date(now); d.setDate(d.getDate() - 90);
      return { fromDate: toISODate(d), toDate: today };
    }
    case 'YTD': {
      return { fromDate: `${now.getFullYear()}-01-01`, toDate: today };
    }
    case 'All':
    default:
      return { fromDate: null, toDate: null };
  }
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
      <div style={{ color: COLORS.muted, marginBottom: 4 }}>{label}</div>
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

function StatCard({ label, value, color }) {
  return (
    <article className="stat-card">
      <div className="stat-label">{label}</div>
      <div className="stat-value" style={color ? { color } : undefined}>{value}</div>
    </article>
  );
}

function SectionLoader({ text }) {
  return <div className="chart-loading">{text || 'Loading...'}</div>;
}

function SectionEmpty({ text }) {
  return <div className="chart-empty">{text}</div>;
}

function formatMoney(v) {
  if (v === null || v === undefined) return '-';
  return Number(v).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatPct(v) {
  if (v === null || v === undefined) return '-';
  return `${Number(v).toFixed(2)}%`;
}

function pnlColorClass(v) {
  if (v === null || v === undefined) return '';
  return Number(v) >= 0 ? 'pnl-positive' : 'pnl-negative';
}

export default function Dashboard() {
  const navigate = useNavigate();

  // Date range state
  const [preset, setPreset] = useState('All');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [customActive, setCustomActive] = useState(false);

  // Section data
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);

  const [equity, setEquity] = useState([]);
  const [equityLoading, setEquityLoading] = useState(true);

  const [recentTrades, setRecentTrades] = useState([]);
  const [tradesLoading, setTradesLoading] = useState(true);

  const [rulesStatus, setRulesStatus] = useState(null);

  const applyPreset = useCallback((p) => {
    setPreset(p);
    setCustomActive(false);
    const range = getPresetRange(p);
    setFromDate(range.fromDate || '');
    setToDate(range.toDate || '');
  }, []);

  const applyCustom = useCallback(() => {
    setCustomActive(true);
    setPreset('');
  }, []);

  // Fetch stats
  useEffect(() => {
    let cancelled = false;
    setStatsLoading(true);
    const fd = customActive ? fromDate || null : (preset === 'All' ? null : fromDate);
    const td = customActive ? toDate || null : (preset === 'All' ? null : toDate);
    getStatistics(fd, td)
      .then(data => { if (!cancelled) setStats(data); })
      .catch(() => {})
      .finally(() => { if (!cancelled) setStatsLoading(false); });
    return () => { cancelled = true; };
  }, [fromDate, toDate, preset, customActive]);

  // Fetch equity curve
  useEffect(() => {
    let cancelled = false;
    setEquityLoading(true);
    const fd = customActive ? fromDate || null : (preset === 'All' ? null : fromDate);
    const td = customActive ? toDate || null : (preset === 'All' ? null : toDate);
    getEquityCurve(fd, td)
      .then(data => { if (!cancelled) setEquity(data); })
      .catch(() => {})
      .finally(() => { if (!cancelled) setEquityLoading(false); });
    return () => { cancelled = true; };
  }, [fromDate, toDate, preset, customActive]);

  // Fetch recent trades
  useEffect(() => {
    let cancelled = false;
    setTradesLoading(true);
    const filters = { sort: 'exitDate,desc', size: 10, status: 'CLOSED' };
    const fd = customActive ? fromDate || null : (preset === 'All' ? null : fromDate);
    const td = customActive ? toDate || null : (preset === 'All' ? null : toDate);
    if (fd) filters.fromDate = fd;
    if (td) filters.toDate = td;
    getTrades(filters)
      .then(data => {
        if (!cancelled) {
          const content = data?.content || data || [];
          setRecentTrades(Array.isArray(content) ? content : []);
        }
      })
      .catch(() => {})
      .finally(() => { if (!cancelled) setTradesLoading(false); });
    return () => { cancelled = true; };
  }, [fromDate, toDate, preset, customActive]);

  // Fetch rules status (daily loss limit + monthly goal)
  useEffect(() => {
    let cancelled = false;
    getRulesStatus()
      .then((data) => { if (!cancelled) setRulesStatus(data); })
      .catch(() => {});
    return () => { cancelled = true; };
  }, []);

  const effectiveFrom = customActive ? fromDate || null : (preset === 'All' ? null : fromDate);
  const effectiveTo = customActive ? toDate || null : (preset === 'All' ? null : toDate);

  return (
    <div className="container">
      <NavBar active="dashboard" />

      <header className="header">
        <h1>Dashboard</h1>
      </header>

      <RulesWidget status={rulesStatus} />

      {/* Date Range Control */}
      <div className="date-range-bar">
        <div className="date-preset-group">
          {['7D', '30D', '90D', 'YTD', 'All'].map(p => (
            <button
              key={p}
              className={`btn btn-sm ${preset === p && !customActive ? 'is-selected' : ''}`}
              onClick={() => applyPreset(p)}
            >
              {p}
            </button>
          ))}
        </div>
        <div className="date-custom-group">
          <label className="date-label">From</label>
          <input
            type="date"
            className="date-input"
            value={fromDate}
            onChange={e => { setFromDate(e.target.value); applyCustom(); }}
          />
          <label className="date-label">To</label>
          <input
            type="date"
            className="date-input"
            value={toDate}
            onChange={e => { setToDate(e.target.value); applyCustom(); }}
          />
        </div>
      </div>

      {/* Summary Cards */}
      <div className="dashboard-grid">
        {equityLoading ? (
          <div className="stat-card"><SectionLoader /></div>
        ) : (
          <StatCard
            label="Total P&L"
            value={formatMoney(equity.length > 0 ? equity[equity.length - 1].cumulativePnl : null)}
            color={equity.length > 0 ? (Number(equity[equity.length - 1].cumulativePnl) >= 0 ? COLORS.positive : COLORS.negative) : undefined}
          />
        )}
        {statsLoading ? (
          <>
            <div className="stat-card"><SectionLoader /></div>
            <div className="stat-card"><SectionLoader /></div>
            <div className="stat-card"><SectionLoader /></div>
          </>
        ) : (
          <>
            <StatCard label="Win Rate" value={formatPct(stats?.winRate)} />
            <StatCard
              label="Profit Factor"
              value={stats?.profitFactor != null ? Number(stats.profitFactor).toFixed(2) : '-'}
            />
            <StatCard label="Avg R:R" value={stats?.avgRiskReward != null ? Number(stats.avgRiskReward).toFixed(2) : '-'} />
          </>
        )}
      </div>

      {/* Equity Curve */}
      <div className="chart-card">
        <div className="chart-card-header">
          <h2>Equity Curve</h2>
        </div>
        {equityLoading ? (
          <SectionLoader text="Loading equity curve..." />
        ) : equity.length === 0 ? (
          <SectionEmpty text="No closed trades in this range" />
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={equity}>
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

      {/* Recent Trades */}
      <div className="chart-card">
        <div className="chart-card-header">
          <h2>Recent Trades</h2>
          <Link to="/trades" className="btn btn-sm" style={{ textDecoration: 'none' }}>View All</Link>
        </div>
        {tradesLoading ? (
          <SectionLoader text="Loading recent trades..." />
        ) : recentTrades.length === 0 ? (
          <SectionEmpty text="No closed trades in this range" />
        ) : (
          <div className="recent-trades-list">
            {recentTrades.map((t) => (
              <div
                key={t.tradeId}
                className="recent-trade-row"
                onClick={() => navigate(`/journal/${t.journalEntryId}`)}
              >
                <div className="recent-trade-ticker">{t.ticker}</div>
                <div className="recent-trade-date">
                  {t.entryDate || (t.exitDate ? new Date(t.exitDate).toLocaleDateString() : '-')}
                </div>
                <div className="recent-trade-side">
                  <span className={`badge badge-${t.positionType?.toLowerCase()}`}>
                    {t.positionType}
                  </span>
                </div>
                <div className="recent-trade-outcome">
                  <span className={`badge badge-${t.outcome?.toLowerCase()}`}>
                    {t.outcome}
                  </span>
                </div>
                <div className={`recent-trade-pnl ${pnlColorClass(t.netPnl)}`}>
                  {formatMoney(t.netPnl)}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
