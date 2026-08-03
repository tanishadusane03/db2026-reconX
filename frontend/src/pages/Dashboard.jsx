import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
function StatCard({ label, value }) { return <article className="stat-card"><h3>{label}</h3><p>{value}</p></article>; }
function Dashboard({ trades: tradesProp }) {
  const stream = useTradeStream(); const trades = tradesProp ?? stream.trades; const isConnected = tradesProp ? true : stream.isConnected;
  const portfolioValue = useMemo(() => trades.reduce((sum, trade) => sum + (Number(trade.quantity) * Number(trade.price) || 0), 0), [trades]);
  const { matched, unmatched, breaks } = useMemo(() => trades.reduce((summary, trade) => { if (trade.status === 'MATCHED') summary.matched += 1; else if (trade.status === 'UNMATCHED') { summary.unmatched += 1; summary.breaks += 1; } else if (trade.status === 'DISPUTED') summary.breaks += 1; return summary; }, { matched: 0, unmatched: 0, breaks: 0 }), [trades]);
  return <section><h2>Dashboard</h2><div className="stat-grid"><StatCard label="Portfolio value (USD)" value={portfolioValue.toLocaleString()} /><StatCard label="Trades streamed" value={trades.length} /><StatCard label="Matched trades" value={matched} /><StatCard label="Unmatched trades" value={unmatched} /><StatCard label="Open breaks" value={breaks} /></div><div role="status" aria-live="polite">SSE: {isConnected ? 'connected' : 'disconnected'}</div></section>;
}
export default withAuth(Dashboard);
