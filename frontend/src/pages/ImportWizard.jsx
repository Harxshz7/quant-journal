import React, { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Download, Zap, FileText, CheckCircle2, Lightbulb, Hourglass } from 'lucide-react';
import { importTradingViewCsv } from '../api/journal';
import NavBar from '../components/NavBar';
import '../styles/ImportWizard.css';

export default function ImportWizard() {
  const [currentStep, setCurrentStep] = useState(1);
  const [exportFormat, setExportFormat] = useState('orderHistory');
  const [selectedFile, setSelectedFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        setError('Please select a valid CSV file (.csv)');
        return;
      }
      setSelectedFile(file);
      setError(null);
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
      const res = await importTradingViewCsv(selectedFile);
      setSummary(res);
      setCurrentStep(4);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to import CSV file';
      setError(msg);
    } finally {
      setIsUploading(false);
    }
  };

  const getErrorGuidance = (reason) => {
    if (!reason) return null;
    const lower = reason.toLowerCase();
    if (lower.includes('unmapped header') || lower.includes('missing required column')) {
      return "This column name wasn't recognized. Check that it matches the header names in the sample CSV.";
    }
    if (lower.includes('invalid price') || lower.includes('invalid quantity') || lower.includes('malformed')) {
      return "Check for currency symbols ($), commas, or non-numeric formatting in price and quantity columns.";
    }
    if (lower.includes('duplicate') || lower.includes('already exists')) {
      return "Already imported. This is expected on re-upload and does not require fixing.";
    }
    if (lower.includes('invalid side') || lower.includes('position type')) {
      return "Check position side value. Supported values are Buy, Sell, Long, or Short.";
    }
    if (lower.includes('warning: csv p&l')) {
      return "Discrepancy detected between CSV P&L and calculated P&L. P&L is derived automatically from entry/exit/fees.";
    }
    return null;
  };

  return (
    <div className="container">
      <NavBar active="import" />

      <header className="header">
        <div>
          <h1><Download size={32} className="inline mr-2" /> TradingView Import Wizard</h1>
          <p className="muted section-subtitle">
            Follow guided steps to import your order or trade history from TradingView
          </p>
        </div>
      </header>

      <div className="wizard-card">
        {/* Step Indicator */}
        <div className="step-indicator-bar">
          <div
            className={`step-indicator-item ${currentStep === 1 ? 'active' : currentStep > 1 ? 'completed' : ''}`}
            onClick={() => currentStep > 1 && setCurrentStep(1)}
          >
            <div className="step-circle">{currentStep > 1 ? <CheckCircle2 size={16} /> : '1'}</div>
            <div className="step-title">1. Export Guide</div>
          </div>
          <div
            className={`step-indicator-item ${currentStep === 2 ? 'active' : currentStep > 2 ? 'completed' : ''}`}
            onClick={() => currentStep > 2 && setCurrentStep(2)}
          >
            <div className="step-circle">{currentStep > 2 ? <CheckCircle2 size={16} /> : '2'}</div>
            <div className="step-title">2. Download Samples</div>
          </div>
          <div
            className={`step-indicator-item ${currentStep === 3 ? 'active' : currentStep > 3 ? 'completed' : ''}`}
            onClick={() => currentStep > 3 && setCurrentStep(3)}
          >
            <div className="step-circle">{currentStep > 3 ? <CheckCircle2 size={16} /> : '3'}</div>
            <div className="step-title">3. Upload CSV</div>
          </div>
          <div className={`step-indicator-item ${currentStep === 4 ? 'active' : ''}`}>
            <div className="step-circle">4</div>
            <div className="step-title">4. Results</div>
          </div>
        </div>

        {/* Step Content */}
        <div className="wizard-step-content">
          {/* STEP 1: EXPORT GUIDE */}
          {currentStep === 1 && (
            <div>
              <div className="wizard-step-header">
                <h3>Step 1: Exporting from TradingView</h3>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => setCurrentStep(3)}
                >
                  <Zap size={16} className="inline mr-1" /> Skip to Upload
                </button>
              </div>

              <div className="format-toggle">
                <button
                  type="button"
                  className={`format-btn ${exportFormat === 'orderHistory' ? 'active' : ''}`}
                  onClick={() => setExportFormat('orderHistory')}
                >
                  <h4>Order History Format</h4>
                  <p>Individual execution fills with Symbol, Side (Buy/Sell), Qty, Price, Time, Order ID</p>
                </button>
                <button
                  type="button"
                  className={`format-btn ${exportFormat === 'accountHistory' ? 'active' : ''}`}
                  onClick={() => setExportFormat('accountHistory')}
                >
                  <h4>Account History / Positions Format</h4>
                  <p>Completed trades with Entry Price, Exit Price, Open/Close Time, Commission, Trade ID</p>
                </button>
              </div>

              <div className="instruction-steps">
                {exportFormat === 'orderHistory' ? (
                  <>
                    <div className="instruction-step">
                      <div className="step-num">1</div>
                      <div className="step-desc">
                        <h5>Open TradingView Account Panel</h5>
                        <p>Open any chart on TradingView and look at the bottom panel showing your connected Broker account tab.</p>
                        <div className="generic-diagram">
                          <span className="diagram-badge">BOTTOM PANEL</span>
                          <span>[ Trading Panel ] &gt; [ Broker Tab (e.g. Paper Trading / Tradovate) ]</span>
                        </div>
                      </div>
                    </div>

                    <div className="instruction-step">
                      <div className="step-num">2</div>
                      <div className="step-desc">
                        <h5>Select the Order History Tab</h5>
                        <p>Click on the <strong>Order History</strong> or <strong>Orders</strong> sub-tab inside the broker panel.</p>
                      </div>
                    </div>

                    <div className="instruction-step">
                      <div className="step-num">3</div>
                      <div className="step-desc">
                        <h5>Export to CSV</h5>
                        <p>Click the export icon (<Download size={14} className="inline" />) at the top right of the table grid and choose <strong>Export Data to CSV</strong>.</p>
                      </div>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="instruction-step">
                      <div className="step-num">1</div>
                      <div className="step-desc">
                        <h5>Open Positions / Account Summary</h5>
                        <p>Open the TradingView bottom panel and select your active Broker account tab.</p>
                        <div className="generic-diagram">
                          <span className="diagram-badge">BOTTOM PANEL</span>
                          <span>[ Account History ] or [ Closed Positions ] Tab</span>
                        </div>
                      </div>
                    </div>

                    <div className="instruction-step">
                      <div className="step-num">2</div>
                      <div className="step-desc">
                        <h5>Filter Desired Date Range</h5>
                        <p>Ensure the history table displays the date range you wish to import.</p>
                      </div>
                    </div>

                    <div className="instruction-step">
                      <div className="step-num">3</div>
                      <div className="step-desc">
                        <h5>Export to CSV</h5>
                        <p>Click the table export icon (<Download size={14} className="inline" />) and select <strong>Export to CSV</strong>.</p>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </div>
          )}

          {/* STEP 2: DOWNLOAD SAMPLES */}
          {currentStep === 2 && (
            <div>
              <div className="wizard-step-header">
                <h3>Step 2: Download Sample Files & Verify Layout</h3>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => setCurrentStep(3)}
                >
                   <Zap size={16} className="inline mr-1" /> Skip to Upload
                </button>
              </div>
              <p className="muted wizard-mb">
                Compare your CSV headers against our known-good templates. The importer automatically handles lowercase/uppercase variations.
              </p>

              <div className="samples-grid">
                <div className="sample-card">
                  <div>
                    <h4>Order History Sample</h4>
                    <p>Ideal for importing individual fills or single order logs.</p>
                    <table className="sample-preview-table">
                      <thead>
                        <tr>
                          <th>Symbol</th>
                          <th>Side</th>
                          <th>Qty</th>
                          <th>Fill Price</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>AAPL</td>
                          <td>Buy</td>
                          <td>100</td>
                          <td>185.50</td>
                        </tr>
                        <tr>
                          <td>TSLA</td>
                          <td>Sell</td>
                          <td>50</td>
                          <td>220.00</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <a
                    href="/samples/tradingview-order-history-sample.csv"
                    download="tradingview-order-history-sample.csv"
                    className="btn btn-secondary btn-sm download-link"
                  >
                     <Download size={16} className="inline mr-1" /> Download Order History Sample (.csv)
                  </a>
                </div>

                <div className="sample-card">
                  <div>
                    <h4>Account History Sample</h4>
                    <p>Ideal for importing complete trades with entry & exit prices.</p>
                    <table className="sample-preview-table">
                      <thead>
                        <tr>
                          <th>Symbol</th>
                          <th>Side</th>
                          <th>Entry</th>
                          <th>Exit</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>MSFT</td>
                          <td>Long</td>
                          <td>410.00</td>
                          <td>418.50</td>
                        </tr>
                        <tr>
                          <td>AMD</td>
                          <td>Short</td>
                          <td>162.00</td>
                          <td>158.40</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <a
                    href="/samples/tradingview-account-history-sample.csv"
                    download="tradingview-account-history-sample.csv"
                    className="btn btn-secondary btn-sm download-link"
                  >
                     <Download size={16} className="inline mr-1" /> Download Account History Sample (.csv)
                  </a>
                </div>
              </div>
            </div>
          )}

          {/* STEP 3: UPLOAD CSV */}
          {currentStep === 3 && (
            <div>
              <h3 className="wizard-step-title">Step 3: Select & Upload Your CSV File</h3>

              {error && <div className="error-message mb-3">{error}</div>}

              <div
                className="dropzone"
                onDrop={handleDrop}
                onDragOver={handleDragOver}
                onClick={() => fileInputRef.current?.click()}
              >
                <div className="dropzone-icon"><FileText size={48} /></div>
                <p>Click or drag & drop your TradingView CSV file here</p>
                <span>Supports .csv files up to 10MB</span>
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
                  <span><FileText size={16} className="inline mr-1" /> {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)</span>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => setSelectedFile(null)}
                    type="button"
                  >
                    Remove
                  </button>
                </div>
              )}
            </div>
          )}

          {/* STEP 4: RESULTS */}
          {currentStep === 4 && summary && (
            <div>
              <h3 className="wizard-step-title">Step 4: Import Summary & Guidance</h3>

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
                  <div className="errors-section-title">Row Processing Guidance ({summary.errors.length}):</div>
                  <div className="errors-table-container">
                    <table className="errors-table">
                      <thead>
                        <tr>
                          <th>Row</th>
                          <th>Message & Plain-Language Guidance</th>
                        </tr>
                      </thead>
                      <tbody>
                        {summary.errors.map((err, idx) => {
                          const guidance = getErrorGuidance(err.reason);
                          return (
                            <tr key={idx}>
                              <td className="row-num">#{err.row}</td>
                              <td className="reason">
                                <div>{err.reason}</div>
                                {guidance && (
                                  <div className="guidance-box">
                                     <Lightbulb size={16} className="inline mr-1" /> <strong>Guidance:</strong> {guidance}
                                  </div>
                                )}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer Navigation */}
        <div className="wizard-footer">
          <div>
            {currentStep > 1 && currentStep < 4 && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setCurrentStep((prev) => prev - 1)}
                disabled={isUploading}
              >
                ← Back
              </button>
            )}
          </div>

          <div>
            {currentStep === 1 && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setCurrentStep(2)}
              >
                Next: Download Samples →
              </button>
            )}

            {currentStep === 2 && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setCurrentStep(3)}
              >
                Next: Upload CSV →
              </button>
            )}

            {currentStep === 3 && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleUpload}
                disabled={!selectedFile || isUploading}
              >
                {isUploading ? <><Hourglass size={16} className="inline mr-1" /> Importing...</> : 'Upload & Process CSV'}
              </button>
            )}

            {currentStep === 4 && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => navigate('/trades')}
              >
                View Imported Trades →
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
