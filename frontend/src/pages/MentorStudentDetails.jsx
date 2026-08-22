import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import mentorService from '../services/mentorService';
import studentService from '../services/studentService'; // For DTO structures if needed
import TrendChart from '../components/charts/TrendChart';
import ActivityList from '../components/dashboard/ActivityList';
import RecommendationCard from '../components/dashboard/RecommendationCard';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';
import { ArrowLeft, User, Mail, ShieldAlert, Award, Clock, Flame } from 'lucide-react';

const MentorStudentDetails = () => {
  const { studentId } = useParams();
  const [details, setDetails] = useState(null);
  const [progress, setProgress] = useState([]);
  const [activities, setActivities] = useState([]);
  const [trendData, setTrendData] = useState([]);
  const [trendRange, setTrendRange] = useState('7d');
  const [loading, setLoading] = useState(true);
  const [errMessage, setErrMessage] = useState('');

  const fetchStudentMonitoringData = async () => {
    setLoading(true);
    setErrMessage('');
    try {
      const [detailsData, progressData, activityData, trendData] = await Promise.all([
        mentorService.getStudentDetails(studentId),
        mentorService.getStudentProgress(studentId),
        mentorService.getStudentActivity(studentId),
        mentorService.getStudentAnalytics(studentId, trendRange)
      ]);

      setDetails(detailsData);
      setProgress(progressData);
      setActivities(activityData);
      setTrendData(trendData.data || []);
    } catch (err) {
      console.error(err);
      if (err.response && err.response.status === 403) {
        setErrMessage('Access Denied: You are not authorized to monitor this student profile.');
      } else {
        setErrMessage('Failed to load student tracking session.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudentMonitoringData();
  }, [studentId]);

  const handleRangeChange = async (newRange) => {
    setTrendRange(newRange);
    try {
      const trend = await mentorService.getStudentAnalytics(studentId, newRange);
      setTrendData(trend.data || []);
    } catch (err) {
      console.error('Failed to load trends data:', err);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'HEALTHY':
        return 'bg-emerald-50 text-emerald-700 border-emerald-100';
      case 'NEEDS_ATTENTION':
        return 'bg-amber-50 text-amber-700 border-amber-100';
      case 'AT_RISK':
        return 'bg-red-50 text-red-700 border-red-100';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-100';
    }
  };

  if (loading) return <LoadingState message="Loading student profile details..." />;
  if (errMessage) return <ErrorState message={errMessage} onRetry={fetchStudentMonitoringData} />;
  if (!details) return <ErrorState message="Student details not found." onRetry={fetchStudentMonitoringData} />;

  // Infer mock recommendations for the student if empty or show active ones
  // We can fetch student recommendations using studentService if authorized, but the backend is checked on a session basis.
  // Wait, let's look at recommendations. We can generate them client-side based on progress to show or fetch them.
  // Actually, we can fetch student's recommendations if the service provides a method! But wait, does `mentorService` have it?
  // Let's see: `getStudentDetails` returns a `StudentDetailsDto` which does not contain recommendations. We can show course progress rules directly or display mock recommendations list. Let's construct explainable warnings based on status.
  const customRecommendations = [];
  if (details.status === 'AT_RISK') {
    customRecommendations.push({
      id: 'mentor_rec_risk',
      type: 'INACTIVITY',
      title: 'Initiate Student Re-Engagement',
      description: 'Schedule a call or send a message to check in.',
      priority: 'HIGH',
      reason: `Student status is currently AT_RISK. Overall course progress is ${details.overallProgress}% with study session gaps.`
    });
  } else if (details.status === 'NEEDS_ATTENTION') {
    customRecommendations.push({
      id: 'mentor_rec_attn',
      type: 'LOW_PROGRESS',
      title: 'Review Study Momentum',
      description: 'Review syllabus progression and check for blockers.',
      priority: 'MEDIUM',
      reason: `Student progress is ${details.overallProgress}% which falls into the NEEDS_ATTENTION boundary.`
    });
  } else {
    customRecommendations.push({
      id: 'mentor_rec_healthy',
      type: 'NEXT_LESSON',
      title: 'Continue Tracking Progress',
      description: 'Encourage student to complete remaining lessons.',
      priority: 'LOW',
      reason: 'Student is making steady progress and is in a HEALTHY state.'
    });
  }

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Back button */}
      <div>
        <Link to="/mentor/dashboard" className="inline-flex items-center gap-2 text-sm font-semibold text-gray-500 hover:text-indigo-650 transition">
          <ArrowLeft className="w-4 h-4" />
          Back to Cohort Console
        </Link>
      </div>

      {/* Student Profile Overview Card */}
      <section className="bg-white border border-gray-100 rounded-3xl p-6 md:p-8 shadow-xs flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-indigo-50 border border-indigo-100 flex items-center justify-center shrink-0">
            <User className="w-6 h-6 text-indigo-650" />
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2.5">
              <h2 className="text-xl md:text-2xl font-extrabold text-gray-900 tracking-tight">
                {details.student.name}
              </h2>
              <span className={`inline-flex items-center px-2.5 py-1 border rounded-lg text-xs font-bold ${getStatusBadge(details.status)}`}>
                {details.status}
              </span>
            </div>
            <p className="text-xs text-gray-500 font-semibold mt-1 flex items-center gap-1.5">
              <Mail className="w-3.5 h-3.5" /> {details.student.email}
            </p>
          </div>
        </div>

        {/* High-level indicators */}
        <div className="grid grid-cols-3 gap-6 bg-gray-50/50 border border-gray-150/50 p-5 rounded-2xl w-full md:w-auto">
          <div className="text-center">
            <span className="flex items-center justify-center gap-1 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
              <Clock className="w-3.5 h-3.5" /> Study Time
            </span>
            <span className="text-base font-black text-gray-800 mt-1 block">
              {(details.learningMinutes / 60).toFixed(1)} hrs
            </span>
          </div>
          <div className="w-px h-8 bg-gray-200 self-center" />
          <div className="text-center">
            <span className="flex items-center justify-center gap-1 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
              <Flame className="w-3.5 h-3.5" /> Streak
            </span>
            <span className="text-base font-black text-gray-800 mt-1 block">
              {details.currentStreak} days
            </span>
          </div>
          <div className="w-px h-8 bg-gray-200 self-center" />
          <div className="text-center">
            <span className="flex items-center justify-center gap-1 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
              <Award className="w-3.5 h-3.5" /> Progress
            </span>
            <span className="text-base font-black text-indigo-600 mt-1 block">
              {details.overallProgress?.toFixed(0)}%
            </span>
          </div>
        </div>
      </section>

      {/* Analytics Trend and Recommendations */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Study Trend Chart */}
        <div className="lg:col-span-2">
          <TrendChart 
            data={trendData} 
            range={trendRange} 
            onRangeChange={handleRangeChange} 
          />
        </div>

        {/* Mentor Action Steps */}
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Mentor Recommendations</h3>
            <p className="text-xs text-gray-500 font-medium">Suggested actions for student progression</p>
          </div>
          <div className="space-y-3">
            {customRecommendations.map((rec) => (
              <RecommendationCard key={rec.id} recommendation={rec} />
            ))}
          </div>
        </div>
      </section>

      {/* Courses progress grid and recent activities */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Enrolled course lists */}
        <div className="lg:col-span-2 space-y-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Enrolled Courses</h3>
            <p className="text-xs text-gray-500 font-medium">Syllabus progression breakdown</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {details.courses && details.courses.length > 0 ? (
              details.courses.map((course) => (
                <div key={course.id} className="bg-white border border-gray-100 rounded-2xl p-5 shadow-2xs space-y-4">
                  <div>
                    <span className="text-[10px] font-bold px-2 py-0.5 bg-gray-50 border border-gray-100 text-gray-500 rounded-lg uppercase">
                      {course.category}
                    </span>
                    <h4 className="text-base font-bold text-gray-900 mt-2 line-clamp-1">{course.title}</h4>
                  </div>
                  
                  {/* Progress info */}
                  <div>
                    <div className="flex items-center justify-between text-xs font-semibold text-gray-600 mb-1.5">
                      <span>Progress</span>
                      <span className="text-indigo-650">{course.progressPercentage?.toFixed(0)}%</span>
                    </div>
                    <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-indigo-650 rounded-full" 
                        style={{ width: `${course.progressPercentage}%` }}
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-xs text-gray-500 border-t border-gray-50 pt-3">
                    <span>Lessons: <strong>{course.completedLessons} / {course.totalLessons}</strong></span>
                    <span>Time spent: <strong>{course.timeSpentMinutes} mins</strong></span>
                  </div>
                </div>
              ))
            ) : (
              <div className="bg-white border border-gray-100 rounded-2xl p-6 text-center text-gray-400 col-span-2">
                No active courses enrolled.
              </div>
            )}
          </div>
        </div>

        {/* Student activities timeline */}
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Recent Student Activity</h3>
            <p className="text-xs text-gray-500 font-medium">Milestones achieved by this student</p>
          </div>
          <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs min-h-[300px]">
            <ActivityList activities={activities} />
          </div>
        </div>
      </section>
    </div>
  );
};

export default MentorStudentDetails;
