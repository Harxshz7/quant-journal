import client from './client';

export async function getLessons(tag) {
  const params = {};
  if (tag) params.tag = tag;
  const response = await client.get('/lessons', { params });
  return response.data;
}

export async function createLesson(data) {
  const response = await client.post('/lessons', data);
  return response.data;
}

export async function updateLesson(id, data) {
  const response = await client.put(`/lessons/${id}`, data);
  return response.data;
}

export async function deleteLesson(id) {
  await client.delete(`/lessons/${id}`);
}
