import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import { adminService } from '../services/adminService';
import LoadingState from '../components/common/LoadingState';
import ReactMarkdown from 'react-markdown';

const LessonEditor = () => {
  const { courseId, lessonId } = useParams();
  const navigate = useNavigate();
  const isEditing = Boolean(lessonId);

  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [isPreview, setIsPreview] = useState(false);
  
  const [lesson, setLesson] = useState({
    title: '',
    description: '',
    orderIndex: 1,
    estimatedMinutes: 10,
    content: ''
  });

  // Track parent course ID for back navigation
  const [parentCourseId, setParentCourseId] = useState(courseId);

  useEffect(() => {
    if (isEditing) {
      const fetchLesson = async () => {
        try {
          const lData = await adminService.getLessonById(lessonId);
          setLesson(lData);
          setParentCourseId(lData.courseId);
          setLoading(false);
        } catch (err) {
          console.error(err);
          navigate('/admin/dashboard');
        }
      };
      fetchLesson();
    }
  }, [lessonId, navigate, isEditing]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setLesson(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isEditing) {
        await adminService.updateLesson(lessonId, lesson);
      } else {
        await adminService.createLesson(courseId, lesson);
      }
      navigate(`/admin/courses/${parentCourseId}/edit`);
    } catch (err) {
      console.error(err);
      alert('Failed to save lesson');
      setSaving(false);
    }
  };

  if (loading) return <LoadingState />;

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-12">
      <div className="flex items-center gap-4">
        <Link to={`/admin/courses/${parentCourseId}/edit`} className="p-2 bg-white rounded-lg shadow-xs hover:bg-gray-50 transition text-gray-500">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <h1 className="text-2xl font-bold text-gray-900">
          {isEditing ? 'Edit Lesson' : 'Create New Lesson'}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white p-6 md:p-8 rounded-xl shadow-xs border border-gray-100 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="md:col-span-2">
              <label className="block text-sm font-semibold text-gray-700 mb-1">Lesson Title</label>
              <input
                type="text"
                name="title"
                required
                value={lesson.title}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Order Index (Position)</label>
              <input
                type="number"
                name="orderIndex"
                min="1"
                required
                value={lesson.orderIndex}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Estimated Minutes</label>
              <input
                type="number"
                name="estimatedMinutes"
                min="1"
                required
                value={lesson.estimatedMinutes}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-semibold text-gray-700 mb-1">Short Description</label>
              <textarea
                name="description"
                rows="2"
                value={lesson.description}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:ring-2 focus:ring-indigo-600 focus:outline-hidden"
              />
            </div>
          </div>
        </div>

        {/* Markdown Content Editor */}
        <div className="bg-white rounded-xl shadow-xs border border-gray-100 overflow-hidden flex flex-col h-[600px]">
          <div className="flex border-b border-gray-100 bg-gray-50/50 p-2">
            <button
              type="button"
              onClick={() => setIsPreview(false)}
              className={`px-4 py-2 rounded-lg text-sm font-semibold transition cursor-pointer ${!isPreview ? 'bg-white shadow-xs text-indigo-600' : 'text-gray-500 hover:bg-gray-100'}`}
            >
              Write
            </button>
            <button
              type="button"
              onClick={() => setIsPreview(true)}
              className={`px-4 py-2 rounded-lg text-sm font-semibold transition cursor-pointer ${isPreview ? 'bg-white shadow-xs text-indigo-600' : 'text-gray-500 hover:bg-gray-100'}`}
            >
              Preview
            </button>
          </div>
          
          <div className="flex-1 p-0 overflow-hidden relative">
            {!isPreview ? (
              <textarea
                name="content"
                value={lesson.content}
                onChange={handleChange}
                placeholder="Write your lesson content using Markdown...&#10;&#10;# Heading 1&#10;## Heading 2&#10;**Bold text**&#10;`Code block`"
                className="absolute inset-0 w-full h-full p-6 resize-none focus:outline-hidden text-gray-900 font-mono text-sm"
              />
            ) : (
              <div className="absolute inset-0 w-full h-full p-6 overflow-y-auto prose prose-indigo max-w-none text-gray-900">
                {lesson.content ? (
                  <ReactMarkdown>{lesson.content}</ReactMarkdown>
                ) : (
                  <p className="text-gray-400 italic">Nothing to preview.</p>
                )}
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-end pt-2">
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 bg-indigo-600 text-white px-8 py-3 rounded-xl text-sm font-semibold hover:bg-indigo-700 transition disabled:opacity-50 shadow-md cursor-pointer"
          >
            <Save className="w-5 h-5" />
            {saving ? 'Saving...' : 'Save Lesson Content'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default LessonEditor;
