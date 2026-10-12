import { api } from './api';

export async function login(credentials) {
  const response = await api.post('/auth/login', credentials);
  return response.data;
}

export async function register(account) {
  const response = await api.post('/auth/register', account);
  return response.data;
}

export function getApiErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message || fallbackMessage;
}
