import React from 'react';
import { BookOpen, Award, CheckCircle, Clock } from 'lucide-react';

const ActivityList = ({ activities }) => {
  const getEventIcon = (type) => {
    switch (type) {
      case 'LESSON_COMPLETE':
        return <Award className="w-4 h-4 text-emerald-600" />;
      case 'STUDY_SESSION':
        return <Clock className="w-4 h-4 text-indigo-600" />;
      default:
        return <BookOpen className="w-4 h-4 text-blue-600" />;
    }
  };

  const getEventColor = (type) => {
    switch (type) {
      case 'LESSON_COMPLETE':
        return 'bg-emerald-550/10 text-emerald-700';
      case 'STUDY_SESSION':
        return 'bg-indigo-550/10 text-indigo-700';
      default:
        return 'bg-blue-550/10 text-blue-700';
    }
  };

  const formatEventDate = (dateString) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString(undefined, { 
        month: 'short', 
        day: 'numeric', 
        hour: '2-digit', 
        minute: '2-digit' 
      });
    } catch (e) {
      return dateString;
    }
  };

  if (!activities || activities.length === 0) {
    return (
      <div className="text-center py-6 text-gray-500 text-sm font-medium">
        No recent activities logged yet. Start studying to log your progress!
      </div>
    );
  }

  return (
    <div className="flow-root">
      <ul className="-mb-8">
        {activities.map((activity, idx) => (
          <li key={activity.id || idx}>
            <div className="relative pb-8">
              {/* Connector line */}
              {idx !== activities.length - 1 && (
                <span 
                  className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-150" 
                  aria-hidden="true" 
                />
              )}
              
              <div className="relative flex space-x-3">
                <div>
                  <span className={`h-8 w-8 rounded-full flex items-center justify-center ring-8 ring-white ${getEventColor(activity.eventType)}`}>
                    {getEventIcon(activity.eventType)}
                  </span>
                </div>
                
                <div className="flex-1 min-w-0 pt-1.5 flex justify-between space-x-4">
                  <div>
                    <p className="text-sm font-semibold text-gray-800">
                      {activity.eventType === 'LESSON_COMPLETE' ? 'Completed lesson' : 'Logged study time'}:{' '}
                      <span className="font-bold text-gray-900">
                        {activity.lessonTitle || 'Lesson'}
                      </span>
                    </p>
                    <p className="text-xs text-gray-500 font-semibold mt-0.5">
                      Course: {activity.courseTitle} • {activity.durationMinutes} mins spent
                    </p>
                  </div>
                  
                  <div className="text-right text-xs whitespace-nowrap text-gray-400 font-semibold">
                    <time dateTime={activity.eventDate}>
                      {formatEventDate(activity.eventDate)}
                    </time>
                  </div>
                </div>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ActivityList;
