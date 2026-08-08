import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function getJournalEntries() {
  const response = await api.get('/journal');
  return response.data;
}

export async function getJournalEntry(id) {
  const response = await api.get(`/journal/${id}`);
  return response.data;
}

export async function createJournalEntry(data) {
  const response = await api.post('/journal', data);
  return response.data;
}

export async function updateJournalEntry(id, data) {
  const response = await api.put(`/journal/${id}`, data);
  return response.data;
}

export async function createTrade(data) {
  const response = await api.post('/trades', data);
  return response.data;
}

export async function getTrades() {
  const response = await api.get('/trades');
  return response.data;
}

export default api;
