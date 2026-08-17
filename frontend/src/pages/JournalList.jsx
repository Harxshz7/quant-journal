import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getJournalEntries, createJournalEntry } from '../api/journal';
import NavBar from '../components/NavBar';

const MOOD_OPTIONS = ['GREAT', 'GOOD', 'NEUTRAL', 'POOR', 'TERRIBLE'];

export default function JournalList() {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [notes, setNotes] = useState('');
  const [mood, setMood] = useState('');
  const [energy, setEnergy] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate();

  const fetchEntries = async () => {
    try {
      setLoading(true);
      const data = await getJournalEntries();
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

  useEffect(() => { fetchEntries(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!entryDate) return;
    try {
      setSubmitting(true);
      const payload = { entryDate, notes };
      if (mood) payload.mood = mood;
      if (energy) payload.energy = parseInt(energy);
      const created = await createJournalEntry(payload);
      setShowModal(false);
      setNotes('');
      setMood('');
      setEnergy('');
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
      <NavBar active="journal">
        <button className="btn btn-primary btn-sm" onClick={() => setShowModal(true)}>
          + New Entry
        </button>
      </NavBar>

      <header className="header">
        <div>
          <h1>Trading Journal</h1>
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
                {(entry.mood || entry.energy || entry.dayRating) && (
                  <div className="entry-meta-row">
                    {entry.mood && (
                      <span className="tag-chip">{entry.mood}</span>
                    )}
                    {entry.energy && (
                      <span className="tag-chip">Energy {entry.energy}</span>
                    )}
                    {entry.dayRating && (
                      <span className="tag-chip">Rating {entry.dayRating}/5</span>
                    )}
                  </div>
                )}
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
                <label>Pre-Market Mood</label>
                <div className="mood-btn-row">
                  {MOOD_OPTIONS.map(m => (
                    <button
                      key={m}
                      type="button"
                      className={`btn btn-sm mood-btn ${mood === m ? 'is-selected' : ''}`}
                      onClick={() => setMood(mood === m ? '' : m)}
                    >
                      {m}
                    </button>
                  ))}
                </div>
              </div>
              <div className="form-group">
                <label>Pre-Market Energy (1-5)</label>
                <div className="energy-btn-row">
                  {[1,2,3,4,5].map(n => (
                    <button
                      key={n}
                      type="button"
                      className={`btn btn-sm energy-btn ${energy == n ? 'is-selected' : ''}`}
                      onClick={() => setEnergy(energy == n ? '' : n)}
                    >
                      {n}
                    </button>
                  ))}
                </div>
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
