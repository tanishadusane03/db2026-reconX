// TICKET-ADV124 — ThemeProvider: context flips data-theme; CSS owns colours.
import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback
} from 'react';

const STORAGE_KEY = 'reconx-theme';

function initialTheme() {
  if (typeof window === 'undefined') {
    return 'light';
  }

  const storedTheme = localStorage.getItem(STORAGE_KEY);

  if (storedTheme) {
    return storedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches
    ? 'dark'
    : 'light';
}

const ThemeContext = createContext(null);

export function ThemeProvider({ children }) {
  // TODO(TICKET-ADV124): lazy-init from localStorage('reconx-theme') — fall back
  //                     to 'light' if nothing is stored.
  const [theme, setTheme] = useState(initialTheme);

  // TODO(TICKET-ADV124): useEffect that:
  //                     1. sets document.documentElement.dataset.theme = theme
  //                     2. persists `theme` to localStorage on every change.
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  const toggle = useCallback(() => {
  setTheme(prev => prev === 'light' ? 'dark' : 'light');
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, setTheme, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);

  if (context === null) {
    throw new Error('useTheme must be used inside ThemeProvider');
  }

  return context;
}
