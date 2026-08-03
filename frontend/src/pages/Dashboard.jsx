// TICKET-ADV120 — useMemo for portfolio-value calc.
// TICKET-ADV116 — useTradeStream live feed.
import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';

function StatCard({ label, value, icon, iconColor }) {
  return (
    <article className="stat-card" style={{ position: 'relative' }}>
      <div style={{
        display: 'flex',
        alignItems: 'flex-start',
        justifyContent: 'space-between'
      }}>
        <div style={{ flex: 1 }}>
          <h3 style={{
            margin: 0,
            fontSize: '0.75rem',
            textTransform: 'uppercase',
            letterSpacing: '0.06em',
            color: 'var(--color-text-muted)',
            fontWeight: '600',
            marginBottom: '6px'
          }}>
            {label}
          </h3>
          <p style={{
            margin: 0,
            fontSize: '1.8rem',
            fontWeight: '700',
            color: 'var(--color-text)',
            letterSpacing: '-0.02em',
            lineHeight: '1.2'
          }}>
            {value}
          </p>
        </div>
        <div style={{
          width: '44px',
          height: '44px',
          borderRadius: 'var(--radius)',
          background: `${iconColor || 'var(--color-accent)'}15`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}>
          <i className={`fas fa-${icon}`} style={{
            fontSize: '1.4rem',
            color: iconColor || 'var(--color-accent)',
            opacity: 0.9
          }}></i>
        </div>
      </div>
    </article>
  );
}

function Dashboard() {
  const { trades, isConnected } = useTradeStream();

  const portfolioValue = useMemo(
    () => trades.reduce((sum, t) => sum + ((t.quantity * t.price) || 0), 0),
    [trades]
  );

  const matched = useMemo(
    () => trades.filter(t => t.status === 'MATCHED').length,
    [trades]
  );

  const breaks = useMemo(
    () => trades.filter(
      t => t.status === 'UNMATCHED' || t.status === 'DISPUTED'
    ).length,
    [trades]
  );

  // Mock trend data for visual effect
  const trends = useMemo(() => ({
    portfolio: (Math.random() * 10 + 2).toFixed(1),
    matched: (Math.random() * 8 + 1).toFixed(1),
  }), [trades]);

  return (
    <section>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '8px' 
      }}>
        <h1 style={{ 
          fontSize: '1.8rem', 
          fontWeight: '700',
          margin: 0,
          color: 'var(--color-text)'
        }}>
          Dashboard
        </h1>
        <div className="sse-status">
          <span className={`dot ${isConnected ? 'on' : 'off'}`}></span>
          {isConnected ? 'Connected' : 'Disconnected'}
        </div>
      </div>
      
      <div className="stat-grid">
        <StatCard 
          label="Portfolio Value" 
          value={`$${portfolioValue.toLocaleString()}`}
          icon="coins"
          iconColor="#f5a623"
        />
        <StatCard 
          label="Total Trades" 
          value={trades.length}
          icon="wave-square"
          iconColor="#4facfe"
        />
        <StatCard 
          label="Matched" 
          value={matched}
          icon="check-circle"
          iconColor="#00b894"
        />
        <StatCard 
          label="Open Breaks" 
          value={breaks}
          icon="exclamation-triangle"
          iconColor="#e17055"
        />
      </div>
    </section>
  );
}

export default withAuth(Dashboard);