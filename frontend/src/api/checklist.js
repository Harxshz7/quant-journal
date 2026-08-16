import client from './client';

export async function getChecklistTemplates() {
  const response = await client.get('/checklist-templates');
  return response.data;
}

export async function createChecklistTemplate(data) {
  const response = await client.post('/checklist-templates', data);
  return response.data;
}

export async function updateChecklistTemplate(id, data) {
  const response = await client.put(`/checklist-templates/${id}`, data);
  return response.data;
}

export async function deactivateChecklistTemplate(id) {
  await client.delete(`/checklist-templates/${id}`);
}
