// TICKET-ADV072 — Login page exchanging email/password for a JWT.
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@context/AuthContext.jsx';
import { api } from '@services/apiService.js';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@db.com');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState(null);

  async function submit(e) {
    e.preventDefault();
    setError(null);
    try {
      const { token, role } = await api.login(email, password);
      login(token, role);
      navigate('/');
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '70vh'
    }}>
      <div style={{
        background: 'var(--color-surface)',
        borderRadius: 'var(--radius-lg)',
        border: '1px solid var(--color-border)',
        padding: '48px 40px',
        maxWidth: '420px',
        width: '100%',
        boxShadow: 'var(--shadow-card)'
      }}>
        {/* Logo in login */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '12px',
            marginBottom: '8px'
          }}>
            <div style={{
              width: '44px',
              height: '44px',
              background: 'var(--color-primary)',
              borderRadius: 'var(--radius)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <svg viewBox="0 0 24 24" style={{ width: '28px', height: '28px' }}>
                <path d="M12 2L2 7l10 5 10-5-10-5z" fill="none" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M2 17l10 5 10-5" fill="none" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M2 12l10 5 10-5" fill="none" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <circle cx="12" cy="12" r="2" fill="#ffffff" />
              </svg>
            </div>
            <div style={{ textAlign: 'left' }}>
              <div style={{ fontWeight: '700', fontSize: '1.5rem', color: 'var(--color-text)' }}>RECONX</div>
              <div style={{ fontSize: '0.55rem', fontWeight: '500', textTransform: 'uppercase', letterSpacing: '0.12em', color: 'var(--color-text-muted)' }}>Trade Reconciliation</div>
            </div>
          </div>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.95rem', marginTop: '4px' }}>
            Sign in to access your dashboard
          </p>
        </div>

        <form onSubmit={submit}>
          <div style={{ marginBottom: '16px' }}>
            <label style={{ 
              display: 'block', 
              fontWeight: '500', 
              fontSize: '0.85rem', 
              color: 'var(--color-text-secondary)',
              marginBottom: '4px'
            }}>
              Email
            </label>
            <input 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              type="email" 
              required 
              placeholder="admin@db.com"
              style={{
                width: '100%',
                padding: '10px 14px',
                border: '1px solid var(--color-border)',
                borderRadius: 'var(--radius)',
                background: 'var(--color-bg)',
                color: 'var(--color-text)',
                fontSize: '0.95rem',
                transition: 'var(--transition)',
                fontFamily: 'var(--font-base)'
              }}
            />
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label style={{ 
              display: 'block', 
              fontWeight: '500', 
              fontSize: '0.85rem', 
              color: 'var(--color-text-secondary)',
              marginBottom: '4px'
            }}>
              Password
            </label>
            <input 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              type="password" 
              required 
              placeholder="••••••••"
              style={{
                width: '100%',
                padding: '10px 14px',
                border: '1px solid var(--color-border)',
                borderRadius: 'var(--radius)',
                background: 'var(--color-bg)',
                color: 'var(--color-text)',
                fontSize: '0.95rem',
                transition: 'var(--transition)',
                fontFamily: 'var(--font-base)'
              }}
            />
          </div>

          {error && (
            <div role="alert" style={{ 
              color: 'var(--color-danger)', 
              fontSize: '0.85rem',
              padding: '8px 12px',
              background: 'rgba(225, 112, 85, 0.08)',
              borderRadius: 'var(--radius)',
              marginBottom: '16px'
            }}>
              {error}
            </div>
          )}

          <button 
            type="submit" 
            style={{
              width: '100%',
              padding: '12px',
              border: 'none',
              background: 'var(--color-accent)',
              color: '#ffffff',
              borderRadius: 'var(--radius)',
              cursor: 'pointer',
              fontWeight: '600',
              fontSize: '0.95rem',
              transition: 'var(--transition)',
              fontFamily: 'var(--font-base)'
            }}
          >
            <i className="fas fa-sign-in-alt" style={{ marginRight: '8px' }}></i>
            Sign In
          </button>
        </form>
      </div>
    </div>
  );
}