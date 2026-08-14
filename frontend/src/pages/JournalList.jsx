import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getJournalEntries, createJournalEntry } from '../api/journal';
import { useAuth } from '../context/AuthContext';

export default function JournalList() {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const fetchEntries = async () => {
    try {
      setLoading(true);
      const data = await getJournalEntries();
      // Ensure sorted newest first by entryDate
      const sorted = Array.isArray(data)
        ? data.sort((a, b) => new Date(b.entryDate) - new Date(a.entryDate))
        : [];
      setEntries(sorted);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load journal entries');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEntries();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!entryDate) return;
    try {
      setSubmitting(true);
      const created = await createJournalEntry({ entryDate, notes });
      setShowModal(false);
      setNotes('');
      // Navigate directly to detail view of new entry
      const newId = created.journalEntryId || created.id;
      if (newId) {
        navigate(`/journal/${newId}`);
      } else {
        fetchEntries();
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create journal entry');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>Trading Journal</h1>
          {user && (
            <p className="muted" style={{ margin: '0.25rem 0 0 0', fontSize: '0.9rem' }}>
              Logged in as <strong>{user.fullName || user.email}</strong>
            </p>
          )}
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <Link to="/trades" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
            Trades
          </Link>
          <Link to="/profile" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
            Profile
          </Link>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            + New Entry
          </button>
          <button className="btn btn-secondary" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <p className="loading">Loading journal entries...</p>
      ) : entries.length === 0 ? (
        <div className="empty-state">
          <p>No journal entries yet.</p>
          <button className="btn btn-secondary" onClick={() => setShowModal(true)}>
            Create your first entry
          </button>
        </div>
      ) : (
        <div className="entry-list">
          {entries.map((entry) => {
            const id = entry.journalEntryId || entry.id;
            return (
              <div
                key={id}
                className="entry-card"
                onClick={() => navigate(`/journal/${id}`)}
              >
                <div className="entry-card-header">
                  <span className="entry-date">{entry.entryDate}</span>
                  <span className="trade-count">
                    {entry.trades ? `${entry.trades.length} trades` : ''}
                  </span>
                </div>
                <p className="entry-notes-preview">
                  {entry.notes ? entry.notes : <em>No notes provided.</em>}
                </p>
              </div>
            );
          })}
        </div>
      )}

      {showModal && (
        <div className="modal-backdrop" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>New Journal Entry</h2>
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label htmlFor="entryDate">Entry Date</label>
                <input
                  type="date"
                  id="entryDate"
                  value={entryDate}
                  onChange={(e) => setEntryDate(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="notes">Notes</label>
                <textarea
                  id="notes"
                  rows="4"
                  placeholder="Market conditions, thoughts, strategy details..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>
              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Creating...' : 'Create Entry'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
