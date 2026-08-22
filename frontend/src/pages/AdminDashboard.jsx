import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Edit, Trash2, BookOpen } from 'lucide-react';
import { adminService } from '../services/adminService';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';

const AdminDashboard = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCourses();
  }, []);

  const fetchCourses = async () => {
    try {
      const data = await adminService.getAllCourses();
      setCourses(data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load courses');
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this course?')) {
      try {
        await adminService.deleteCourse(id);
        setCourses(courses.filter(c => c.id !== id));
      } catch (err) {
        alert('Failed to delete course');
      }
    }
  };

  if (loading) return <LoadingState />;
  if (error) return <ErrorState message={error} retry={fetchCourses} />;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Course Management</h1>
          <p className="text-gray-500 mt-1">Create and manage your educational content.</p>
        </div>
        <Link
          to="/admin/courses/new"
          className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-semibold hover:bg-indigo-700 transition"
        >
          <Plus className="w-4 h-4" />
          New Course
        </Link>
      </div>

      <div className="bg-white rounded-xl shadow-xs border border-gray-100 overflow-hidden">
        <ul className="divide-y divide-gray-100">
          {courses.map(course => (
            <li key={course.id} className="p-6 flex items-center justify-between hover:bg-gray-50/50 transition">
              <div className="flex items-start gap-4">
                <div className="bg-indigo-50 p-3 rounded-lg text-indigo-600">
                  <BookOpen className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900 text-lg">{course.title}</h3>
                  <div className="flex gap-2 text-sm text-gray-500 mt-1">
                    <span>{course.category}</span>
                    <span>•</span>
                    <span>{course.difficulty}</span>
                    <span>•</span>
                    <span>{course.estimatedHours} Hours</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Link
                  to={`/admin/courses/${course.id}/edit`}
                  className="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                >
                  <Edit className="w-5 h-5" />
                </Link>
                <button
                  onClick={() => handleDelete(course.id)}
                  className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition cursor-pointer"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            </li>
          ))}
          {courses.length === 0 && (
            <li className="p-8 text-center text-gray-500">
              No courses found. Create one to get started.
            </li>
          )}
        </ul>
      </div>
    </div>
  );
};

export default AdminDashboard;
