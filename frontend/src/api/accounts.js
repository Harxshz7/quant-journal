import client from './client';

export async function getAccounts() {
  const response = await client.get('/accounts');
  return response.data;
}

export async function createAccount(name) {
  const response = await client.post('/accounts', { name });
  return response.data;
}

export async function updateAccount(id, name) {
  const response = await client.put(`/accounts/${id}`, { name });
  return response.data;
}

export async function setDefaultAccount(id) {
  const response = await client.post(`/accounts/${id}/default`);
  return response.data;
}

export async function deleteAccount(id) {
  await client.delete(`/accounts/${id}`);
}

export default client;