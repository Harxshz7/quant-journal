import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function getTrades() {
  const response = await api.get('/trades');
  return response.data;
}

export async function createTrade(trade) {
  const response = await api.post('/trades', trade);
  return response.data;
}
