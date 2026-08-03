// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
import React, { useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';

function Trades() {
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ items: [], totalPages: 0 });

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams({ page: String(page), size: '10' });
    if (debounced) params.set('status', debounced);

    api.listTrades(`?${params.toString()}`)
      .then((res) => { if (!cancelled) setData(res); })
      .catch(() => { if (!cancelled) setData({ items: [], totalPages: 0 }); });

    return () => { cancelled = true; };
  }, [page, debounced]);

  // Status options for filter dropdown
  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'MATCHED', label: 'Matched' },
    { value: 'UNMATCHED', label: 'Unmatched' },
    { value: 'DISPUTED', label: 'Disputed' },
  ];

  // Get status color for badges
  const getStatusColor = (status) => {
    const colors = {
      'PENDING': '#74b9ff',
      'MATCHED': '#00b894',
      'UNMATCHED': '#fdcb6e',
      'DISPUTED': '#e17055'
    };
    return colors[status] || '#a0aec0';
  };

  return (
    <section>
      {/* Header with stats */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '24px',
        flexWrap: 'wrap',
        gap: '12px'
      }}>
        <div>
          <h1 style={{ 
            fontSize: '1.8rem', 
            fontWeight: '700',
            margin: 0,
            color: 'var(--color-text)'
          }}>
            Trades
          </h1>
          <p style={{ 
            margin: '4px 0 0 0',
            color: 'var(--color-text-secondary)',
            fontSize: '0.9rem'
          }}>
            {data.items.length} trades • Page {page + 1} of {Math.max(1, data.totalPages)}
          </p>
        </div>
        
        {/* Filter dropdown */}
        <div style={{ 
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          <label style={{
            fontSize: '0.85rem',
            color: 'var(--color-text-secondary)',
            fontWeight: '500'
          }}>
            <i className="fas fa-filter" style={{ marginRight: '6px' }}></i>
            Filter by status
          </label>
          <select
            style={{
              padding: '8px 36px 8px 16px',
              borderRadius: 'var(--radius)',
              border: '1px solid var(--color-border)',
              background: 'var(--color-surface)',
              color: 'var(--color-text)',
              fontSize: '0.9rem',
              transition: 'var(--transition)',
              fontFamily: 'var(--font-base)',
              minWidth: '180px',
              cursor: 'pointer',
              appearance: 'none',
              WebkitAppearance: 'none',
              backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364748b' d='M6 8L1 3h10z'/%3E%3C/svg%3E")`,
              backgroundRepeat: 'no-repeat',
              backgroundPosition: 'right 12px center',
              boxShadow: 'var(--shadow-card)'
            }}
            aria-label="Filter by status"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          >
            {statusOptions.map(opt => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Enhanced Data Table - 5 Columns (NO NOTES) */}
      <div style={{
        background: 'var(--color-surface)',
        borderRadius: 'var(--radius-lg)',
        border: '1px solid var(--color-border)',
        overflow: 'hidden',
        boxShadow: 'var(--shadow-card)'
      }}>
        {/* Table Header - 5 columns */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '2fr 1.5fr 1fr 1.5fr 1.5fr',
          background: 'var(--color-bg-secondary)',
          padding: '14px 20px',
          borderBottom: '1px solid var(--color-border)',
          fontWeight: '600',
          fontSize: '0.7rem',
          textTransform: 'uppercase',
          letterSpacing: '0.06em',
          color: 'var(--color-text-muted)'
        }}>
          <span>Reference</span>
          <span>Symbol</span>
          <span>Quantity</span>
          <span>Price</span>
          <span>Status</span>
        </div>

        {/* Table Body */}
        {data.items.length === 0 ? (
          <div style={{
            padding: '48px 20px',
            textAlign: 'center',
            color: 'var(--color-text-secondary)'
          }}>
            <i className="fas fa-inbox" style={{ fontSize: '2rem', opacity: '0.3', display: 'block', marginBottom: '12px' }}></i>
            <p style={{ margin: 0 }}>No trades found</p>
          </div>
        ) : (
          data.items.map((trade, index) => (
            <div
              key={index}
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 1.5fr 1fr 1.5fr 1.5fr',
                padding: '12px 20px',
                borderBottom: index < data.items.length - 1 ? '1px solid var(--color-border)' : 'none',
                transition: 'var(--transition)',
                alignItems: 'center',
                background: index % 2 === 0 ? 'transparent' : 'var(--color-surface-hover)'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = 'var(--color-surface-hover)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = index % 2 === 0 ? 'transparent' : 'var(--color-surface-hover)';
              }}
            >
              <span style={{ fontWeight: '500', fontSize: '0.9rem', color: 'var(--color-text)' }}>
                {trade.tradeRef}
              </span>
              <span style={{ color: 'var(--color-text-secondary)' }}>
                {trade.instrumentSymbol}
              </span>
              <span style={{ color: 'var(--color-text-secondary)' }}>
                {trade.quantity}
              </span>
              <span style={{ fontWeight: '500', color: 'var(--color-text)' }}>
                ${trade.price}
              </span>
              <span>
                <span style={{
                  display: 'inline-block',
                  padding: '4px 14px',
                  borderRadius: '20px',
                  fontSize: '0.7rem',
                  fontWeight: '600',
                  textTransform: 'uppercase',
                  letterSpacing: '0.04em',
                  background: `${getStatusColor(trade.status)}20`,
                  color: getStatusColor(trade.status),
                  border: `1px solid ${getStatusColor(trade.status)}30`
                }}>
                  <span style={{
                    display: 'inline-block',
                    width: '6px',
                    height: '6px',
                    borderRadius: '50%',
                    background: getStatusColor(trade.status),
                    marginRight: '6px',
                    verticalAlign: 'middle'
                  }}></span>
                  {trade.status}
                </span>
              </span>
            </div>
          ))
        )}

        {/* Pagination */}
        <div style={{
          display: 'flex',
          gap: '12px',
          padding: '14px 20px',
          justifyContent: 'center',
          alignItems: 'center',
          background: 'var(--color-bg-secondary)',
          borderTop: '1px solid var(--color-border)'
        }}>
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            style={{
              padding: '6px 16px',
              borderRadius: 'var(--radius)',
              border: '1px solid var(--color-border)',
              background: 'var(--color-surface)',
              color: 'var(--color-text)',
              cursor: page === 0 ? 'not-allowed' : 'pointer',
              opacity: page === 0 ? '0.4' : '1',
              transition: 'var(--transition)',
              fontFamily: 'var(--font-base)',
              fontSize: '0.85rem',
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}
          >
            <i className="fas fa-chevron-left" style={{ fontSize: '0.7rem' }}></i>
            Previous
          </button>
          
          <span style={{
            color: 'var(--color-text-secondary)',
            fontSize: '0.85rem',
            fontWeight: '500'
          }}>
            Page {page + 1} of {Math.max(1, data.totalPages)}
          </span>
          
          <button
            onClick={() => setPage(Math.min(Math.max(1, data.totalPages) - 1, page + 1))}
            disabled={page + 1 >= data.totalPages}
            style={{
              padding: '6px 16px',
              borderRadius: 'var(--radius)',
              border: '1px solid var(--color-border)',
              background: 'var(--color-surface)',
              color: 'var(--color-text)',
              cursor: page + 1 >= data.totalPages ? 'not-allowed' : 'pointer',
              opacity: page + 1 >= data.totalPages ? '0.4' : '1',
              transition: 'var(--transition)',
              fontFamily: 'var(--font-base)',
              fontSize: '0.85rem',
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}
          >
            Next
            <i className="fas fa-chevron-right" style={{ fontSize: '0.7rem' }}></i>
          </button>
        </div>
      </div>
    </section>
  );
}

export default withAuth(Trades);