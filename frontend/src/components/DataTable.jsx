// TICKET-ADV114 — Compound <DataTable> with Header / Body / Pagination subcomponents.
// Pagination and row data are server-driven (Trades.jsx owns `page` and fetches
// each page from the API), so Body/Pagination take rows/page directly as props
// rather than slicing an internal `data` array.

import React, { createContext, useContext, useState } from 'react';

const DataTableContext = createContext(null);

function useDataTable() {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error('useDataTable must be used inside <DataTable>');
  }

  return context;
}

export default function DataTable({ onSortChange, children }) {
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');

  const value = { sortKey, sortDir, setSortKey, setSortDir, onSortChange };

  return (
    <DataTableContext.Provider value={value}>
      <div className="data-table">
        {children}
      </div>
    </DataTableContext.Provider>
  );
}

function Header({ columns }) {
  const { sortKey, sortDir, setSortKey, setSortDir, onSortChange } = useDataTable();

  function handleSort(key) {
    const nextDir = sortKey === key && sortDir === 'asc' ? 'desc' : 'asc';
    setSortKey(key);
    setSortDir(nextDir);
    onSortChange?.(key, nextDir);
  }

  return (
    <div className="data-table__header" role="row">
      {columns.map((column) => (
        <button
          key={column.key}
          className={`data-table__th ${sortKey === column.key ? 'data-table__th--active' : ''}`}
          onClick={() => handleSort(column.key)}
        >
          {column.label}
          {sortKey === column.key && (sortDir === 'asc' ? ' ▲' : ' ▼')}
        </button>
      ))}
    </div>
  );
}

function Body({ rows = [], render }) {
  return (
    <div className="data-table__body">
      {rows.map((row, index) => (
        <div key={row.id ?? index} className="data-table__row" role="row">
          {render(row)}
        </div>
      ))}
    </div>
  );
}

function Pagination({ page = 0, totalPages = 1, onChange }) {
  return (
    <nav className="data-table__pagination" aria-label="Pagination">
      <button disabled={page === 0} onClick={() => onChange(page - 1)}>‹</button>
      <span>{page + 1} / {totalPages}</span>
      <button disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>›</button>
    </nav>
  );
}

DataTable.Header = Header;
DataTable.Body = Body;
DataTable.Pagination = Pagination;
