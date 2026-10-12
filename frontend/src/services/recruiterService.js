import { api } from './api';

export async function getRecruiterDashboard() {
  return (await api.get('/recruiters/dashboard')).data;
}

export async function getCompanyProfile() {
  return (await api.get('/recruiters/company')).data;
}

export async function saveCompanyProfile(company) {
  return (await api.put('/recruiters/company', company)).data;
}

export async function getMyJobs() {
  return (await api.get('/jobs/mine')).data;
}

export async function saveJob(job, id) {
  return id ? (await api.put(`/jobs/${id}`, job)).data : (await api.post('/jobs', job)).data;
}

export async function deactivateJob(id) {
  await api.delete(`/jobs/${id}`);
}

export async function getApplicants(jobId) {
  return (await api.get(`/applications/job/${jobId}`)).data;
}

export async function updateApplicantStatus(id, status) {
  return (await api.put(`/applications/${id}/status`, { status })).data;
}
