import React, { useEffect, useId, useRef } from 'react';
import { mapTradingViewSymbol } from '../utils/tradingViewSymbol';
import '../styles/TradingViewChart.css';

export default function TradingViewChart({ symbol, theme = 'dark', height = 500 }) {
  const containerRef = useRef(null);
  const reactId = useId();
  const safeId = reactId.replace(/[^a-zA-Z0-9_]/g, '_');
  const containerId = `tv_chart_container_${safeId}`;

  const mappedSymbol = mapTradingViewSymbol(symbol);

  useEffect(() => {
    let scriptElement = null;
    let isMounted = true;

    const initWidget = () => {
      if (!isMounted || !document.getElementById(containerId)) return;

      if (window.TradingView) {
        new window.TradingView.widget({
          autosize: true,
          symbol: mappedSymbol,
          interval: 'D',
          timezone: 'Etc/UTC',
          theme: theme,
          style: '1',
          locale: 'en',
          toolbar_bg: '#f1f3f6',
          enable_publishing: false,
          allow_symbol_change: true,
          container_id: containerId,
        });
      }
    };

    if (containerRef.current) {
      containerRef.current.innerHTML = `<div id="${containerId}" style="height: ${height}px; width: 100%;"></div>`;
    }

    if (window.TradingView) {
      initWidget();
    } else {
      scriptElement = document.createElement('script');
      scriptElement.id = 'tradingview-tv-js';
      scriptElement.src = 'https://s3.tradingview.com/tv.js';
      scriptElement.type = 'text/javascript';
      scriptElement.async = true;
      scriptElement.onload = initWidget;
      document.head.appendChild(scriptElement);
    }

    return () => {
      isMounted = false;
      if (containerRef.current) {
        containerRef.current.innerHTML = '';
      }
    };
  }, [mappedSymbol, theme, height, containerId]);

  return (
    <div className="tradingview-chart-container">
      <div className="tradingview-chart-header">
        <span className="tradingview-chart-title">
          📈 TradingView Advanced Chart ({mappedSymbol})
        </span>
        <span>Interactive Chart</span>
      </div>
      <div className="tradingview-chart-widget-wrapper" style={{ height: `${height}px` }}>
        <div ref={containerRef} style={{ height: '100%', width: '100%' }} />
      </div>
    </div>
  );
}
