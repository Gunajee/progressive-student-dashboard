import api from './api';

const authService = {
  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data; // Expected format: { token, user: { id, name, email, role } }
  },

  register: async (name, email, password) => {
    const response = await api.post('/api/auth/register', { name, email, password });
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await api.get('/api/auth/me');
    return response.data; // Expected: { id, name, email, role }
  }
};

export default authService;
