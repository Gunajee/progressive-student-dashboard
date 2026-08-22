import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import studentService from '../services/studentService';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';
import { ArrowLeft, BookOpen, Clock, Award, Play, CheckCircle, Flame } from 'lucide-react';

const StudentCourseDetails = () => {
  const { courseId } = useParams();
  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [progressMap, setProgressMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [errMessage, setErrMessage] = useState('');
  const [toast, setToast] = useState({ show: false, message: '', type: 'success' });
  const [timeInputs, setTimeInputs] = useState({}); // Tracking custom study minutes inputs per lesson

  const showToast = (message, type = 'success') => {
    setToast({ show: true, message, type });
    setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 4000);
  };

  const fetchCourseDetails = async () => {
    setLoading(true);
    setErrMessage('');
    try {
      const [courseData, lessonsData, progressData] = await Promise.all([
        studentService.getCourseDetails(courseId),
        studentService.getCourseLessons(courseId),
        studentService.getCourseProgress() // To get spent times
      ]);

      setCourse(courseData);
      setLessons(lessonsData);

      // Find progress specific to this course lessons
      const matchingProgress = progressData.find(p => p.courseTitle === courseData.title);
      // Map progress list to easy lookup by lesson title or id
      const courseProgressions = await apiFetchProgressDetails();
      setProgressMap(courseProgressions);
    } catch (err) {
      console.error(err);
      setErrMessage(err.response?.data?.message || 'Failed to load course syllabus.');
    } finally {
      setLoading(false);
    }
  };

  const apiFetchProgressDetails = async () => {
    try {
      // Fetch progress records directly from the progress endpoint
      const response = await studentService.getCourseProgress();
      // Map to id-to-progress-object
      const mapping = {};
      // We can also infer spent minutes by matching lessons
      return mapping;
    } catch (e) {
      return {};
    }
  };

  // Helper fetch mapping
  const loadData = async () => {
    setLoading(true);
    setErrMessage('');
    try {
      const courseData = await studentService.getCourseDetails(courseId);
      const lessonsData = await studentService.getCourseLessons(courseId);
      
      setCourse(courseData);
      setLessons(lessonsData);
    } catch (err) {
      setErrMessage('Failed to load course details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [courseId]);

  const handleUpdateProgress = async (lessonId, newStatus, durationMinutes) => {
    try {
      await studentService.updateProgress(lessonId, newStatus, durationMinutes);
      showToast(`Lesson marked as ${newStatus.replace('_', ' ').toLowerCase()} successfully!`);
      // Reload details to update stats
      const updatedCourse = await studentService.getCourseDetails(courseId);
      const updatedLessons = await studentService.getCourseLessons(courseId);
      setCourse(updatedCourse);
      setLessons(updatedLessons);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update progress.', 'error');
    }
  };

  if (loading) return <LoadingState message="Loading course syllabus..." />;
  if (errMessage) return <ErrorState message={errMessage} onRetry={loadData} />;
  if (!course) return <ErrorState message="Course details not found." onRetry={loadData} />;

  return (
    <div className="space-y-8 animate-fadeIn relative">
      {/* Toast Notification Banner */}
      {toast.show && (
        <div className={`fixed top-4 right-4 z-50 flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg border text-sm font-semibold transition-all duration-300 animate-slideIn ${
          toast.type === 'error'
            ? 'bg-red-50 text-red-800 border-red-150'
            : 'bg-emerald-50 text-emerald-800 border-emerald-150'
        }`}>
          {toast.type === 'error' ? <AlertCircle className="w-5 h-5" /> : <CheckCircle className="w-5 h-5 text-emerald-600" />}
          <span>{toast.message}</span>
        </div>
      )}

      {/* Back button */}
      <div>
        <Link to="/student/courses" className="inline-flex items-center gap-2 text-sm font-semibold text-gray-500 hover:text-indigo-650 transition">
          <ArrowLeft className="w-4 h-4" />
          Back to Courses
        </Link>
      </div>

      {/* Course Header Banner */}
      <section className="bg-white border border-gray-100 rounded-3xl p-6 md:p-8 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-3">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs font-semibold px-2.5 py-1 bg-gray-50 border border-gray-100 text-gray-600 rounded-lg">
              {course.category}
            </span>
            <span className="text-xs font-semibold px-2.5 py-1 bg-indigo-50 border border-indigo-100 text-indigo-700 rounded-lg">
              {course.difficulty}
            </span>
          </div>
          <h2 className="text-2xl md:text-3xl font-extrabold text-gray-900 tracking-tight">{course.title}</h2>
          <p className="text-sm text-gray-500 max-w-2xl">{course.description}</p>
        </div>

        {/* Course Stats aggregation */}
        <div className="shrink-0 flex items-center gap-6 bg-gray-50/50 border border-gray-100/50 p-6 rounded-2xl">
          <div className="text-center">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Lessons Finished</span>
            <span className="text-lg font-black text-gray-800 mt-1 block">
              {course.completedLessons} / {course.totalLessons}
            </span>
          </div>
          <div className="w-px h-10 bg-gray-200" />
          <div className="text-center">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Progress</span>
            <span className="text-lg font-black text-indigo-600 mt-1 block">
              {course.progressPercentage?.toFixed(0)}%
            </span>
          </div>
        </div>
      </section>

      {/* Syllabus Lessons Lists */}
      <section className="space-y-4">
        <div>
          <h3 className="text-lg font-bold text-gray-900">Course Syllabus</h3>
          <p className="text-xs text-gray-500 font-medium">Start and complete sequential topics at your own pace</p>
        </div>

        <div className="space-y-3.5">
          {lessons.map((lesson) => {
            const status = lesson.status || 'NOT_STARTED'; // Status populated from DTO
            const spent = lesson.timeSpentMinutes || 0;
            const inputVal = timeInputs[lesson.id] || '';

            return (
              <div 
                key={lesson.id}
                className={`bg-white border rounded-2xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all duration-200 ${
                  status === 'COMPLETED'
                    ? 'border-emerald-100 bg-emerald-50/5'
                    : status === 'IN_PROGRESS'
                    ? 'border-indigo-150 shadow-xs'
                    : 'border-gray-100'
                }`}
              >
                {/* Lesson Info */}
                <div className="flex items-start gap-4">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm shrink-0 mt-0.5 ${
                    status === 'COMPLETED'
                      ? 'bg-emerald-550/10 text-emerald-700'
                      : status === 'IN_PROGRESS'
                      ? 'bg-indigo-600 text-white'
                      : 'bg-gray-100 text-gray-500'
                  }`}>
                    {lesson.orderIndex}
                  </div>
                  <div>
                    <h4 className="text-base font-bold text-gray-900 leading-tight">
                      {lesson.title}
                    </h4>
                    <p className="text-xs text-gray-500 font-semibold mt-1 flex flex-wrap items-center gap-x-3 gap-y-1">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5" /> Estimated: {lesson.estimatedMinutes} mins
                      </span>
                      {spent > 0 && (
                        <span className="flex items-center gap-1 text-indigo-650 font-bold">
                          <Flame className="w-3.5 h-3.5" /> Logged Study: {spent} mins
                        </span>
                      )}
                    </p>
                  </div>
                </div>

                {/* Lesson Actions */}
                <div className="flex items-center gap-3 self-end sm:self-center">
                  {status === 'NOT_STARTED' && (
                    <button
                      onClick={() => handleUpdateProgress(lesson.id, 'IN_PROGRESS', 5)}
                      className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-xs transition cursor-pointer"
                    >
                      <Play className="w-3.5 h-3.5 fill-white" />
                      Start Lesson
                    </button>
                  )}

                  {status === 'IN_PROGRESS' && (
                    <div className="flex items-center gap-2 bg-gray-50 border border-gray-100 p-1.5 rounded-xl">
                      <div className="flex items-center gap-1.5 px-2">
                        <input
                          type="number"
                          min="1"
                          placeholder="Mins"
                          value={inputVal}
                          onChange={(e) => setTimeInputs({ ...timeInputs, [lesson.id]: e.target.value })}
                          className="w-16 bg-white border border-gray-200 rounded-lg py-1 px-1.5 text-center text-xs font-semibold focus:outline-hidden focus:ring-1 focus:ring-indigo-600 focus:border-indigo-600"
                        />
                        <span className="text-[10px] font-bold text-gray-400 uppercase">study mins</span>
                      </div>
                      <button
                        onClick={() => {
                          const mins = parseInt(inputVal) || 15; // default to 15 mins if empty
                          handleUpdateProgress(lesson.id, 'COMPLETED', mins);
                        }}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-xs transition cursor-pointer"
                      >
                        <CheckCircle className="w-3.5 h-3.5" />
                        Complete
                      </button>
                    </div>
                  )}

                  {status === 'COMPLETED' && (
                    <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-emerald-50 text-emerald-700 border border-emerald-100 rounded-xl text-sm font-bold">
                      <CheckCircle className="w-4 h-4 text-emerald-600" />
                      Completed
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
};

export default StudentCourseDetails;
