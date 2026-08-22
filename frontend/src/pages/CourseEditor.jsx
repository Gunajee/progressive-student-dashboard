import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save, Plus, Trash2, Edit } from 'lucide-react';
import { adminService } from '../services/adminService';
import LoadingState from '../components/common/LoadingState';

const CourseEditor = () => {
  const { courseId } = useParams();
  const navigate = useNavigate();
  const isEditing = Boolean(courseId);

  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  
  const [course, setCourse] = useState({
    title: '',
    category: '',
    difficulty: 'BEGINNER',
    estimatedHours: 1,
    description: ''
  });
  
  const [lessons, setLessons] = useState([]);

  useEffect(() => {
    if (isEditing) {
      const fetchData = async () => {
        try {
          const cData = await adminService.getCourseById(courseId);
          setCourse(cData);
          const lData = await adminService.getLessonsByCourse(courseId);
          setLessons(lData);
          setLoading(false);
        } catch (err) {
          console.error(err);
          navigate('/admin/dashboard');
        }
      };
      fetchData();
    }
  }, [courseId, navigate, isEditing]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCourse(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isEditing) {
        await adminService.updateCourse(courseId, course);
        navigate('/admin/dashboard');
      } else {
        const newCourse = await adminService.createCourse(course);
        navigate(`/admin/courses/${newCourse.id}/edit`);
      }
    } catch (err) {
      console.error(err);
      alert('Failed to save course');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteLesson = async (id) => {
    if (window.confirm('Delete this lesson?')) {
      try {
        await adminService.deleteLesson(id);
        setLessons(lessons.filter(l => l.id !== id));
      } catch (err) {
        alert('Failed to delete lesson');
      }
    }
  };

  if (loading) return <LoadingState />;

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link to="/admin/dashboard" className="p-2 bg-white rounded-lg shadow-xs hover:bg-gray-50 transition text-gray-500">
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Edit Course' : 'Create New Course'}
          </h1>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="bg-white p-6 md:p-8 rounded-xl shadow-xs border border-gray-100 space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="md:col-span-2">
            <label className="block text-sm font-semibold text-gray-700 mb-1">Course Title</label>
            <input
              type="text"
              name="title"
              required
              value={course.title}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
              placeholder="e.g. Advanced React Patterns"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Category</label>
            <input
              type="text"
              name="category"
              required
              value={course.category}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Difficulty</label>
            <select
              name="difficulty"
              value={course.difficulty}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
            >
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Estimated Hours</label>
            <input
              type="number"
              name="estimatedHours"
              min="1"
              required
              value={course.estimatedHours}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
            />
          </div>

          <div className="md:col-span-2">
            <label className="block text-sm font-semibold text-gray-700 mb-1">Description</label>
            <textarea
              name="description"
              rows="3"
              value={course.description}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
            />
          </div>
        </div>

        <div className="flex justify-end pt-4 border-t border-gray-100">
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 bg-indigo-600 text-white px-6 py-2.5 rounded-lg text-sm font-semibold hover:bg-indigo-700 transition disabled:opacity-50 cursor-pointer"
          >
            <Save className="w-4 h-4" />
            {saving ? 'Saving...' : 'Save Course'}
          </button>
        </div>
      </form>

      {isEditing && (
        <div className="bg-white rounded-xl shadow-xs border border-gray-100 overflow-hidden">
          <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
            <h2 className="text-lg font-bold text-gray-900">Lessons Syllabus</h2>
            <Link
              to={`/admin/courses/${courseId}/lessons/new`}
              className="flex items-center gap-2 text-indigo-600 hover:text-indigo-700 font-semibold text-sm"
            >
              <Plus className="w-4 h-4" />
              Add Lesson
            </Link>
          </div>
          <ul className="divide-y divide-gray-100">
            {lessons.map(lesson => (
              <li key={lesson.id} className="p-4 sm:p-6 flex items-center justify-between hover:bg-gray-50 transition">
                <div>
                  <span className="text-xs font-bold text-gray-400">LESSON {lesson.orderIndex}</span>
                  <h4 className="font-semibold text-gray-900 mt-1">{lesson.title}</h4>
                </div>
                <div className="flex gap-2">
                  <Link
                    to={`/admin/lessons/${lesson.id}/edit`}
                    className="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                  >
                    <Edit className="w-5 h-5" />
                  </Link>
                  <button
                    onClick={() => handleDeleteLesson(lesson.id)}
                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition cursor-pointer"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </div>
              </li>
            ))}
            {lessons.length === 0 && (
              <li className="p-8 text-center text-gray-500 text-sm">
                No lessons added yet.
              </li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
};

export default CourseEditor;
