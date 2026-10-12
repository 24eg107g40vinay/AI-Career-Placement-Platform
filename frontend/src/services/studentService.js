import { api } from './api';

export async function getStudentProfile() {
  return (await api.get('/students/profile')).data;
}

export async function updateStudentProfile(profile) {
  return (await api.put('/students/profile', profile)).data;
}

export async function getStudentSkills() {
  return (await api.get('/students/profile/skills')).data;
}

export async function addStudentSkill(name) {
  return (await api.post('/students/profile/skills', { name })).data;
}

export async function removeStudentSkill(id) {
  return (await api.delete(`/students/profile/skills/${id}`)).data;
}

export async function getResumes() {
  return (await api.get('/students/resumes')).data;
}

export async function uploadResume(file, onUploadProgress) {
  const formData = new FormData();
  formData.append('file', file);
  return (await api.post('/students/resumes', formData, { onUploadProgress })).data;
}

export async function deleteResume(id) {
  await api.delete(`/students/resumes/${id}`);
}

export async function getApplications() {
  return (await api.get('/applications/my')).data;
}

export async function getRecommendations() {
  return (await api.get('/recommendations/jobs')).data;
}

export async function getSkillGap(jobId) {
  return (await api.get(`/skill-gap/${jobId}`)).data;
}

export async function sendAssistantMessage(message, history = []) {
  return (await api.post('/ai/chat', { message, history })).data;
}

export async function applyForJob(jobId, coverMessage = '') {
  return (await api.post(`/applications/${jobId}`, { coverMessage })).data;
}

export function getRequestError(error, fallback = 'Something went wrong. Please try again.') {
  return error.response?.data?.message || fallback;
}
