import React from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, ArrowRight, CheckCircle, Zap } from 'lucide-react';

const RecommendationCard = ({ recommendation }) => {
  const navigate = useNavigate();

  const getPriorityStyles = (priority) => {
    switch (priority) {
      case 'HIGH':
        return {
          border: 'border-red-100 hover:border-red-200 bg-red-50/10',
          badge: 'bg-red-50 text-red-700 border-red-100',
          icon: <AlertCircle className="w-5 h-5 text-red-500" />,
        };
      case 'MEDIUM':
        return {
          border: 'border-amber-100 hover:border-amber-200 bg-amber-50/10',
          badge: 'bg-amber-50 text-amber-700 border-amber-100',
          icon: <Zap className="w-5 h-5 text-amber-500" />,
        };
      case 'LOW':
        return {
          border: 'border-blue-100 hover:border-blue-200 bg-blue-50/10',
          badge: 'bg-blue-50 text-blue-700 border-blue-100',
          icon: <CheckCircle className="w-5 h-5 text-blue-500" />,
        };
      default:
        return {
          border: 'border-gray-100 hover:border-gray-200 bg-white',
          badge: 'bg-gray-50 text-gray-700 border-gray-100',
          icon: <CheckCircle className="w-5 h-5 text-gray-500" />,
        };
    }
  };

  const styles = getPriorityStyles(recommendation.priority);

  const handleAction = () => {
    if (recommendation.courseId) {
      navigate(`/student/courses/${recommendation.courseId}`);
    } else {
      navigate('/student/courses');
    }
  };

  return (
    <div className={`border rounded-2xl p-5 transition-all duration-300 ${styles.border} flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-2xs`}>
      <div className="flex items-start gap-3.5">
        <div className="mt-0.5 shrink-0">
          {styles.icon}
        </div>
        <div>
          <div className="flex flex-wrap items-center gap-2 mb-1.5">
            <h4 className="text-base font-bold text-gray-900 leading-tight">
              {recommendation.title}
            </h4>
            <span className={`text-[10px] font-bold px-2 py-0.5 border rounded-full uppercase tracking-wider ${styles.badge}`}>
              {recommendation.priority}
            </span>
          </div>
          <p className="text-sm font-semibold text-indigo-650 leading-relaxed">
            {recommendation.description}
          </p>
          <p className="mt-1 text-xs text-gray-500 leading-relaxed font-medium">
            Reason: {recommendation.reason}
          </p>
        </div>
      </div>

      <button
        onClick={handleAction}
        className="shrink-0 flex items-center justify-center gap-2 px-4 py-2.5 bg-white border border-gray-200 hover:border-indigo-200 hover:bg-indigo-50/30 text-indigo-600 rounded-xl text-sm font-semibold shadow-2xs transition cursor-pointer"
      >
        Take Action
        <ArrowRight className="w-4 h-4" />
      </button>
    </div>
  );
};

export default RecommendationCard;
