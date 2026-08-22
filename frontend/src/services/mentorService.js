import api from './api';

const mentorService = {
  getDashboard: async () => {
    const response = await api.get('/api/mentor/dashboard');
    return response.data;
  },

  getStudents: async (page = 0, size = 10) => {
    const response = await api.get(`/api/mentor/students?page=${page}&size=${size}`);
    return response.data;
  },

  getStudentDetails: async (studentId) => {
    const response = await api.get(`/api/mentor/students/${studentId}`);
    return response.data;
  },

  getStudentProgress: async (studentId) => {
    const response = await api.get(`/api/mentor/students/${studentId}/progress`);
    return response.data;
  },

  getStudentActivity: async (studentId) => {
    const response = await api.get(`/api/mentor/students/${studentId}/activity`);
    return response.data;
  },

  getStudentAnalytics: async (studentId, range = '7d') => {
    const response = await api.get(`/api/mentor/students/${studentId}/analytics?range=${range}`);
    return response.data;
  },

  exportStudentsCsv: async () => {
    const response = await api.get('/api/mentor/export/students.csv', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'students.csv');
    document.body.appendChild(link);
    link.click();
    link.remove();
  }
};

export default mentorService;
