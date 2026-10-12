import axios from 'axios';
import { TOKEN_STORAGE_KEY } from './authStorage';

const configuredBaseURL = import.meta.env.VITE_API_URL?.trim();
const baseURL = (configuredBaseURL || (import.meta.env.PROD ? '' : 'http://localhost:8080/api')).replace(/\/+$/, '');

export const api = axios.create({
  baseURL,
  headers: { Accept: 'application/json' },
});

export function ensureApiConfigured() {
  if (import.meta.env.PROD && !configuredBaseURL) {
    throw new Error('The API URL is not configured. Set VITE_API_URL in the Vercel project settings.');
  }
}

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_STORAGE_KEY);
  if (token && !config.url?.startsWith('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
