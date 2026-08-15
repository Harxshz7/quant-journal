/**
 * Maps plain ticker strings (e.g., "RELIANCE", "AAPL") to TradingView compatible EXCHANGE:SYMBOL format.
 * If the ticker already contains a colon (e.g., "NASDAQ:AAPL", "NSE:NIFTY"), it is returned as-is.
 * Otherwise, the default exchange prefix is applied.
 *
 * @param {string} symbol - Ticker symbol
 * @param {string} [defaultExchange] - Default exchange prefix (defaults to REACT_APP_DEFAULT_EXCHANGE or "NSE")
 * @returns {string} Formatted TradingView symbol (e.g. "NSE:RELIANCE")
 */
export function mapTradingViewSymbol(symbol, defaultExchange) {
  const fallbackExchange = defaultExchange || (typeof process !== 'undefined' && process.env?.REACT_APP_DEFAULT_EXCHANGE) || 'NSE';

  if (!symbol || typeof symbol !== 'string') {
    return `${fallbackExchange}:RELIANCE`;
  }

  const trimmed = symbol.trim().toUpperCase();
  if (trimmed.includes(':')) {
    return trimmed;
  }

  return `${fallbackExchange}:${trimmed}`;
}
