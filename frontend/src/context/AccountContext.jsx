import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { getAccounts } from '../api/accounts';
import { useAuth } from './AuthContext';

const STORAGE_KEY = 'activeAccountId';
const AccountContext = createContext(null);

export function AccountProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeAccountId, setActiveAccountIdState] = useState(() => localStorage.getItem(STORAGE_KEY) || null);

  const refreshAccounts = useCallback(async () => {
    try {
      setLoading(true);
      const data = await getAccounts();
      const list = Array.isArray(data) ? data : [];
      setAccounts(list);
      setActiveAccountIdState((current) => {
        const stillExists = !current || list.some((a) => a.id === current);
        if (!stillExists) {
          localStorage.removeItem(STORAGE_KEY);
          return null;
        }
        return current;
      });
    } catch (err) {
      console.error('Failed to load accounts:', err);
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      refreshAccounts();
    }
  }, [isAuthenticated, refreshAccounts]);

  const setActiveAccount = (id) => {
    setActiveAccountIdState(id);
    if (id) {
      localStorage.setItem(STORAGE_KEY, id);
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  const activeAccount = accounts.find((a) => a.id === activeAccountId) || null;

  return (
    <AccountContext.Provider
      value={{ accounts, activeAccountId, activeAccount, setActiveAccount, refreshAccounts, loading }}
    >
      {children}
    </AccountContext.Provider>
  );
}

export function useAccounts() {
  const context = useContext(AccountContext);
  if (!context) {
    throw new Error('useAccounts must be used within an AccountProvider');
  }
  return context;
}