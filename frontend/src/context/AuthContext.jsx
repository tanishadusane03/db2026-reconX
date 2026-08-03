// TICKET-ADV112 — AuthContext used by withAuth HOC; JWT persisted in memory
// (refresh path lives in HttpOnly cookie — out of scope for this trainer copy).
import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext({ user: null, login: () => {}, logout: () => {} });

function readStoredUser() {
  const token = sessionStorage.getItem('reconx-token');
  const role = sessionStorage.getItem('reconx-role');
  return token && role ? { token, role } : null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const login = (token, role) => {
    sessionStorage.setItem('reconx-token', token);
    sessionStorage.setItem('reconx-role', role);
    setUser({ token, role });
  };

  const logout = () => {
    sessionStorage.removeItem('reconx-token');
    sessionStorage.removeItem('reconx-role');
    setUser(null);
  };

  // Helper to check if user is admin
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'admin';

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);