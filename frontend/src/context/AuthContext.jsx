import { createContext, useContext, useMemo, useState } from 'react';
import { login as loginRequest, register as registerRequest } from '../services/authService';
import { clearSession, readStoredUser, storeSession } from '../services/authStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  async function login(credentials) {
    const session = await loginRequest(credentials);
    storeSession(session);
    setUser({ id: session.id, name: session.name, email: session.email, role: session.role });
    return session;
  }

  async function register(account) {
    const session = await registerRequest(account);
    storeSession(session);
    setUser({ id: session.id, name: session.name, email: session.email, role: session.role });
    return session;
  }

  function logout() {
    clearSession();
    setUser(null);
  }

  const value = useMemo(() => ({ user, login, register, logout }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
