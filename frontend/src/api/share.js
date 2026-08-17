import axios from 'axios';

const publicClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function getPublicShare(shareToken) {
  const response = await publicClient.get(`/public/${shareToken}`);
  return response.data;
}

export default publicClient;