import client, { clearAuthStorage, refreshAccessToken, storeAuthTokens } from './client';

export async function loginApi(credentials) {
  const response = await client.post('/auth/login', credentials);
  return response.data;
}

export async function registerApi(userData) {
  const response = await client.post('/auth/register', userData);
  return response.data;
}

export async function getCurrentUserApi() {
  const response = await client.get('/auth/me');
  return response.data;
}

export async function refreshApi() {
  return refreshAccessToken();
}

export async function logoutApi() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (refreshToken) {
    try {
      await client.post('/auth/logout', { refreshToken });
    } catch (err) {
      // Ignore logout errors and clear local session anyway
    }
  }
  clearAuthStorage();
}

export async function updateProfileApi(profile) {
  const response = await client.put('/auth/me', profile);
  return response.data;
}

export async function changePasswordApi(passwords) {
  await client.put('/auth/me/password', passwords);
}

export async function getSettings() {
  const response = await client.get('/auth/me/settings');
  return response.data;
}

export async function updateSettings(settings) {
  const response = await client.put('/auth/me/settings', settings);
  return response.data;
}

export async function getWebhookUrl() {
  const response = await client.get('/auth/me/webhook');
  return response.data;
}

export async function regenerateWebhookUrl() {
  const response = await client.post('/auth/me/webhook/regenerate');
  return response.data;
}

export async function getShareStatus() {
  const response = await client.get('/auth/me/share');
  return response.data;
}

export async function enableShare() {
  const response = await client.post('/auth/me/share/enable');
  return response.data;
}

export async function disableShare() {
  const response = await client.post('/auth/me/share/disable');
  return response.data;
}

export { storeAuthTokens, clearAuthStorage };

export default client;
