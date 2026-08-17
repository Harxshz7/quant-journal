import React from 'react';
import { useTheme } from '../context/ThemeContext';
import { Sun, Moon } from 'lucide-react';

export default function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      type="button"
      className="btn btn-secondary"
      onClick={toggleTheme}
      title={theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme'}
      aria-label="Toggle theme"
    >
      {theme === 'dark' ? <><Sun size={16} className="inline mr-1" /> Light</> : <><Moon size={16} className="inline mr-1" /> Dark</>}
    </button>
  );
}
