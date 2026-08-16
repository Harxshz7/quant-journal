import client from './client';

export async function getRulesStatus() {
  const response = await client.get('/rules/status');
  return response.data;
}

export default client;
