import React from 'react';
import { useAuth } from '../context/AuthContext';

export default function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="dashboard-layout">
      <header className="dashboard-navbar">
        <div className="brand-logo">
          <span className="logo-icon">⚡</span> Quant Journal
        </div>
        <div className="navbar-actions">
          <span className="user-badge">{user?.email}</span>
          <button onClick={logout} className="btn btn-secondary">
            Logout
          </button>
        </div>
      </header>

      <main className="dashboard-content">
        <div className="welcome-card">
          <h1>Welcome, {user?.fullName || 'User'}</h1>
          <p>Your authentication token is active and secure.</p>
        </div>
      </main>
    </div>
  );
}
