import React, { createContext, useState, useEffect, useContext } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const user = await authService.getCurrentUser();
          setCurrentUser(user);
        } catch (err) {
          console.error('Failed to restore authentication session:', err);
          localStorage.removeItem('token');
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    setError(null);
    try {
      const data = await authService.login(email, password);
      // Expected structure: { token, user: { id, name, email, role } }
      localStorage.setItem('token', data.token);
      setCurrentUser(data.user);
      return data.user;
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please check credentials.';
      setError(message);
      throw new Error(message);
    }
  };

  const register = async (name, email, password) => {
    setError(null);
    try {
      await authService.register(name, email, password);
      // Registration complete, perform auto-login for a seamless student experience
      return await login(email, password);
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed. Try again.';
      setError(message);
      throw new Error(message);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setCurrentUser(null);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        loading,
        error,
        login,
        register,
        logout,
        isAuthenticated: !!currentUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
