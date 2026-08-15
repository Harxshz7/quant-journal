import React, { useRef, useState } from 'react';
import { importTradingViewCsv } from '../api/journal';
import '../styles/TradingViewImportModal.css';

export default function TradingViewImportModal({ isOpen, onClose, onImportSuccess, journalEntryId }) {
  const [selectedFile, setSelectedFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  if (!isOpen) return null;

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        setError('Please select a valid CSV file (.csv)');
        return;
      }
      setSelectedFile(file);
      setError(null);
      setSummary(null);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files?.[0];
    if (file) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        setError('Please select a valid CSV file (.csv)');
        return;
      }
      setSelectedFile(file);
      setError(null);
      setSummary(null);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsUploading(true);
    setError(null);

    try {
      const res = await importTradingViewCsv(selectedFile, journalEntryId);
      setSummary(res);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to import CSV';
      setError(msg);
    } finally {
      setIsUploading(false);
    }
  };

  const handleDone = () => {
    if (summary && summary.imported > 0) {
      onImportSuccess?.();
    }
    handleClose();
  };

  const handleClose = () => {
    setSelectedFile(null);
    setSummary(null);
    setError(null);
    setIsUploading(false);
    onClose();
  };

  return (
    <div className="import-modal-overlay">
      <div className="import-modal-content">
        <div className="import-modal-header">
          <h3>📥 Import TradingView CSV</h3>
          <button className="close-modal-btn" onClick={handleClose} type="button">
            &times;
          </button>
        </div>

        <div className="import-modal-body">
          {error && <div className="error-message mb-3">{error}</div>}

          {!summary ? (
            <>
              <div
                className="dropzone"
                onDrop={handleDrop}
                onDragOver={handleDragOver}
                onClick={() => fileInputRef.current?.click()}
              >
                <div className="dropzone-icon">📄</div>
                <p>Click or drag & drop TradingView CSV file here</p>
                <span>Supports Order History & Account History exports</span>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".csv"
                  onChange={handleFileSelect}
                  style={{ display: 'none' }}
                />
              </div>

              {selectedFile && (
                <div className="selected-file-info">
                  <span>📄 {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)</span>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => setSelectedFile(null)}
                    type="button"
                  >
                    Remove
                  </button>
                </div>
              )}

              <div className="import-actions">
                <button className="btn btn-secondary" onClick={handleClose} type="button" disabled={isUploading}>
                  Cancel
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleUpload}
                  disabled={!selectedFile || isUploading}
                  type="button"
                >
                  {isUploading ? '⏳ Importing...' : 'Import CSV'}
                </button>
              </div>
            </>
          ) : (
            <>
              <div className="summary-cards-grid">
                <div className="summary-card">
                  <div className="summary-card-value">{summary.totalRows}</div>
                  <div className="summary-card-label">Total Rows</div>
                </div>
                <div className="summary-card">
                  <div className="summary-card-value imported">{summary.imported}</div>
                  <div className="summary-card-label">Imported</div>
                </div>
                <div className="summary-card">
                  <div className="summary-card-value duplicates">{summary.duplicatesSkipped}</div>
                  <div className="summary-card-label">Duplicates</div>
                </div>
              </div>

              {summary.unmappedHeaders && summary.unmappedHeaders.length > 0 && (
                <div className="unmapped-headers-section">
                  <div className="unmapped-headers-title">Unmapped Columns Ignored:</div>
                  <div className="unmapped-tags">
                    {summary.unmappedHeaders.map((header, idx) => (
                      <span key={idx} className="unmapped-tag">
                        {header}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {summary.errors && summary.errors.length > 0 && (
                <div>
                  <div className="errors-section-title">Row Warnings / Errors ({summary.errors.length}):</div>
                  <div className="errors-table-container">
                    <table className="errors-table">
                      <thead>
                        <tr>
                          <th>Row</th>
                          <th>Reason / Message</th>
                        </tr>
                      </thead>
                      <tbody>
                        {summary.errors.map((err, idx) => (
                          <tr key={idx}>
                            <td className="row-num">#{err.row}</td>
                            <td className="reason">{err.reason}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              <div className="import-actions">
                <button className="btn btn-primary" onClick={handleDone} type="button">
                  Done
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
