import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, PublicRoute } from './components/common/RouteGuard';
import MainLayout from './components/layout/MainLayout';
import Login from './pages/Login';
import Register from './pages/Register';
import StudentDashboard from './pages/StudentDashboard';
import StudentCourses from './pages/StudentCourses';
import StudentCourseDetails from './pages/StudentCourseDetails';
import MentorDashboard from './pages/MentorDashboard';
import MentorStudentDetails from './pages/MentorStudentDetails';
import AdminDashboard from './pages/AdminDashboard';
import CourseEditor from './pages/CourseEditor';
import LessonEditor from './pages/LessonEditor';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public routes (unauthenticated access only) */}
          <Route element={<PublicRoute />}>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
          </Route>

          {/* Student Protected routes */}
          <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
            <Route element={<MainLayout />}>
              <Route path="/student/dashboard" element={<StudentDashboard />} />
              <Route path="/student/courses" element={<StudentCourses />} />
              <Route path="/student/courses/:courseId" element={<StudentCourseDetails />} />
            </Route>
          </Route>

          {/* Mentor Protected routes */}
          <Route element={<ProtectedRoute allowedRoles={['MENTOR']} />}>
            <Route element={<MainLayout />}>
              <Route path="/mentor/dashboard" element={<MentorDashboard />} />
              <Route path="/mentor/students/:studentId" element={<MentorStudentDetails />} />
            </Route>
          </Route>

          {/* Admin Protected routes */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route element={<MainLayout />}>
              <Route path="/admin/dashboard" element={<AdminDashboard />} />
              <Route path="/admin/courses/new" element={<CourseEditor />} />
              <Route path="/admin/courses/:courseId/edit" element={<CourseEditor />} />
              <Route path="/admin/courses/:courseId/lessons/new" element={<LessonEditor />} />
              <Route path="/admin/lessons/:lessonId/edit" element={<LessonEditor />} />
            </Route>
          </Route>

          {/* Root & Fallback Redirection */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
