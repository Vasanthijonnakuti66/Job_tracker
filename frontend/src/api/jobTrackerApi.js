import api from './client.js';

export async function login(credentials) {
  const { data } = await api.post('/auth/login', credentials);
  return data;
}

export async function register(details) {
  const { data } = await api.post('/auth/register', details);
  return data;
}

export async function getCurrentUser() {
  const { data } = await api.get('/auth/me');
  return data;
}

export async function logout() {
  await api.post('/auth/logout');
}

export async function getApplications(params = {}) {
  const { data } = await api.get('/applications', { params });
  return data;
}

export async function getApplication(id) {
  const { data } = await api.get(`/applications/${id}`);
  return data;
}

export async function getAllApplications() {
  const firstPage = await getApplications({ page: 0, size: 100, sortBy: 'dateApplied', sortDir: 'desc' });
  if (firstPage.totalPages < 2) {
    return firstPage.content;
  }

  const remainingPages = await Promise.all(
    Array.from({ length: firstPage.totalPages - 1 }, (_, index) =>
      getApplications({ page: index + 1, size: 100, sortBy: 'dateApplied', sortDir: 'desc' })),
  );
  return firstPage.content.concat(...remainingPages.map((page) => page.content));
}

export async function createApplication(application) {
  const { data } = await api.post('/applications', application);
  return data;
}

export async function updateApplication(id, application) {
  const { data } = await api.put(`/applications/${id}`, application);
  return data;
}

export async function deleteApplication(id) {
  await api.delete(`/applications/${id}`);
}

export async function changeApplicationStatus(id, status) {
  const { data } = await api.patch(`/applications/${id}/status`, { status });
  return data;
}

export async function getAttachments(applicationId) {
  const { data } = await api.get(`/applications/${applicationId}/attachments`);
  return data;
}

export async function uploadAttachment(applicationId, file, type) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await api.post(
    `/applications/${applicationId}/attachments`,
    formData,
    {
      params: { type },
      headers: { 'Content-Type': 'multipart/form-data' },
    },
  );
  return data;
}

export async function downloadAttachment(applicationId, attachmentId) {
  const response = await api.get(
    `/applications/${applicationId}/attachments/${attachmentId}/download`,
    { responseType: 'blob' },
  );
  return response;
}

export async function getDashboard() {
  const { data } = await api.get('/dashboard');
  return data;
}

export async function getAnalytics() {
  const { data } = await api.get('/analytics');
  return data;
}
