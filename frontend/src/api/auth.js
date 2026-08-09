import client from './client';

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

export default client;
