import React, { useState } from 'react';
import { NavLink, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useAccounts } from '../context/AccountContext';
import ThemeToggle from './ThemeToggle';

const NAV_LINKS = [
  { key: 'dashboard', to: '/', label: 'Dashboard' },
  { key: 'journal', to: '/journal', label: 'Journal' },
  { key: 'trades', to: '/trades', label: 'Trades' },
  { key: 'stats', to: '/stats', label: 'Stats' },
  { key: 'lessons', to: '/lessons', label: 'Lessons' },
  { key: 'calculator', to: '/calculator', label: 'Calculator' },
  { key: 'import', to: '/import', label: 'Import' },
  { key: 'profile', to: '/profile', label: 'Profile' },
];

/**
 * Shared top navigation. Collapses to a hamburger drawer below 768px.
 * @param {string} active - Key of the active page (for styling)
 * @param {React.ReactNode} [children] - Extra action buttons rendered in the nav (and drawer on mobile)
 */
export default function NavBar({ active, children }) {
  const [open, setOpen] = useState(false);
  const { logout } = useAuth();
  const { accounts, activeAccountId, setActiveAccount } = useAccounts();

  const handleAccountChange = (e) => {
    setActiveAccount(e.target.value || null);
  };

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">Quant Journal</Link>

      <nav className={`navbar-links ${open ? 'open' : ''}`}>
        {NAV_LINKS.map((link) => (
          <NavLink
            key={link.key}
            to={link.to}
            end={link.to === '/'}
            className={({ isActive }) =>
              `navbar-link ${isActive || active === link.key ? 'is-active' : ''}`
            }
            onClick={() => setOpen(false)}
          >
            {link.label}
          </NavLink>
        ))}
        {children}
      </nav>

      <div className="navbar-actions">
        <select
          className="account-switcher"
          value={activeAccountId || ''}
          onChange={handleAccountChange}
          aria-label="Select trading account"
        >
          <option value="">All accounts</option>
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>{a.name}{a.isDefault ? ' (default)' : ''}</option>
          ))}
        </select>
        <ThemeToggle />
        <button type="button" className="btn btn-secondary btn-sm" onClick={logout}>
          Logout
        </button>
        <button
          type="button"
          className="navbar-hamburger"
          aria-label="Toggle navigation menu"
          aria-expanded={open}
          onClick={() => setOpen((current) => !current)}
        >
          {open ? '✕' : '☰'}
        </button>
      </div>
    </header>
  );
}
