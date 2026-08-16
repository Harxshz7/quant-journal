import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { changePasswordApi, getWebhookUrl, regenerateWebhookUrl, getSettings, updateSettings } from '../api/auth';
import { getChecklistTemplates, createChecklistTemplate, updateChecklistTemplate, deactivateChecklistTemplate } from '../api/checklist';
import NavBar from '../components/NavBar';

const BUY_SELL_TEMPLATE = `{
  "ticker": "{{ticker}}",
  "action": "{{strategy.order.action}}",
  "quantity": "{{strategy.order.contracts}}",
  "price": "{{strategy.order.price}}",
  "strategy": "{{strategy.name}}",
  "time": "{{time}}"
}`;

const CLOSE_TEMPLATE = `{
  "ticker": "{{ticker}}",
  "action": "close",
  "price": "{{strategy.order.price}}",
  "time": "{{time}}"
}`;

export default function Profile() {
  const { user, updateProfile } = useAuth();

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

  const [settings, setSettings] = useState({ accountSize: '', dailyLossLimitAmount: '', monthlyGoalPnl: '' });
  const [settingsLoading, setSettingsLoading] = useState(true);
  const [settingsSaving, setSettingsSaving] = useState(false);
  const [settingsMsg, setSettingsMsg] = useState('');
  const [settingsError, setSettingsError] = useState('');

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setEmail(user.email || '');
      fetchWebhookUrl();
      fetchTemplates();
      fetchSettings();
    }
  }, [user]);

  const fetchSettings = async () => {
    try {
      setSettingsLoading(true);
      const data = await getSettings();
      setSettings({
        accountSize: data.accountSize ?? '',
        dailyLossLimitAmount: data.dailyLossLimitAmount ?? '',
        monthlyGoalPnl: data.monthlyGoalPnl ?? '',
      });
    } catch (err) {
      setSettingsError('Failed to load settings');
    } finally {
      setSettingsLoading(false);
    }
  };

  const handleSaveSettings = async (e) => {
    e.preventDefault();
    setSettingsMsg('');
    setSettingsError('');
    try {
      setSettingsSaving(true);
      const toNumberOrNull = (v) => (v === '' || v === null || v === undefined ? null : Number(v));
      await updateSettings({
        accountSize: toNumberOrNull(settings.accountSize),
        dailyLossLimitAmount: toNumberOrNull(settings.dailyLossLimitAmount),
        monthlyGoalPnl: toNumberOrNull(settings.monthlyGoalPnl),
      });
      setSettingsMsg('Trading settings saved.');
    } catch (err) {
      setSettingsError(err.response?.data?.message || 'Failed to save settings');
    } finally {
      setSettingsSaving(false);
    }
  };

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
      <NavBar active="profile" />

      <header className="header">
        <div>
          <h1>Profile</h1>
          {user && (
            <p className="muted" style={{ margin: '0.25rem 0 0 0', fontSize: '0.9rem' }}>
              Manage your account settings
            </p>
          )}
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
          <h2>Trading Settings</h2>
          <p>Account size, daily loss limit, and monthly P&L goal</p>
        </div>
        {settingsError && <div className="alert alert-error">{settingsError}</div>}
        {settingsMsg && (
          <div className="alert alert-error" style={{ borderColor: 'var(--pnl-positive)', color: 'var(--pnl-positive)' }}>
            {settingsMsg}
          </div>
        )}
        {settingsLoading ? (
          <p className="loading">Loading settings...</p>
        ) : (
          <form onSubmit={handleSaveSettings}>
            <div className="form-group">
              <label htmlFor="accountSize">Account Size</label>
              <input
                id="accountSize"
                type="number"
                step="any"
                min="0"
                placeholder="e.g. 100000"
                value={settings.accountSize}
                onChange={(e) => setSettings({ ...settings, accountSize: e.target.value })}
              />
              <p className="muted" style={{ fontSize: '0.8rem', margin: 0 }}>
                Used by the position size calculator on the Calculator page.
              </p>
            </div>
            <div className="form-group">
              <label htmlFor="dailyLossLimitAmount">Daily Loss Limit</label>
              <input
                id="dailyLossLimitAmount"
                type="number"
                step="any"
                min="0"
                placeholder="e.g. 500"
                value={settings.dailyLossLimitAmount}
                onChange={(e) => setSettings({ ...settings, dailyLossLimitAmount: e.target.value })}
              />
              <p className="muted" style={{ fontSize: '0.8rem', margin: 0 }}>
                Shows a warning banner when today's realized P&amp;L hits this loss.
              </p>
            </div>
            <div className="form-group">
              <label htmlFor="monthlyGoalPnl">Monthly P&amp;L Goal</label>
              <input
                id="monthlyGoalPnl"
                type="number"
                step="any"
                placeholder="e.g. 10000"
                value={settings.monthlyGoalPnl}
                onChange={(e) => setSettings({ ...settings, monthlyGoalPnl: e.target.value })}
              />
              <p className="muted" style={{ fontSize: '0.8rem', margin: 0 }}>
                Shows a progress bar toward this goal on the dashboard.
              </p>
            </div>
            <button type="submit" className="btn btn-primary" disabled={settingsSaving}>
              {settingsSaving ? 'Saving...' : 'Save Trading Settings'}
            </button>
          </form>
        )}
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

      <div className="auth-card" style={{ marginBottom: '1.75rem' }}>
        <div className="auth-header">
          <h2>Webhook & Automation Settings</h2>
          <p>Personal endpoint for TradingView alert automation</p>
        </div>
        <div className="alert" style={{ background: 'rgba(74, 222, 128, 0.1)', borderColor: 'rgba(74, 222, 128, 0.3)', color: '#4ade80', fontSize: '0.85rem' }}>
          <strong>Active</strong> — TradingView alerts sent to this URL will automatically create or close trades.
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

      <div className="auth-card">
        <div className="auth-header">
          <h2>TradingView Setup Instructions</h2>
          <p>How to connect your TradingView alerts to this journal</p>
        </div>

        <div style={{ fontSize: '0.9rem', color: 'var(--muted)', lineHeight: 1.7 }}>
          <ol style={{ paddingLeft: '1.25rem', margin: 0 }}>
            <li style={{ marginBottom: '0.75rem' }}>
              <strong style={{ color: 'var(--text)' }}>Open your TradingView chart</strong> and add your indicator or strategy.
            </li>
            <li style={{ marginBottom: '0.75rem' }}>
              <strong style={{ color: 'var(--text)' }}>Create an Alert</strong> — right-click on the chart or use the Alerts panel. Configure your conditions.
            </li>
            <li style={{ marginBottom: '0.75rem' }}>
              <strong style={{ color: 'var(--text)' }}>Under "Webhook URL"</strong>, paste your webhook URL from above.
            </li>
            <li style={{ marginBottom: '0.75rem' }}>
              <strong style={{ color: 'var(--text)' }}>Under "Message"</strong>, paste the JSON template below. Use the copy button for convenience.
            </li>
          </ol>
        </div>

        <div style={{ marginTop: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <h3 style={{ margin: 0, fontSize: '0.95rem', color: 'var(--text)' }}>Buy / Sell Alert Template</h3>
            <button
              type="button"
              className="btn btn-sm btn-secondary"
              onClick={() => navigator.clipboard.writeText(BUY_SELL_TEMPLATE).then(() => alert('Copied!'))}
            >
              Copy
            </button>
          </div>
          <pre style={{
            background: 'var(--surface)', padding: '0.75rem 1rem', borderRadius: 10,
            fontSize: '0.8rem', fontFamily: 'monospace', color: 'var(--text)',
            overflow: 'auto', margin: 0, whiteSpace: 'pre-wrap',
            boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.2)',
          }}>
            {BUY_SELL_TEMPLATE}
          </pre>
          <p style={{ fontSize: '0.8rem', color: 'var(--muted)', marginTop: '0.4rem' }}>
            <code>action</code> will be <code>buy</code> or <code>sell</code> based on your strategy order side.
          </p>
        </div>

        <div style={{ marginTop: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <h3 style={{ margin: 0, fontSize: '0.95rem', color: 'var(--text)' }}>Close Position Alert Template</h3>
            <button
              type="button"
              className="btn btn-sm btn-secondary"
              onClick={() => navigator.clipboard.writeText(CLOSE_TEMPLATE).then(() => alert('Copied!'))}
            >
              Copy
            </button>
          </div>
          <pre style={{
            background: 'var(--surface)', padding: '0.75rem 1rem', borderRadius: 10,
            fontSize: '0.8rem', fontFamily: 'monospace', color: 'var(--text)',
            overflow: 'auto', margin: 0, whiteSpace: 'pre-wrap',
            boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.2)',
          }}>
            {CLOSE_TEMPLATE}
          </pre>
          <p style={{ fontSize: '0.8rem', color: 'var(--muted)', marginTop: '0.4rem' }}>
            Use this for exit signals. It will close your most recent open trade matching the ticker.
          </p>
        </div>

        <div style={{ marginTop: '1.25rem', padding: '0.75rem 1rem', borderRadius: 10, background: 'rgba(255,255,255,0.03)', fontSize: '0.85rem', color: 'var(--muted)' }}>
          <strong style={{ color: 'var(--text)' }}>How it works:</strong> When TradingView fires an alert, it sends the JSON payload to your webhook URL. The server creates a trade entry automatically (buy/sell) or closes an existing open trade (close). A journal entry is created for today if one doesn't exist yet.
        </div>
      </div>
    </div>
  );
}
