import axios from 'axios';

const client = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

const refreshClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshPromise = null;

export function clearAuthStorage() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
}

export function storeAuthTokens(data) {
  localStorage.setItem('token', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  if (data.user) {
    localStorage.setItem('user', JSON.stringify(data.user));
  }
}

export async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  if (!refreshPromise) {
    refreshPromise = refreshClient
      .post('/auth/refresh', { refreshToken })
      .then((response) => {
        storeAuthTokens(response.data);
        return response.data;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isUnauthorized = error.response?.status === 401;
    const requestUrl = originalRequest?.url || '';
    const isAuthEndpoint =
      requestUrl.includes('/auth/login') ||
      requestUrl.includes('/auth/register') ||
      requestUrl.includes('/auth/refresh');

    if (!isUnauthorized || !originalRequest || originalRequest._retry || isAuthEndpoint) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const data = await refreshAccessToken();
      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
      return client(originalRequest);
    } catch (refreshError) {
      clearAuthStorage();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      return Promise.reject(refreshError);
    }
  }
);

export default client;
