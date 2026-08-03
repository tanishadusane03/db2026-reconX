import { useEffect, useState } from 'react';
const MAX_BUFFER = 200;
export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);
  useEffect(() => {
    const sse = new EventSource(url);
    sse.onopen = () => setConnected(true);
    sse.onerror = () => setConnected(false);
    sse.onmessage = (event) => { try { const trade = JSON.parse(event.data); setTrades((previous) => [trade, ...previous].slice(0, MAX_BUFFER)); } catch { /* ignore malformed payload */ } };
    return () => sse.close();
  }, [url]);
  return { trades, isConnected };
}
