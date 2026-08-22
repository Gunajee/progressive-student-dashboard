import api from './api';

const studentService = {
  getDashboard: async () => {
    const response = await api.get('/api/student/dashboard');
    return response.data;
  },

  getCourses: async () => {
    const response = await api.get('/api/student/courses');
    return response.data;
  },

  getCourseDetails: async (courseId) => {
    const response = await api.get(`/api/student/courses/${courseId}`);
    return response.data;
  },

  getCourseLessons: async (courseId) => {
    const response = await api.get(`/api/student/courses/${courseId}/lessons`);
    return response.data;
  },

  getLesson: async (lessonId) => {
    const response = await api.get(`/api/student/lessons/${lessonId}`);
    return response.data;
  },

  updateProgress: async (lessonId, status, timeSpentMinutes) => {
    const response = await api.put(`/api/student/lessons/${lessonId}/progress`, {
      status,
      timeSpentMinutes,
    });
    return response.data;
  },

  getRecommendations: async () => {
    const response = await api.get('/api/student/recommendations');
    return response.data;
  },

  getLearningTrend: async (range = '7d') => {
    const response = await api.get(`/api/student/analytics/learning-trend?range=${range}`);
    return response.data;
  },

  getCourseDistribution: async () => {
    const response = await api.get('/api/student/analytics/course-distribution');
    return response.data;
  },

  getCourseProgress: async () => {
    const response = await api.get('/api/student/analytics/course-progress');
    return response.data;
  }
};

export default studentService;
