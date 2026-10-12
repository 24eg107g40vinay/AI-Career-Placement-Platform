export const TOKEN_STORAGE_KEY = 'career-platform-token';
export const USER_STORAGE_KEY = 'career-platform-user';

export function readStoredUser() {
  const userValue = sessionStorage.getItem(USER_STORAGE_KEY);
  if (!userValue) return null;

  try {
    return JSON.parse(userValue);
  } catch (error) {
    console.warn('Stored account data is invalid and has been cleared.', error);
    sessionStorage.removeItem(USER_STORAGE_KEY);
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    return null;
  }
}

export function storeSession(authResponse) {
  sessionStorage.setItem(TOKEN_STORAGE_KEY, authResponse.token);
  const { id, name, email, role } = authResponse;
  sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify({ id, name, email, role }));
}

export function clearSession() {
  sessionStorage.removeItem(TOKEN_STORAGE_KEY);
  sessionStorage.removeItem(USER_STORAGE_KEY);
}
