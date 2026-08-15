import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { closeTrade, deleteTrade, getTrades, updateTrade } from '../api/journal';
import TradingViewImportModal from '../components/TradingViewImportModal';

const DEFAULT_FILTERS = {
  ticker: '',
  strategy: '',
  status: '',
  outcome: '',
  fromDate: '',
  toDate: '',
  includeArchived: false,
  page: 0,
  size: 20,
  sort: 'entryDate,desc',
};

const EMPTY_EDIT_FORM = {
  ticker: '',
  positionType: 'LONG',
  entryPrice: '',
  quantity: '',
  stopLoss: '',
  strategy: '',
};

const EMPTY_CLOSE_FORM = {
  exitPrice: '',
  fees: '0',
};

export default function Trades() {
  const [draftFilters, setDraftFilters] = useState(DEFAULT_FILTERS);
  const [queryFilters, setQueryFilters] = useState(DEFAULT_FILTERS);
  const [pageData, setPageData] = useState({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 20,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [selectedTrade, setSelectedTrade] = useState(null);
  const [modalMode, setModalMode] = useState(null);
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [editForm, setEditForm] = useState(EMPTY_EDIT_FORM);
  const [closeForm, setCloseForm] = useState(EMPTY_CLOSE_FORM);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchTrades = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await getTrades(queryFilters);
        setPageData({
          content: data.content || [],
          totalElements: data.totalElements || 0,
          totalPages: data.totalPages || 0,
          number: data.number || 0,
          size: data.size || queryFilters.size,
        });
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load trades');
      } finally {
        setLoading(false);
      }
    };

    fetchTrades();
  }, [queryFilters]);

  const applyFilters = (event) => {
    event.preventDefault();
    setQueryFilters((current) => ({
      ...draftFilters,
      page: 0,
      sort: current.sort,
      size: draftFilters.size,
    }));
  };

  const resetFilters = () => {
    setDraftFilters(DEFAULT_FILTERS);
    setQueryFilters(DEFAULT_FILTERS);
  };

  const updateSort = (field) => {
    setQueryFilters((current) => {
      const [currentField, currentDirection] = (current.sort || 'entryDate,desc').split(',');
      const nextDirection = currentField === field && currentDirection === 'asc' ? 'desc' : 'asc';
      return {
        ...current,
        page: 0,
        sort: `${field},${nextDirection}`,
      };
    });
  };

  const openEditModal = (trade) => {
    setSelectedTrade(trade);
    setModalMode('edit');
    setEditForm({
      ticker: trade.ticker || '',
      positionType: trade.positionType || 'LONG',
      entryPrice: trade.entryPrice ?? '',
      quantity: trade.quantity ?? '',
      stopLoss: trade.stopLoss ?? '',
      strategy: trade.strategy || '',
    });
  };

  const openCloseModal = (trade) => {
    setSelectedTrade(trade);
    setModalMode('close');
    setCloseForm({
      exitPrice: '',
      fees: trade.fees ?? '0',
    });
  };

  const closeModal = () => {
    setModalMode(null);
    setSelectedTrade(null);
    setEditForm(EMPTY_EDIT_FORM);
    setCloseForm(EMPTY_CLOSE_FORM);
  };

  const refreshPage = () => {
    setQueryFilters((current) => ({ ...current }));
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();
    if (!selectedTrade) return;

    try {
      setSubmitting(true);
      await updateTrade(selectedTrade.tradeId, {
        ticker: editForm.ticker.trim().toUpperCase(),
        positionType: editForm.positionType,
        entryPrice: Number(editForm.entryPrice),
        quantity: Number(editForm.quantity),
        stopLoss: editForm.stopLoss === '' ? null : Number(editForm.stopLoss),
        strategy: editForm.strategy.trim() === '' ? null : editForm.strategy.trim(),
      });
      closeModal();
      refreshPage();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update trade');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCloseSubmit = async (event) => {
    event.preventDefault();
    if (!selectedTrade) return;

    try {
      setSubmitting(true);
      await closeTrade(selectedTrade.tradeId, {
        exitPrice: Number(closeForm.exitPrice),
        fees: closeForm.fees === '' ? undefined : Number(closeForm.fees),
      });
      closeModal();
      refreshPage();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to close trade');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (trade) => {
    if (!window.confirm(`Archive trade ${trade.ticker}?`)) {
      return;
    }

    try {
      await deleteTrade(trade.tradeId);
      refreshPage();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete trade');
    }
  };

  const changePage = (nextPage) => {
    setQueryFilters((current) => ({
      ...current,
      page: nextPage,
    }));
  };

  const [sortField, sortDirection] = (queryFilters.sort || 'entryDate,desc').split(',');

  const formatNumber = (value, digits = 2) => {
    if (value === null || value === undefined || value === '') {
      return '-';
    }

    const numeric = Number(value);
    if (Number.isNaN(numeric)) {
      return '-';
    }

    return numeric.toLocaleString(undefined, {
      minimumFractionDigits: digits,
      maximumFractionDigits: digits,
    });
  };

  const formatSigned = (value) => {
    if (value === null || value === undefined) {
      return '-';
    }

    const numeric = Number(value);
    if (Number.isNaN(numeric)) {
      return '-';
    }

    const prefix = numeric > 0 ? '+' : '';
    return `${prefix}${formatNumber(numeric)}`;
  };

  const renderSortLabel = (field, label) => {
    const active = sortField === field;
    const indicator = active ? (sortDirection === 'asc' ? ' ^' : ' v') : '';

    return (
      <button
        type="button"
        className={`sort-button${active ? ' is-selected' : ''}`}
        onClick={() => updateSort(field)}
      >
        {label}
        <span className="sort-indicator">{indicator}</span>
      </button>
    );
  };

  const trades = pageData.content || [];

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>Trades</h1>
          <p className="muted" style={{ margin: '0.25rem 0 0 0', fontSize: '0.9rem' }}>
            Flat view across all journal entries
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <Link to="/import" className="btn btn-primary" style={{ textDecoration: 'none' }}>
            📥 Import TradingView
          </Link>
          <Link to="/" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
            Journal
          </Link>
          <Link to="/stats" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
            Stats
          </Link>
          <Link to="/profile" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
            Profile
          </Link>
        </div>
      </header>

      <section className="section-card">
        <form className="trade-filter-bar" onSubmit={applyFilters}>
          <div className="filters-grid">
            <div className="form-group">
              <label htmlFor="ticker">Ticker</label>
              <input
                id="ticker"
                type="text"
                value={draftFilters.ticker}
                onChange={(e) => setDraftFilters({ ...draftFilters, ticker: e.target.value, page: 0 })}
                placeholder="AAPL"
              />
            </div>
            <div className="form-group">
              <label htmlFor="strategy">Strategy</label>
              <input
                id="strategy"
                type="text"
                value={draftFilters.strategy}
                onChange={(e) => setDraftFilters({ ...draftFilters, strategy: e.target.value, page: 0 })}
                placeholder="Breakout"
              />
            </div>
            <div className="form-group">
              <label htmlFor="status">Status</label>
              <select
                id="status"
                value={draftFilters.status}
                onChange={(e) => setDraftFilters({ ...draftFilters, status: e.target.value, page: 0 })}
              >
                <option value="">All</option>
                <option value="OPEN">Open</option>
                <option value="CLOSED">Closed</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="outcome">Outcome</label>
              <select
                id="outcome"
                value={draftFilters.outcome}
                onChange={(e) => setDraftFilters({ ...draftFilters, outcome: e.target.value, page: 0 })}
              >
                <option value="">All</option>
                <option value="WIN">Win</option>
                <option value="LOSS">Loss</option>
                <option value="BREAKEVEN">Breakeven</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="fromDate">From Date</label>
              <input
                id="fromDate"
                type="date"
                value={draftFilters.fromDate}
                onChange={(e) => setDraftFilters({ ...draftFilters, fromDate: e.target.value, page: 0 })}
              />
            </div>
            <div className="form-group">
              <label htmlFor="toDate">To Date</label>
              <input
                id="toDate"
                type="date"
                value={draftFilters.toDate}
                onChange={(e) => setDraftFilters({ ...draftFilters, toDate: e.target.value, page: 0 })}
              />
            </div>
            <div className="form-group">
              <label htmlFor="size">Page Size</label>
              <select
                id="size"
                value={draftFilters.size}
                onChange={(e) =>
                  setDraftFilters({
                    ...draftFilters,
                    size: Number(e.target.value),
                    page: 0,
                  })
                }
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </div>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={draftFilters.includeArchived}
                onChange={(e) =>
                  setDraftFilters({ ...draftFilters, includeArchived: e.target.checked, page: 0 })
                }
              />
              <span>Include archived</span>
            </label>
          </div>

          <div className="filter-actions">
            <button type="submit" className="btn btn-primary">
              Apply Filters
            </button>
            <button type="button" className="btn btn-secondary" onClick={resetFilters}>
              Reset
            </button>
          </div>
        </form>
      </section>

      {error && <div className="error-banner">{error}</div>}

      <section className="section-card">
        {loading ? (
          <p className="loading">Loading trades...</p>
        ) : trades.length === 0 ? (
          <div className="empty-state">
            <p>No trades found.</p>
          </div>
        ) : (
          <>
            <div className="table-wrap">
              <table className="trades-table trades-table-wide">
                <thead>
                  <tr>
                    <th>{renderSortLabel('entryDate', 'Entry Date')}</th>
                    <th>{renderSortLabel('ticker', 'Ticker')}</th>
                    <th>{renderSortLabel('strategy', 'Strategy')}</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Outcome</th>
                    <th>{renderSortLabel('entryPrice', 'Entry')}</th>
                    <th>{renderSortLabel('quantity', 'Qty')}</th>
                    <th>{renderSortLabel('exitPrice', 'Exit')}</th>
                    <th>Stop Loss</th>
                    <th>Fees</th>
                    <th>Gross PnL</th>
                    <th>Net PnL</th>
                    <th>Risk/Reward</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {trades.map((trade) => {
                    const isOpen = trade.status === 'OPEN';
                    const isArchived = trade.deleted;
                    return (
                      <tr key={trade.tradeId} className={isArchived ? 'row-archived' : ''}>
                        <td>
                          <Link to={`/journal/${trade.journalEntryId}`} className="table-link">
                            {trade.entryDate}
                          </Link>
                        </td>
                        <td><strong>{trade.ticker}</strong></td>
                        <td>{trade.strategy || '-'}</td>
                        <td>
                          <span className={`badge ${trade.positionType === 'LONG' ? 'badge-long' : 'badge-short'}`}>
                            {trade.positionType}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${isOpen ? 'badge-open' : 'badge-closed'}`}>
                            {trade.status}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${trade.outcome ? `badge-${trade.outcome.toLowerCase()}` : 'badge-muted'}`}>
                            {trade.outcome || '-'}
                          </span>
                        </td>
                        <td>{formatNumber(trade.entryPrice)}</td>
                        <td>{formatNumber(trade.quantity, 4)}</td>
                        <td>{trade.exitPrice !== null && trade.exitPrice !== undefined ? formatNumber(trade.exitPrice) : '-'}</td>
                        <td>{trade.stopLoss !== null && trade.stopLoss !== undefined ? formatNumber(trade.stopLoss) : '-'}</td>
                        <td>{formatNumber(trade.fees)}</td>
                        <td className={trade.grossPnl > 0 ? 'pnl-positive' : trade.grossPnl < 0 ? 'pnl-negative' : 'pnl-neutral'}>
                          {formatSigned(trade.grossPnl)}
                        </td>
                        <td className={trade.netPnl > 0 ? 'pnl-positive' : trade.netPnl < 0 ? 'pnl-negative' : 'pnl-neutral'}>
                          {formatSigned(trade.netPnl)}
                        </td>
                        <td>{trade.riskRewardRatio !== null && trade.riskRewardRatio !== undefined ? formatNumber(trade.riskRewardRatio, 4) : '-'}</td>
                        <td>
                          <div className="table-actions">
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              onClick={() => openEditModal(trade)}
                              disabled={!isOpen || isArchived}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              onClick={() => openCloseModal(trade)}
                              disabled={!isOpen || isArchived}
                            >
                              Close
                            </button>
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleDelete(trade)}
                              disabled={isArchived}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="pagination-bar">
              <span className="muted">
                Showing {pageData.totalElements === 0 ? 0 : pageData.number * pageData.size + 1}-
                {Math.min((pageData.number + 1) * pageData.size, pageData.totalElements)} of {pageData.totalElements}
              </span>
              <div className="pagination-actions">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => changePage(Math.max(pageData.number - 1, 0))}
                  disabled={pageData.number === 0}
                >
                  Prev
                </button>
                <span className="page-chip">
                  Page {pageData.number + 1} of {Math.max(pageData.totalPages, 1)}
                </span>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => changePage(pageData.number + 1)}
                  disabled={pageData.number + 1 >= pageData.totalPages}
                >
                  Next
                </button>
              </div>
            </div>
          </>
        )}
      </section>

      {modalMode && selectedTrade && (
        <div className="modal-backdrop" onClick={closeModal}>
          <div className="modal-content modal-wide" onClick={(event) => event.stopPropagation()}>
            <h2>{modalMode === 'edit' ? 'Edit Trade' : 'Close Trade'}</h2>

            {modalMode === 'edit' ? (
              <form onSubmit={handleEditSubmit}>
                <div className="modal-grid">
                  <div className="form-group">
                    <label htmlFor="editTicker">Ticker</label>
                    <input
                      id="editTicker"
                      type="text"
                      value={editForm.ticker}
                      onChange={(e) => setEditForm({ ...editForm, ticker: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="editType">Position Type</label>
                    <select
                      id="editType"
                      value={editForm.positionType}
                      onChange={(e) => setEditForm({ ...editForm, positionType: e.target.value })}
                      required
                    >
                      <option value="LONG">LONG</option>
                      <option value="SHORT">SHORT</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label htmlFor="editEntryPrice">Entry Price</label>
                    <input
                      id="editEntryPrice"
                      type="number"
                      step="any"
                      value={editForm.entryPrice}
                      onChange={(e) => setEditForm({ ...editForm, entryPrice: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="editQuantity">Quantity</label>
                    <input
                      id="editQuantity"
                      type="number"
                      step="any"
                      value={editForm.quantity}
                      onChange={(e) => setEditForm({ ...editForm, quantity: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="editStopLoss">Stop Loss</label>
                    <input
                      id="editStopLoss"
                      type="number"
                      step="any"
                      value={editForm.stopLoss}
                      onChange={(e) => setEditForm({ ...editForm, stopLoss: e.target.value })}
                    />
                  </div>
                  <div className="form-group modal-grid-full">
                    <label htmlFor="editStrategy">Strategy</label>
                    <input
                      id="editStrategy"
                      type="text"
                      value={editForm.strategy}
                      onChange={(e) => setEditForm({ ...editForm, strategy: e.target.value })}
                    />
                  </div>
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={closeModal}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Saving...' : 'Save Trade'}
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleCloseSubmit}>
                <div className="modal-grid">
                  <div className="form-group">
                    <label htmlFor="closeExitPrice">Exit Price</label>
                    <input
                      id="closeExitPrice"
                      type="number"
                      step="any"
                      value={closeForm.exitPrice}
                      onChange={(e) => setCloseForm({ ...closeForm, exitPrice: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="closeFees">Fees</label>
                    <input
                      id="closeFees"
                      type="number"
                      step="any"
                      value={closeForm.fees}
                      onChange={(e) => setCloseForm({ ...closeForm, fees: e.target.value })}
                    />
                  </div>
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={closeModal}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Closing...' : 'Close Trade'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      <TradingViewImportModal
        isOpen={isImportModalOpen}
        onClose={() => setIsImportModalOpen(false)}
        onImportSuccess={applyFilters}
      />
    </div>
  );
}
