import React, { useEffect, useState } from 'react';
import { createTrade, getTrades } from './api/trades';

export default function App() {
  const [ticker, setTicker] = useState('');
  const [positionType, setPositionType] = useState('LONG');
  const [entryPrice, setEntryPrice] = useState('');
  const [quantity, setQuantity] = useState('');
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchTrades();
  }, []);

  const fetchTrades = async () => {
    setLoading(true);
    setError('');

    try {
      const result = await getTrades();
      setTrades(result);
    } catch (err) {
      setError('Unable to load trades. Check backend connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!ticker || !entryPrice || !quantity) {
      setError('Please fill in all fields.');
      return;
    }

    try {
      await createTrade({
        ticker,
        positionType,
        entryPrice,
        quantity,
      });
      setTicker('');
      setEntryPrice('');
      setQuantity('');
      setPositionType('LONG');
      fetchTrades();
    } catch (err) {
      setError('Failed to create trade.');
    }
  };

  return (
    <div style={{ maxWidth: 760, margin: '0 auto', padding: 24, fontFamily: 'Arial, sans-serif' }}>
      <h1>Trading Journal</h1>
      <p>Record new trades and review your current trade list.</p>

      <form onSubmit={handleSubmit} style={{ marginBottom: 24, gap: 12, display: 'grid' }}>
        <label>
          Ticker
          <input
            type="text"
            value={ticker}
            onChange={(e) => setTicker(e.target.value.toUpperCase())}
            placeholder="AAPL"
            style={{ width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>

        <label>
          Position Type
          <select
            value={positionType}
            onChange={(e) => setPositionType(e.target.value)}
            style={{ width: '100%', padding: 8, marginTop: 4 }}
          >
            <option value="LONG">LONG</option>
            <option value="SHORT">SHORT</option>
          </select>
        </label>

        <label>
          Entry Price
          <input
            type="number"
            step="0.0001"
            value={entryPrice}
            onChange={(e) => setEntryPrice(e.target.value)}
            placeholder="123.45"
            style={{ width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>

        <label>
          Quantity
          <input
            type="number"
            step="0.01"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            placeholder="1.00"
            style={{ width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>

        <button type="submit" style={{ padding: '12px 18px', fontSize: 16, cursor: 'pointer' }}>
          Add Trade
        </button>
      </form>

      {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}

      <section>
        <h2>Trades</h2>
        {loading ? (
          <p>Loading trades…</p>
        ) : trades.length === 0 ? (
          <p>No trades recorded yet.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left', borderBottom: '2px solid #ccc' }}>
                <th style={{ padding: 8 }}>Ticker</th>
                <th style={{ padding: 8 }}>Position</th>
                <th style={{ padding: 8 }}>Entry Price</th>
                <th style={{ padding: 8 }}>Quantity</th>
                <th style={{ padding: 8 }}>Created At</th>
              </tr>
            </thead>
            <tbody>
              {trades.map((trade) => (
                <tr key={trade.tradeId} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: 8 }}>{trade.ticker}</td>
                  <td style={{ padding: 8 }}>{trade.positionType}</td>
                  <td style={{ padding: 8 }}>{trade.entryPrice}</td>
                  <td style={{ padding: 8 }}>{trade.quantity}</td>
                  <td style={{ padding: 8 }}>{new Date(trade.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
