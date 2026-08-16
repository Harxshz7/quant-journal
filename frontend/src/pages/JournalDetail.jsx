import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getJournalEntry, updateJournalEntry, createTrade, closeTrade } from '../api/journal';
import { getRulesStatus } from '../api/rules';
import TradeCardWithScreenshots from '../components/TradeCardWithScreenshots';
import NavBar from '../components/NavBar';
import RulesWidget from '../components/RulesWidget';

const MOOD_OPTIONS = ['GREAT', 'GOOD', 'NEUTRAL', 'POOR', 'TERRIBLE'];

export default function JournalDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [entry, setEntry] = useState(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [savingNotes, setSavingNotes] = useState(false);
  const [notesSavedAlert, setNotesSavedAlert] = useState(false);
  const [error, setError] = useState('');

  const [mood, setMood] = useState('');
  const [energy, setEnergy] = useState('');
  const [marketBias, setMarketBias] = useState('');
  const [dailyGoal, setDailyGoal] = useState('');
  const [dayRating, setDayRating] = useState('');
  const [savingMeta, setSavingMeta] = useState(false);

  const [ticker, setTicker] = useState('');
  const [positionType, setPositionType] = useState('LONG');
  const [entryPrice, setEntryPrice] = useState('');
  const [quantity, setQuantity] = useState('');
  const [stopLoss, setStopLoss] = useState('');
  const [addingTrade, setAddingTrade] = useState(false);

  const [rulesStatus, setRulesStatus] = useState(null);

  const [exitPrices, setExitPrices] = useState({});
  const [closingTradeId, setClosingTradeId] = useState(null);

  const fetchDetail = async () => {
    try {
      setLoading(true);
      const data = await getJournalEntry(id);
      setEntry(data);
      setNotes(data.notes || '');
      setMood(data.mood || '');
      setEnergy(data.energy || '');
      setMarketBias(data.marketBias || '');
      setDailyGoal(data.dailyGoal || '');
      setDayRating(data.dayRating || '');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load journal entry');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) fetchDetail();
  }, [id]);

  // Prefill the Add Trade form with values from the position size calculator
  useEffect(() => {
    try {
      const raw = sessionStorage.getItem('tradePrefill');
      if (!raw) return;
      const prefill = JSON.parse(raw);
      sessionStorage.removeItem('tradePrefill');
      if (prefill.entryPrice != null) setEntryPrice(String(prefill.entryPrice));
      if (prefill.quantity != null) setQuantity(String(prefill.quantity));
      if (prefill.stopLoss != null) setStopLoss(String(prefill.stopLoss));
    } catch (err) {
      sessionStorage.removeItem('tradePrefill');
    }
  }, []);

  // Rules warnings for the trade-creation form
  useEffect(() => {
    let cancelled = false;
    getRulesStatus()
      .then((data) => { if (!cancelled) setRulesStatus(data); })
      .catch(() => {});
    return () => { cancelled = true; };
  }, []);

  const handleSaveNotes = async (e) => {
    e.preventDefault();
    try {
      setSavingNotes(true);
      const updated = await updateJournalEntry(id, { notes });
      setEntry(updated);
      setNotesSavedAlert(true);
      setTimeout(() => setNotesSavedAlert(false), 3000);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save notes');
    } finally {
      setSavingNotes(false);
    }
  };

  const handleSaveMeta = async () => {
    try {
      setSavingMeta(true);
      const payload = {};
      if (mood) payload.mood = mood;
      else payload.mood = null;
      if (energy) payload.energy = parseInt(energy);
      else payload.energy = null;
      if (marketBias) payload.marketBias = marketBias;
      else payload.marketBias = null;
      if (dailyGoal) payload.dailyGoal = dailyGoal;
      else payload.dailyGoal = null;
      if (dayRating) payload.dayRating = parseInt(dayRating);
      else payload.dayRating = null;
      const updated = await updateJournalEntry(id, payload);
      setEntry(updated);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save');
    } finally {
      setSavingMeta(false);
    }
  };

  const handleAddTrade = async (e) => {
    e.preventDefault();
    if (!ticker || !entryPrice || !quantity) return;
    try {
      setAddingTrade(true);
      await createTrade({
        journalEntryId: id,
        ticker,
        positionType,
        entryPrice: parseFloat(entryPrice),
        quantity: parseFloat(quantity),
        stopLoss: stopLoss === '' ? null : parseFloat(stopLoss),
      });
      setTicker('');
      setPositionType('LONG');
      setEntryPrice('');
      setQuantity('');
      setStopLoss('');
      await fetchDetail();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add trade');
    } finally {
      setAddingTrade(false);
    }
  };

  const handleCloseTrade = async (e, tradeId) => {
    e.preventDefault();
    const priceStr = exitPrices[tradeId];
    if (!priceStr || isNaN(parseFloat(priceStr))) return;
    try {
      setClosingTradeId(tradeId);
      await closeTrade(tradeId, { exitPrice: parseFloat(priceStr) });
      await fetchDetail();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to close trade');
    } finally {
      setClosingTradeId(null);
    }
  };

  if (loading) {
    return <div className="container"><p className="loading">Loading journal detail...</p></div>;
  }

  if (error || !entry) {
    return (
      <div className="container">
        <div className="error-banner">{error || 'Journal entry not found'}</div>
        <Link to="/journal" className="btn btn-secondary back-btn">&larr; Back to Entries</Link>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="back-link">
        <Link to="/journal">&larr; Back to Entries</Link>
      </div>

      <NavBar active="journal" />

      <header className="header">
        <h1>Journal Entry: {entry.entryDate}</h1>
      </header>

      <RulesWidget status={rulesStatus} />

      {/* Mood & Energy Section */}
      <section className="section-card">
        <h2>Daily State</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
          <div className="form-group">
            <label>Mood</label>
            <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
              {MOOD_OPTIONS.map(m => (
                <button
                  key={m}
                  type="button"
                  className={`btn btn-sm ${mood === m ? 'is-selected' : ''}`}
                  onClick={() => setMood(mood === m ? '' : m)}
                  style={{ fontSize: '0.78rem' }}
                >
                  {m}
                </button>
              ))}
            </div>
          </div>
          <div className="form-group">
            <label>Energy (1-5)</label>
            <div style={{ display: 'flex', gap: '0.3rem' }}>
              {[1,2,3,4,5].map(n => (
                <button
                  key={n}
                  type="button"
                  className={`btn btn-sm ${energy == n ? 'is-selected' : ''}`}
                  onClick={() => setEnergy(energy == n ? '' : n)}
                  style={{ width: 36, height: 36, fontSize: '0.85rem' }}
                >
                  {n}
                </button>
              ))}
            </div>
          </div>
          <div className="form-group">
            <label>Day Rating (1-5)</label>
            <div style={{ display: 'flex', gap: '0.3rem' }}>
              {[1,2,3,4,5].map(n => (
                <button
                  key={n}
                  type="button"
                  className={`btn btn-sm ${dayRating == n ? 'is-selected' : ''}`}
                  onClick={() => setDayRating(dayRating == n ? '' : n)}
                  style={{ width: 36, height: 36, fontSize: '0.85rem' }}
                >
                  {n}
                </button>
              ))}
            </div>
          </div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
          <div className="form-group">
            <label>Market Bias</label>
            <input type="text" placeholder="e.g. Bullish NIFTY" value={marketBias} onChange={(e) => setMarketBias(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Daily Goal</label>
            <input type="text" placeholder="e.g. Stick to plan" value={dailyGoal} onChange={(e) => setDailyGoal(e.target.value)} />
          </div>
        </div>
        <button className="btn btn-primary btn-sm" onClick={handleSaveMeta} disabled={savingMeta}>
          {savingMeta ? 'Saving...' : 'Save State'}
        </button>
      </section>

      {/* Notes Section */}
      <section className="section-card">
        <h2>Notes</h2>
        <form onSubmit={handleSaveNotes}>
          <div className="form-group">
            <textarea rows="6" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Enter notes, observations, or trade analysis..." />
          </div>
          <div className="action-row">
            <button type="submit" className="btn btn-primary" disabled={savingNotes}>
              {savingNotes ? 'Saving...' : 'Save Notes'}
            </button>
            {notesSavedAlert && <span className="success-badge">Notes saved!</span>}
          </div>
        </form>
      </section>

      {/* Trades List */}
      <section className="section-card">
        <h2>Trades ({entry.trades ? entry.trades.length : 0})</h2>
        {entry.trades && entry.trades.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {entry.trades.map((t) => (
              <TradeCardWithScreenshots key={t.tradeId || t.id} trade={t} onTradeUpdate={fetchDetail} />
            ))}
          </div>
        ) : (
          <p className="muted">No trades recorded for this entry yet.</p>
        )}
      </section>

      {/* Add Trade Form */}
      <section className="section-card">
        <h2>Add Trade</h2>
        <form onSubmit={handleAddTrade} className="trade-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="ticker">Ticker</label>
              <input type="text" id="ticker" placeholder="e.g. AAPL" value={ticker} onChange={(e) => setTicker(e.target.value.toUpperCase())} required />
            </div>
            <div className="form-group">
              <label htmlFor="positionType">Position Type</label>
              <select id="positionType" value={positionType} onChange={(e) => setPositionType(e.target.value)}>
                <option value="LONG">LONG</option>
                <option value="SHORT">SHORT</option>
              </select>
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="entryPrice">Entry Price</label>
              <input type="number" step="any" id="entryPrice" placeholder="0.00" value={entryPrice} onChange={(e) => setEntryPrice(e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="quantity">Quantity</label>
              <input type="number" step="any" id="quantity" placeholder="0" value={quantity} onChange={(e) => setQuantity(e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="stopLoss">Stop Loss</label>
              <input type="number" step="any" id="stopLoss" placeholder="0.00" value={stopLoss} onChange={(e) => setStopLoss(e.target.value)} />
            </div>
          </div>
          <button type="submit" className="btn btn-primary" disabled={addingTrade}>
            {addingTrade ? 'Adding...' : 'Add Trade'}
          </button>
        </form>
      </section>
    </div>
  );
}
