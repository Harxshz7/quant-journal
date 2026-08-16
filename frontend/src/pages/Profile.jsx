import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { changePasswordApi, getWebhookUrl, regenerateWebhookUrl } from '../api/auth';
import { getChecklistTemplates, createChecklistTemplate, updateChecklistTemplate, deactivateChecklistTemplate } from '../api/checklist';

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

  const [webhookUrl, setWebhookUrl] = useState('');
  const [webhookError, setWebhookError] = useState('');
  const [webhookLoading, setWebhookLoading] = useState(false);
  const [copySuccess, setCopySuccess] = useState(false);
  const [regenerating, setRegenerating] = useState(false);

  const [templates, setTemplates] = useState([]);
  const [templatesLoading, setTemplatesLoading] = useState(true);
  const [newTemplateText, setNewTemplateText] = useState('');
  const [addingTemplate, setAddingTemplate] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editingText, setEditingText] = useState('');

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setEmail(user.email || '');
      fetchWebhookUrl();
      fetchTemplates();
    }
  }, [user]);

  const fetchWebhookUrl = async () => {
    try {
      setWebhookLoading(true);
      setWebhookError('');
      const res = await getWebhookUrl();
      setWebhookUrl(res.webhookUrl || '');
    } catch (err) {
      setWebhookError(err.response?.data?.message || 'Failed to load webhook URL.');
    } finally {
      setWebhookLoading(false);
    }
  };

  const fetchTemplates = async () => {
    try {
      setTemplatesLoading(true);
      const data = await getChecklistTemplates();
      setTemplates(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load checklist templates:', err);
    } finally {
      setTemplatesLoading(false);
    }
  };

  const handleCopyWebhook = () => {
    if (!webhookUrl) return;
    navigator.clipboard.writeText(webhookUrl)
      .then(() => { setCopySuccess(true); setTimeout(() => setCopySuccess(false), 2000); })
      .catch(() => { setWebhookError('Could not copy.'); });
  };

  const handleRegenerateWebhook = async () => {
    if (!window.confirm('Regenerate webhook URL? Previous URL will stop working.')) return;
    try {
      setRegenerating(true);
      const res = await regenerateWebhookUrl();
      setWebhookUrl(res.webhookUrl || '');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to regenerate');
    } finally {
      setRegenerating(false);
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileError('');
    setProfileSuccess('');
    if (!fullName || !email) { setProfileError('Please fill in all fields.'); return; }
    setProfileLoading(true);
    try {
      await updateProfile({ fullName, email });
      setProfileSuccess('Profile updated successfully.');
    } catch (err) {
      setProfileError(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setProfileLoading(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');
    if (!currentPassword || !newPassword) { setPasswordError('Please fill in all fields.'); return; }
    if (newPassword.length < 6) { setPasswordError('New password must be at least 6 characters.'); return; }
    setPasswordLoading(true);
    try {
      await changePasswordApi({ currentPassword, newPassword });
      setCurrentPassword('');
      setNewPassword('');
      setPasswordSuccess('Password changed successfully.');
    } catch (err) {
      setPasswordError(err.response?.data?.message || 'Failed to change password.');
    } finally {
      setPasswordLoading(false);
    }
  };

  const handleAddTemplate = async (e) => {
    e.preventDefault();
    if (!newTemplateText.trim()) return;
    try {
      setAddingTemplate(true);
      await createChecklistTemplate({ text: newTemplateText.trim() });
      setNewTemplateText('');
      await fetchTemplates();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add template');
    } finally {
      setAddingTemplate(false);
    }
  };

  const handleUpdateTemplate = async (id) => {
    if (!editingText.trim()) return;
    try {
      await updateChecklistTemplate(id, { text: editingText.trim() });
      setEditingId(null);
      setEditingText('');
      await fetchTemplates();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update template');
    }
  };

  const handleDeactivateTemplate = async (id) => {
    try {
      await deactivateChecklistTemplate(id);
      await fetchTemplates();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to deactivate template');
    }
  };

  return (
    <div className="container">
      <div className="back-link">
        <Link to="/">&larr; Back to Dashboard</Link>
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
          <button className="btn btn-secondary" onClick={logout}>Logout</button>
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
            <input id="fullName" type="text" placeholder="John Doe" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input id="email" type="email" placeholder="trader@quantjournal.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary" disabled={profileLoading}>
            {profileLoading ? 'Saving...' : 'Save Profile'}
          </button>
        </form>
      </div>

      <div className="auth-card" style={{ marginBottom: '1.75rem' }}>
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
            <input id="currentPassword" type="password" placeholder="••••••••" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="newPassword">New Password</label>
            <input id="newPassword" type="password" placeholder="••••••••" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary" disabled={passwordLoading}>
            {passwordLoading ? 'Updating...' : 'Change Password'}
          </button>
        </form>
      </div>

      <div className="auth-card" style={{ marginBottom: '1.75rem' }}>
        <div className="auth-header">
          <h2>Trade Checklist Templates</h2>
          <p>Reusable pre-trade checklist items. Add, edit, or deactivate items.</p>
        </div>

        <form onSubmit={handleAddTemplate} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
          <input
            type="text"
            placeholder="e.g. Confirmed trend?"
            value={newTemplateText}
            onChange={(e) => setNewTemplateText(e.target.value)}
            style={{ flex: 1 }}
          />
          <button type="submit" className="btn btn-primary btn-sm" disabled={addingTemplate || !newTemplateText.trim()}>
            {addingTemplate ? '...' : 'Add'}
          </button>
        </form>

        {templatesLoading ? (
          <p className="loading">Loading templates...</p>
        ) : templates.length === 0 ? (
          <p className="muted" style={{ fontSize: '0.9rem' }}>No checklist templates yet. Add one above.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {templates.map((t) => (
              <div
                key={t.id}
                style={{
                  display: 'flex', alignItems: 'center', gap: '0.5rem',
                  padding: '0.6rem 0.75rem', borderRadius: 10,
                  background: t.active ? 'var(--surface)' : 'rgba(255,255,255,0.02)',
                  boxShadow: t.active ? 'var(--shadow-elev)' : 'none',
                  opacity: t.active ? 1 : 0.5,
                }}
              >
                {editingId === t.id ? (
                  <>
                    <input
                      type="text"
                      value={editingText}
                      onChange={(e) => setEditingText(e.target.value)}
                      onKeyDown={(e) => { if (e.key === 'Enter') handleUpdateTemplate(t.id); if (e.key === 'Escape') { setEditingId(null); setEditingText(''); } }}
                      style={{ flex: 1, padding: '0.3rem 0.5rem', fontSize: '0.85rem' }}
                      autoFocus
                    />
                    <button className="btn btn-sm btn-primary" onClick={() => handleUpdateTemplate(t.id)}>Save</button>
                    <button className="btn btn-sm btn-secondary" onClick={() => { setEditingId(null); setEditingText(''); }}>Cancel</button>
                  </>
                ) : (
                  <>
                    <span style={{ flex: 1, fontSize: '0.9rem', color: t.active ? 'var(--text)' : 'var(--muted)' }}>
                      {t.text}
                    </span>
                    <button
                      className="btn btn-sm btn-secondary"
                      onClick={() => { setEditingId(t.id); setEditingText(t.text); }}
                    >
                      Edit
                    </button>
                    {t.active && (
                      <button
                        className="btn btn-sm btn-secondary"
                        style={{ color: 'var(--pnl-negative)' }}
                        onClick={() => handleDeactivateTemplate(t.id)}
                      >
                        Deactivate
                      </button>
                    )}
                  </>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="auth-card">
        <div className="auth-header">
          <h2>Webhook & Automation Settings</h2>
          <p>Personal endpoint for future automated trade logging</p>
        </div>
        <div className="alert" style={{ background: 'rgba(56, 189, 248, 0.1)', borderColor: 'rgba(56, 189, 248, 0.3)', color: '#38bdf8', fontSize: '0.85rem' }}>
          <strong>Note:</strong> For future broker/alert automation — receiver is not active yet.
        </div>
        {webhookError && <div className="alert alert-error">{webhookError}</div>}
        <div className="form-group">
          <label htmlFor="webhookUrl">Webhook URL</label>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <input id="webhookUrl" type="text" readOnly value={webhookLoading ? 'Loading...' : webhookUrl} style={{ fontFamily: 'monospace', fontSize: '0.85rem' }} />
            <button type="button" className="btn btn-secondary" onClick={handleCopyWebhook} disabled={!webhookUrl || webhookLoading}>
              {copySuccess ? 'Copied!' : 'Copy'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={handleRegenerateWebhook} disabled={!webhookUrl || regenerating}>
              {regenerating ? '...' : 'Regenerate'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
