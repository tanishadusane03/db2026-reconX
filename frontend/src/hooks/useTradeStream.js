
// useTradeStream() — seeds with existing trades, then keeps them live via SSE.
import { useEffect, useState } from 'react';
import { api } from '@services/apiService.js';

function upsert(prev, trade) {
  const idx = prev.findIndex((t) => t.id === trade.id);
  if (idx === -1) return [trade, ...prev].slice(0, 200);
  const next = [...prev];
  next[idx] = trade;
  return next;
}

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    let cancelled = false;

    // Without this, the Dashboard only ever shows trades created *after* it
    // was opened — a fresh page load always started at zero. Seed with what
    // already exists, then let SSE keep it live.
    api.listTrades('?size=200')
      .then((res) => { if (!cancelled) setTrades(res.items ?? []); })
      .catch(() => { /* SSE below still works even if this seed fails */ });

    // EventSource cannot set an Authorization header, so the JWT rides along
    // as a query param; the backend's JwtAuthenticationFilter accepts either.
    const token = sessionStorage.getItem('reconx-token');
    if (!token) {
      setConnected(false);
      return () => { cancelled = true; };
    }
    const authedUrl = `${url}?token=${encodeURIComponent(token)}`;
    const sse = new EventSource(authedUrl);
    sse.onopen = () => setConnected(true);
    sse.onmessage = (e) => {
      try {
        const t = JSON.parse(e.data);
        // upsert, not prepend: a status-change event re-broadcasts the same
        // trade id, and treating it as a new row would double-count it in
        // the Dashboard's portfolio-value / matched / breaks totals.
        setTrades((prev) => upsert(prev, t));
      } catch { /* ignore malformed payload */ }
    };
    sse.onerror = () => setConnected(false);
    return () => { cancelled = true; sse.close(); };
  }, [url]);

  return { trades, isConnected };
}