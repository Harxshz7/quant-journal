import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export async function loginApi(credentials) {
  const response = await api.post('/auth/login', credentials);
  return response.data;
}

export async function registerApi(userData) {
  const response = await api.post('/auth/register', userData);
  return response.data;
}

export async function getCurrentUserApi() {
  const response = await api.get('/auth/me');
  return response.data;
}

export default api;
