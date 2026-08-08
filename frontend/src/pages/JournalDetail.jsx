import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getJournalEntry, updateJournalEntry, createTrade } from '../api/journal';

export default function JournalDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [entry, setEntry] = useState(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [savingNotes, setSavingNotes] = useState(false);
  const [notesSavedAlert, setNotesSavedAlert] = useState(false);
  const [error, setError] = useState('');

  // Form state for adding trades
  const [ticker, setTicker] = useState('');
  const [positionType, setPositionType] = useState('LONG');
  const [entryPrice, setEntryPrice] = useState('');
  const [quantity, setQuantity] = useState('');
  const [addingTrade, setAddingTrade] = useState(false);

  const fetchDetail = async () => {
    try {
      setLoading(true);
      const data = await getJournalEntry(id);
      setEntry(data);
      setNotes(data.notes || '');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load journal entry detail');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchDetail();
    }
  }, [id]);

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
      });

      // Clear form
      setTicker('');
      setPositionType('LONG');
      setEntryPrice('');
      setQuantity('');

      // Refresh entry detail to show new trade
      await fetchDetail();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add trade');
    } finally {
      setAddingTrade(false);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <p className="loading">Loading journal detail...</p>
      </div>
    );
  }

  if (error || !entry) {
    return (
      <div className="container">
        <div className="error-banner">{error || 'Journal entry not found'}</div>
        <Link to="/" className="btn btn-secondary" style={{ marginTop: '1rem', display: 'inline-block' }}>
          &larr; Back to Entries
        </Link>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="back-link">
        <Link to="/">&larr; Back to Entries</Link>
      </div>

      <header className="header">
        <h1>Journal Entry: {entry.entryDate}</h1>
      </header>

      {/* Editable Notes Section */}
      <section className="section-card">
        <h2>Notes</h2>
        <form onSubmit={handleSaveNotes}>
          <div className="form-group">
            <textarea
              rows="6"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Enter notes, observations, or trade analysis..."
            />
          </div>
          <div className="action-row">
            <button type="submit" className="btn btn-primary" disabled={savingNotes}>
              {savingNotes ? 'Saving...' : 'Save Notes'}
            </button>
            {notesSavedAlert && <span className="success-badge">Notes saved!</span>}
          </div>
        </form>
      </section>

      {/* Trades List Section */}
      <section className="section-card">
        <h2>Trades ({entry.trades ? entry.trades.length : 0})</h2>
        {entry.trades && entry.trades.length > 0 ? (
          <table className="trades-table">
            <thead>
              <tr>
                <th>Ticker</th>
                <th>Type</th>
                <th>Entry Price</th>
                <th>Quantity</th>
                <th>Source</th>
              </tr>
            </thead>
            <tbody>
              {entry.trades.map((t) => (
                <tr key={t.tradeId || t.id}>
                  <td><strong>{t.ticker}</strong></td>
                  <td>
                    <span className={`badge ${t.positionType === 'LONG' ? 'badge-long' : 'badge-short'}`}>
                      {t.positionType}
                    </span>
                  </td>
                  <td>{t.entryPrice}</td>
                  <td>{t.quantity}</td>
                  <td>{t.source || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
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
              <input
                type="text"
                id="ticker"
                placeholder="e.g. AAPL, TSLA"
                value={ticker}
                onChange={(e) => setTicker(e.target.value.toUpperCase())}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="positionType">Position Type</label>
              <select
                id="positionType"
                value={positionType}
                onChange={(e) => setPositionType(e.target.value)}
              >
                <option value="LONG">LONG</option>
                <option value="SHORT">SHORT</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="entryPrice">Entry Price</label>
              <input
                type="number"
                step="any"
                id="entryPrice"
                placeholder="0.00"
                value={entryPrice}
                onChange={(e) => setEntryPrice(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="quantity">Quantity</label>
              <input
                type="number"
                step="any"
                id="quantity"
                placeholder="0"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                required
              />
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
