// TICKET-ADV114 — Compound <DataTable> with Header / Body / Pagination subcomponents.

import React, {
  createContext,
  useContext,
  useMemo,
  useState
} from 'react';

const DataTableContext = createContext(null);

function useDataTable() {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error('useDataTable must be used inside <DataTable>');
  }

  return context;
}

export default function DataTable({
  data = [],
  pageSize = 10,
  children
}) {
  const [page, setPage] = useState(0);
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');

  const sortedRows = useMemo(() => {
    if (!sortKey) return data;

    return [...data].sort((a, b) => {
      const first = a[sortKey];
      const second = b[sortKey];

      if (first < second) return sortDir === 'asc' ? -1 : 1;
      if (first > second) return sortDir === 'asc' ? 1 : -1;

      return 0;
    });
  }, [data, sortKey, sortDir]);


  const pagedRows = useMemo(() => {
    const start = page * pageSize;

    return sortedRows.slice(start, start + pageSize);

  }, [sortedRows, page, pageSize]);


  const value = useMemo(() => ({
    rows: pagedRows,
    totalRows: data.length,

    page,
    pageSize,
    setPage,

    sortKey,
    sortDir,
    setSortKey,
    setSortDir

  }), [
    pagedRows,
    data.length,
    page,
    pageSize,
    sortKey,
    sortDir
  ]);


  return (
    <DataTableContext.Provider value={value}>
      <div className="data-table">
        {children}
      </div>
    </DataTableContext.Provider>
  );
}


function Header({ columns }) {

  const {
    sortKey,
    sortDir,
    setSortKey,
    setSortDir
  } = useDataTable();


  function handleSort(key) {

    if (sortKey === key) {
      setSortDir(
        sortDir === 'asc'
          ? 'desc'
          : 'asc'
      );
    } else {
      setSortKey(key);
      setSortDir('asc');
    }

  }


  return (
    <div className="data-table__header" role="row">

      {columns.map((column) => (

        <button
          key={column.key}
          className={
            `data-table__th ${
              sortKey === column.key
                ? 'data-table__th--active'
                : ''
            }`
          }
          onClick={() => handleSort(column.key)}
        >

          {column.label}

          {sortKey === column.key &&
            (sortDir === 'asc' ? ' ▲' : ' ▼')
          }

        </button>

      ))}

    </div>
  );
}



function Body({ renderRow }) {

  const { rows } = useDataTable();


  return (
    <div className="data-table__body">

      {rows.map((row, index) => (

        <div
          key={row.id ?? index}
          className="data-table__row"
          role="row"
        >

          {renderRow(row)}

        </div>

      ))}

    </div>
  );
}



function Pagination() {

  const {
    page,
    pageSize,
    totalRows,
    setPage
  } = useDataTable();


  const totalPages =
    Math.ceil(totalRows / pageSize);


  return (
    <nav
      className="data-table__pagination"
      aria-label="Pagination"
    >

      <button
        disabled={page === 0}
        onClick={() => setPage(page - 1)}
      >
        ‹
      </button>


      <span>
        {page + 1} / {totalPages}
      </span>


      <button
        disabled={page >= totalPages - 1}
        onClick={() => setPage(page + 1)}
      >
        ›
      </button>


    </nav>
  );
}


DataTable.Header = Header;
DataTable.Body = Body;
DataTable.Pagination = Pagination;