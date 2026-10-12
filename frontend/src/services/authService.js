import { api, ensureApiConfigured } from './api';

export async function login(credentials) {
  ensureApiConfigured();
  const response = await api.post('/auth/login', credentials);
  return response.data;
}

export async function register(account) {
  ensureApiConfigured();
  const response = await api.post('/auth/register', account);
  return response.data;
}

export function getApiErrorMessage(error, fallbackMessage) {
  if (!error.response && error.message?.startsWith('The API URL is not configured.')) {
    return error.message;
  }
  if (!error.response) {
    return 'Could not reach the server. Check the Vercel VITE_API_URL and Render APP_FRONTEND_URLS settings.';
  }
  return error.response?.data?.message || fallbackMessage;
}
