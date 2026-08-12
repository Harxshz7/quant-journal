import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { changePasswordApi } from '../api/auth';

export default function Profile() {
  const { user, logout, updateProfile } = useAuth();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [profileError, setProfileError] = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileLoading, setProfileLoading] = useState(false);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setEmail(user.email || '');
    }
  }, [user]);

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileError('');
    setProfileSuccess('');

    if (!fullName || !email) {
      setProfileError('Please fill in all fields.');
      return;
    }

    setProfileLoading(true);
    try {
      await updateProfile({ fullName, email });
      setProfileSuccess('Profile updated successfully.');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update profile.';
      setProfileError(msg);
    } finally {
      setProfileLoading(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');

    if (!currentPassword || !newPassword) {
      setPasswordError('Please fill in all fields.');
      return;
    }

    if (newPassword.length < 6) {
      setPasswordError('New password must be at least 6 characters long.');
      return;
    }

    setPasswordLoading(true);
    try {
      await changePasswordApi({ currentPassword, newPassword });
      setCurrentPassword('');
      setNewPassword('');
      setPasswordSuccess('Password changed successfully.');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to change password.';
      setPasswordError(msg);
    } finally {
      setPasswordLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="back-link">
        <Link to="/">&larr; Back to Entries</Link>
      </div>

      <header className="header">
        <div>
          <h1>Profile</h1>
          {user && (
            <p className="muted" style={{ margin: '0.25rem 0 0 0', fontSize: '0.9rem' }}>
              Manage your account settings
            </p>
          )}
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <button className="btn btn-secondary" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <div className="auth-card" style={{ marginBottom: '1.75rem' }}>
        <div className="auth-header">
          <h2>Account Details</h2>
          <p>Update your name and email address</p>
        </div>

        {profileError && <div className="alert alert-error">{profileError}</div>}
        {profileSuccess && (
          <div className="alert alert-error" style={{ borderColor: 'var(--pnl-positive)', color: 'var(--pnl-positive)' }}>
            {profileSuccess}
          </div>
        )}

        <form onSubmit={handleProfileSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="fullName">Full Name</label>
            <input
              id="fullName"
              type="text"
              placeholder="John Doe"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              placeholder="trader@quantjournal.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={profileLoading}>
            {profileLoading ? 'Saving...' : 'Save Profile'}
          </button>
        </form>
      </div>

      <div className="auth-card">
        <div className="auth-header">
          <h2>Change Password</h2>
          <p>Update your account password</p>
        </div>

        {passwordError && <div className="alert alert-error">{passwordError}</div>}
        {passwordSuccess && (
          <div className="alert alert-error" style={{ borderColor: 'var(--pnl-positive)', color: 'var(--pnl-positive)' }}>
            {passwordSuccess}
          </div>
        )}

        <form onSubmit={handlePasswordSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="currentPassword">Current Password</label>
            <input
              id="currentPassword"
              type="password"
              placeholder="••••••••"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newPassword">New Password</label>
            <input
              id="newPassword"
              type="password"
              placeholder="••••••••"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={passwordLoading}>
            {passwordLoading ? 'Updating...' : 'Change Password'}
          </button>
        </form>
      </div>
    </div>
  );
}
