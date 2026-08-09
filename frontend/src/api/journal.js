import client from './client';

export async function getJournalEntries() {
  const response = await client.get('/journal');
  return response.data;
}

export async function getJournalEntry(id) {
  const response = await client.get(`/journal/${id}`);
  return response.data;
}

export async function createJournalEntry(data) {
  const response = await client.post('/journal', data);
  return response.data;
}

export async function updateJournalEntry(id, data) {
  const response = await client.put(`/journal/${id}`, data);
  return response.data;
}

export async function createTrade(data) {
  const response = await client.post('/trades', data);
  return response.data;
}

export async function getTrades(journalEntryId) {
  const response = await client.get('/trades', {
    params: journalEntryId ? { journalEntryId } : {},
  });
  return response.data;
}

export default client;
