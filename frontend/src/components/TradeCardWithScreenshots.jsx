import React, { useState } from 'react';
import ScreenshotUpload from './ScreenshotUpload';
import ScreenshotGallery from './ScreenshotGallery';
import TradingViewChart from './TradingViewChart';
import { getScreenshotsForTrade } from '../api/journal';
import { mapTradingViewSymbol } from '../utils/tradingViewSymbol';
import '../styles/TradeCardWithScreenshots.css';

/**
 * TradeCardWithScreenshots Component
 * Renders a trade with screenshot upload and gallery
 * @param {Object} trade - Trade data object
 * @param {function} onTradeUpdate - Callback to refresh trade data from parent
 */
export default function TradeCardWithScreenshots({ trade, onTradeUpdate }) {
  const [screenshots, setScreenshots] = useState(trade.screenshots || []);
  const [expanded, setExpanded] = useState(false);
  const [loadingScreenshots, setLoadingScreenshots] = useState(false);
  const [error, setError] = useState(null);

  const tradeId = trade.tradeId || trade.id;
  const isClosed = trade.status === 'CLOSED' || trade.exitPrice != null;
  const pnl = trade.netPnl ?? trade.realizedPnl ?? trade.grossPnl;

  const handleExpand = async () => {
    if (!expanded && screenshots.length === 0) {
      // Load screenshots if not already loaded
      setLoadingScreenshots(true);
      try {
        const loaded = await getScreenshotsForTrade(tradeId);
        setScreenshots(loaded);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load screenshots');
      } finally {
        setLoadingScreenshots(false);
      }
    }
    setExpanded(!expanded);
  };

  const handleUploadSuccess = async (newScreenshot) => {
    setScreenshots((prev) => [newScreenshot, ...prev]);
    onTradeUpdate?.();
  };

  const handleUploadError = (errorMsg) => {
    setError(errorMsg);
  };

  const handleDeleteSuccess = (deletedId) => {
    setScreenshots((prev) => prev.filter((s) => s.id !== deletedId));
    setError(null);
    onTradeUpdate?.();
  };

  const handleDeleteError = (errorMsg) => {
    setError(errorMsg);
  };

  const handleOpenTradingView = (e) => {
    e.stopPropagation();
    const mappedSymbol = mapTradingViewSymbol(trade.ticker);
    window.open(`https://www.tradingview.com/chart/?symbol=${encodeURIComponent(mappedSymbol)}`, '_blank', 'noopener,noreferrer');
  };

  const pnlClass = isClosed ? (pnl > 0 ? 'pnl-positive' : pnl < 0 ? 'pnl-negative' : 'pnl-neutral') : 'pnl-neutral';
  const pnlDisplay = isClosed ? (pnl > 0 ? `+${pnl}` : pnl) : '-';

  return (
    <div className="trade-card-with-screenshots">
      <div className="trade-card-header">
        <div className="trade-info">
          <div className="trade-ticker">
            <strong>{trade.ticker}</strong>
            <span className={`badge ${trade.positionType === 'LONG' ? 'badge-long' : 'badge-short'}`}>
              {trade.positionType}
            </span>
          </div>
          <div className="trade-prices">
            <span>Entry: ${trade.entryPrice}</span>
            <span className="separator">•</span>
            <span>Qty: {trade.quantity}</span>
            {isClosed && (
              <>
                <span className="separator">•</span>
                <span>Exit: ${trade.exitPrice}</span>
              </>
            )}
          </div>
        </div>

        <div className="trade-stats" style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <div className={`pnl-badge ${pnlClass}`}>{pnlDisplay}</div>
          <button
            className="btn btn-secondary btn-sm"
            onClick={handleOpenTradingView}
            type="button"
            title="Open symbol chart on TradingView in new tab"
            style={{ fontSize: '0.8rem', padding: '0.25rem 0.5rem' }}
          >
             TradingView
          </button>
          <button
            className={`expand-btn ${expanded ? 'expanded' : ''}`}
            onClick={handleExpand}
            type="button"
            title={expanded ? 'Collapse' : 'Expand'}
          >
            {expanded ? '▼' : ''}
          </button>
        </div>
      </div>

      {expanded && (
        <div className="trade-card-details">
          <TradingViewChart symbol={trade.ticker} theme="dark" height={420} />

          {trade.strategy && (
            <div className="detail-row">
              <span className="detail-label">Strategy:</span>
              <span className="detail-value">{trade.strategy}</span>
            </div>
          )}

          {trade.stopLoss && (
            <div className="detail-row">
              <span className="detail-label">Stop Loss:</span>
              <span className="detail-value">${trade.stopLoss}</span>
            </div>
          )}

          {trade.riskRewardRatio && (
            <div className="detail-row">
              <span className="detail-label">R/R Ratio:</span>
              <span className="detail-value">{trade.riskRewardRatio}</span>
            </div>
          )}

          {trade.fees && parseFloat(trade.fees) > 0 && (
            <div className="detail-row">
              <span className="detail-label">Fees:</span>
              <span className="detail-value">${trade.fees}</span>
            </div>
          )}

          <div className="screenshots-section">
            <div className="screenshots-header">
              <h4>Screenshots ({screenshots.length})</h4>
            </div>

            {error && <div className="error-message">{error}</div>}

            <ScreenshotUpload
              tradeId={tradeId}
              onUploadSuccess={handleUploadSuccess}
              onUploadError={handleUploadError}
            />

            {loadingScreenshots && <div className="loading">Loading screenshots...</div>}

            {screenshots.length > 0 && (
              <ScreenshotGallery
                screenshots={screenshots}
                onDeleteSuccess={handleDeleteSuccess}
                onDeleteError={handleDeleteError}
              />
            )}

            {!loadingScreenshots && screenshots.length === 0 && (
              <p className="muted">No screenshots yet. Click "Add Screenshot" to upload.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
