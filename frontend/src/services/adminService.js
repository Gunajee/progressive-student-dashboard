import api from './api';

export const adminService = {
  // Courses
  getAllCourses: async () => {
    const response = await api.get('/api/admin/courses');
    return response.data;
  },
  getCourseById: async (id) => {
    const response = await api.get(`/api/admin/courses/${id}`);
    return response.data;
  },
  createCourse: async (courseData) => {
    const response = await api.post('/api/admin/courses', courseData);
    return response.data;
  },
  updateCourse: async (id, courseData) => {
    const response = await api.put(`/api/admin/courses/${id}`, courseData);
    return response.data;
  },
  deleteCourse: async (id) => {
    const response = await api.delete(`/api/admin/courses/${id}`);
    return response.data;
  },

  // Lessons
  getLessonsByCourse: async (courseId) => {
    const response = await api.get(`/api/admin/lessons/course/${courseId}`);
    return response.data;
  },
  getLessonById: async (id) => {
    const response = await api.get(`/api/admin/lessons/${id}`);
    return response.data;
  },
  createLesson: async (courseId, lessonData) => {
    const response = await api.post(`/api/admin/lessons/course/${courseId}`, lessonData);
    return response.data;
  },
  updateLesson: async (id, lessonData) => {
    const response = await api.put(`/api/admin/lessons/${id}`, lessonData);
    return response.data;
  },
  deleteLesson: async (id) => {
    const response = await api.delete(`/api/admin/lessons/${id}`);
    return response.data;
  }
};
