import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as jobTrackerApi from '../api/jobTrackerApi.js';
import { getErrorMessage } from '../utils/errors.js';

const AuthContext = createContext(null);
const TOKEN_KEY = 'job-tracker-token';

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(localStorage.getItem(TOKEN_KEY)));
  const [notice, setNotice] = useState('');

  const saveAuth = useCallback((response) => {
    localStorage.setItem(TOKEN_KEY, response.accessToken);
    setToken(response.accessToken);
    setUser(response.user);
    setNotice('');
  }, []);

  const signIn = useCallback(async (credentials) => {
    saveAuth(await jobTrackerApi.login(credentials));
  }, [saveAuth]);

  const signUp = useCallback(async (details) => {
    saveAuth(await jobTrackerApi.register(details));
  }, [saveAuth]);

  const signOut = useCallback(async () => {
    try {
      await jobTrackerApi.logout();
    } catch (error) {
      setNotice('You were signed out on this device, but the server could not confirm logout.');
      throw error;
    } finally {
      localStorage.removeItem(TOKEN_KEY);
      setToken(null);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return undefined;
    }

    let active = true;
    jobTrackerApi.getCurrentUser()
      .then((currentUser) => {
        if (active) setUser(currentUser);
      })
      .catch((error) => {
        if (active) {
          setNotice(`Could not verify your session: ${getErrorMessage(error)}`);
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [token]);

  useEffect(() => {
    const handleUnauthorized = () => {
      localStorage.removeItem(TOKEN_KEY);
      setToken(null);
      setUser(null);
      setNotice('');
    };
    window.addEventListener('job-tracker:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('job-tracker:unauthorized', handleUnauthorized);
  }, []);

  const value = useMemo(() => ({
    token,
    user,
    loading,
    notice,
    signIn,
    signUp,
    signOut,
  }), [token, user, loading, notice, signIn, signUp, signOut]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
