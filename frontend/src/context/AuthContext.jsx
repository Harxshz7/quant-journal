import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  getCurrentUserApi,
  loginApi,
  registerApi,
  logoutApi,
  refreshApi,
  updateProfileApi,
  storeAuthTokens,
  clearAuthStorage,
} from '../api/auth';

const AuthContext = createContext(null);

function persistSession(data) {
  storeAuthTokens(data);
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('token');
      const refreshToken = localStorage.getItem('refreshToken');

      if (token) {
        try {
          const userData = await getCurrentUserApi();
          setUser(userData);
        } catch (err) {
          if (refreshToken) {
            try {
              const data = await refreshApi();
              setUser(data.user);
            } catch (refreshErr) {
              console.error('Session expired or invalid:', refreshErr);
              clearAuthStorage();
            }
          } else {
            console.error('Session expired or invalid:', err);
            clearAuthStorage();
          }
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (credentials) => {
    const data = await loginApi(credentials);
    persistSession(data);
    setUser(data.user);
    return data;
  };

  const register = async (userData) => {
    const data = await registerApi(userData);
    persistSession(data);
    setUser(data.user);
    return data;
  };

  const refresh = async () => {
    const data = await refreshApi();
    persistSession(data);
    setUser(data.user);
    return data;
  };

  const updateProfile = async (profile) => {
    const userData = await updateProfileApi(profile);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = async () => {
    await logoutApi();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refresh, updateProfile, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
