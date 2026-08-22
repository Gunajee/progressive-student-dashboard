import React, { useState, useEffect } from 'react';
import studentService from '../services/studentService';
import CourseProgressCard from '../components/courses/CourseProgressCard';
import RecommendationCard from '../components/dashboard/RecommendationCard';
import ActivityList from '../components/dashboard/ActivityList';
import TrendChart from '../components/charts/TrendChart';
import DistributionChart from '../components/charts/DistributionChart';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';
import { BookOpen, Clock, Award, Flame, Library, Play } from 'lucide-react';

const StudentDashboard = () => {
  const [dashboardData, setDashboardData] = useState(null);
  const [trendData, setTrendData] = useState([]);
  const [trendRange, setTrendRange] = useState('7d');
  const [distributionData, setDistributionData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [errMessage, setErrMessage] = useState('');

  const fetchDashboardData = async () => {
    setLoading(true);
    setErrMessage('');
    try {
      // Parallel fetches for fast initialization
      const [dash, trend, dist] = await Promise.all([
        studentService.getDashboard(),
        studentService.getLearningTrend(trendRange),
        studentService.getCourseDistribution(),
      ]);

      setDashboardData(dash);
      setTrendData(trend.data || []);
      setDistributionData(dist);
    } catch (err) {
      console.error(err);
      setErrMessage(err.response?.data?.message || 'Failed to load dashboard. Please reload page.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  // Fetch only trends when range is switched
  const handleRangeChange = async (newRange) => {
    setTrendRange(newRange);
    try {
      const trend = await studentService.getLearningTrend(newRange);
      setTrendData(trend.data || []);
    } catch (err) {
      console.error('Failed to load trends data:', err);
    }
  };

  if (loading) {
    return <LoadingState message="Loading your dashboard analytics..." />;
  }

  if (errMessage) {
    return <ErrorState message={errMessage} onRetry={fetchDashboardData} />;
  }

  if (!dashboardData) {
    return <ErrorState message="Dashboard data could not be recovered." onRetry={fetchDashboardData} />;
  }

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* 1. Summary Cards Grid */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6">
        {/* Streak card */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-amber-50 rounded-2xl text-amber-600 shadow-xs ring-4 ring-amber-50/50 shrink-0">
            <Flame className="w-6 h-6 animate-pulse text-orange-500 fill-orange-500" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Learning Streak</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              🔥 {dashboardData.currentStreak} Day{dashboardData.currentStreak === 1 ? '' : 's'}
            </h3>
          </div>
        </div>

        {/* Total Courses */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-indigo-50 rounded-2xl text-indigo-650 shadow-xs ring-4 ring-indigo-50/50 shrink-0">
            <Library className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Enrolled Courses</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {dashboardData.totalCourses}
            </h3>
          </div>
        </div>

        {/* Completed Lessons */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-emerald-50 rounded-2xl text-emerald-600 shadow-xs ring-4 ring-emerald-50/50 shrink-0">
            <Award className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Lessons Finished</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {dashboardData.completedLessons}
            </h3>
          </div>
        </div>

        {/* Learning Time */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-blue-50 rounded-2xl text-blue-600 shadow-xs ring-4 ring-blue-50/50 shrink-0">
            <Clock className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Study Time</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {(dashboardData.totalLearningMinutes / 60).toFixed(1)} hrs
            </h3>
          </div>
        </div>

        {/* Overall Progress */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-violet-50 rounded-2xl text-violet-600 shadow-xs ring-4 ring-violet-50/50 shrink-0">
            <Play className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Overall Progress</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {dashboardData.overallProgress?.toFixed(1)}%
            </h3>
          </div>
        </div>
      </section>

      {/* 2. Charts Section */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <TrendChart 
            data={trendData} 
            range={trendRange} 
            onRangeChange={handleRangeChange} 
          />
        </div>
        <div>
          <DistributionChart data={distributionData} />
        </div>
      </section>

      {/* 3. Recommendations Row */}
      {dashboardData.recommendations && dashboardData.recommendations.length > 0 && (
        <section className="space-y-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Recommended For You</h3>
            <p className="text-xs text-gray-500 font-medium">Explainable, AI-free learning adjustments</p>
          </div>
          <div className="space-y-3">
            {dashboardData.recommendations.slice(0, 3).map((rec) => (
              <RecommendationCard key={rec.id} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* 4. Two-Column Courses and Activity Section */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Enrolled Courses Progress Cards */}
        <div className="lg:col-span-2 space-y-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900">My Active Courses</h3>
            <p className="text-xs text-gray-500 font-medium">Overview of enrolled course performance</p>
          </div>
          {dashboardData.courseProgress && dashboardData.courseProgress.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              {dashboardData.courseProgress.map((course) => (
                <CourseProgressCard key={course.id} course={course} />
              ))}
            </div>
          ) : (
            <div className="bg-white border border-gray-150 rounded-2xl p-8 text-center text-gray-500">
              You are not enrolled in any courses yet. Please contact your mentor to enroll you.
            </div>
          )}
        </div>

        {/* Recent Activity Timeline */}
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Recent Activity</h3>
            <p className="text-xs text-gray-500 font-medium">Your study and completion milestones</p>
          </div>
          <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs h-[calc(100%-36px)] min-h-[300px]">
            <ActivityList activities={dashboardData.recentActivity} />
          </div>
        </div>
      </section>
    </div>
  );
};

export default StudentDashboard;
