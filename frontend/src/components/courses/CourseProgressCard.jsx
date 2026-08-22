import React from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen, Clock, Award, ArrowRight } from 'lucide-react';

const CourseProgressCard = ({ course }) => {
  const navigate = useNavigate();

  const getDifficultyColor = (difficulty) => {
    switch (difficulty) {
      case 'BEGINNER': return 'bg-emerald-50 text-emerald-700 border-emerald-100';
      case 'INTERMEDIATE': return 'bg-amber-50 text-amber-700 border-amber-100';
      case 'ADVANCED': return 'bg-rose-50 text-rose-700 border-rose-100';
      default: return 'bg-gray-50 text-gray-700 border-gray-100';
    }
  };

  return (
    <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-xs hover:shadow-md transition-all duration-300 flex flex-col justify-between h-full group">
      <div>
        {/* Meta Info */}
        <div className="flex flex-wrap items-center gap-2 mb-4">
          <span className="text-xs font-semibold px-2.5 py-1 bg-gray-50 border border-gray-100 text-gray-600 rounded-lg">
            {course.category}
          </span>
          <span className={`text-xs font-semibold px-2.5 py-1 border rounded-lg ${getDifficultyColor(course.difficulty)}`}>
            {course.difficulty}
          </span>
        </div>

        {/* Title */}
        <h3 className="text-lg font-bold text-gray-900 group-hover:text-indigo-600 transition duration-200 line-clamp-1">
          {course.title}
        </h3>
        <p className="mt-1 text-sm text-gray-500 line-clamp-2 h-10">
          {course.description || 'Learn and track your learning progress systematically.'}
        </p>

        {/* Progress Section */}
        <div className="mt-6">
          <div className="flex items-center justify-between text-sm font-semibold text-gray-700 mb-2">
            <span>Progress</span>
            <span className="text-indigo-600">{course.progressPercentage?.toFixed(0)}%</span>
          </div>
          <div className="w-full h-2.5 bg-gray-100 rounded-full overflow-hidden">
            <div 
              className="h-full bg-indigo-600 rounded-full transition-all duration-500 ease-out"
              style={{ width: `${course.progressPercentage}%` }}
            />
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-4 mt-6 border-t border-b border-gray-50 py-4 mb-6">
          <div className="flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-gray-400 shrink-0" />
            <div className="overflow-hidden">
              <span className="block text-xs font-medium text-gray-400">Lessons</span>
              <span className="text-sm font-bold text-gray-800">
                {course.completedLessons} / {course.totalLessons}
              </span>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-gray-400 shrink-0" />
            <div className="overflow-hidden">
              <span className="block text-xs font-medium text-gray-400">Time Spent</span>
              <span className="text-sm font-bold text-gray-800">
                {course.timeSpentMinutes || 0} mins
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Action Button */}
      <button
        onClick={() => navigate(`/student/courses/${course.id}`)}
        className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-indigo-550 hover:bg-indigo-600 text-white rounded-xl text-sm font-semibold transition cursor-pointer"
      >
        Continue Learning
        <ArrowRight className="w-4 h-4" />
      </button>
    </div>
  );
};

export default CourseProgressCard;
