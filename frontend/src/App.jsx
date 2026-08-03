// TICKET-ADV122 — Lazy + Suspense for route-based code splitting
import React, { Suspense, lazy } from 'react';
import { Routes, Route, Link, Navigate, useLocation } from 'react-router-dom';
import { withErrorBoundary } from '@components/withErrorBoundary.jsx';
import { useTheme } from '@context/ThemeContext.jsx';
import { useAuth } from '@context/AuthContext.jsx';

import PageSkeleton from '@components/PageSkeleton.jsx';

const Dashboard = lazy(() => import('@pages/Dashboard.jsx'));
const Trades    = lazy(() => import('@pages/Trades.jsx'));
const AddTrade  = lazy(() => import('@pages/AddTrade.jsx'));
const Login     = lazy(() => import('@pages/Login.jsx'));

// ===== RECONX LOGO =====
function Logo() {
  return (
    <Link to="/" className="logo-container">
      <div className="logo-icon">
        <svg viewBox="0 0 24 24">
          <path d="M12 2L2 7l10 5 10-5-10-5z" />
          <path d="M2 17l10 5 10-5" />
          <path d="M2 12l10 5 10-5" />
          <circle cx="12" cy="12" r="2" />
        </svg>
      </div>
      <div className="logo-text">
        <span className="brand">RECONX</span>
        <span className="tagline">Trade Reconciliation</span>
      </div>
    </Link>
  );
}

// ===== THEME TOGGLE =====
function ThemeToggle() {
  const { theme, toggle } = useTheme();
  return (
    <button className="theme-toggle" onClick={toggle} aria-label="Toggle theme">
      <i className={`fas ${theme === 'light' ? 'fa-moon' : 'fa-sun'}`}></i>
      <span>{theme === 'light' ? 'Dark' : 'Light'}</span>
    </button>
  );
}

// ===== FOOTER =====
function Footer() {
  return (
    <footer className="layout-footer">
      <span>© 2026 ReconX. All rights reserved.</span>
      <div style={{ display: 'flex', gap: '16px' }}>
        <a href="#">Privacy</a>
        <a href="#">Terms</a>
        <a href="#">Support</a>
      </div>
    </footer>
  );
}

function App() {
  const location = useLocation();
  const { user, isAdmin } = useAuth();
  const isLoginPage = location.pathname === '/login';

  return (
    <div className="layout">
      <header className="layout__header">
        <Logo />

        {/* Only show navigation if NOT on login page */}
        {!isLoginPage && user && (
          <nav className="layout__nav">
            <Link to="/">Dashboard</Link>
            <Link to="/trades">Trades</Link>
            {/* Only show Add Trade link if admin */}
            {isAdmin && <Link to="/trades/new">Add Trade</Link>}
            <ThemeToggle />
          </nav>
        )}

        {/* Always show theme toggle on login page */}
        {isLoginPage && (
          <div style={{ marginLeft: 'auto' }}>
            <ThemeToggle />
          </div>
        )}
      </header>

      <main className="layout__main">
        <Suspense fallback={<PageSkeleton />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Dashboard />} />
            <Route path="/trades" element={<Trades />} />
            <Route path="/trades/new" element={<AddTrade />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </main>

      <Footer />
    </div>
  );
}

export default withErrorBoundary(App);