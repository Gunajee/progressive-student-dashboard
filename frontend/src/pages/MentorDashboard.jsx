import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import mentorService from '../services/mentorService';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';
import { Users, UserCheck, Play, ShieldAlert, Download, Search, Filter, ChevronLeft, ChevronRight, CheckCircle, AlertCircle } from 'lucide-react';

const MentorDashboard = () => {
  const [stats, setStats] = useState(null);
  const [studentsPage, setStudentsPage] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [errMessage, setErrMessage] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [csvLoading, setCsvLoading] = useState(false);
  const [toast, setToast] = useState({ show: false, message: '', type: 'success' });

  const navigate = useNavigate();

  const showToast = (message, type = 'success') => {
    setToast({ show: true, message, type });
    setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 4000);
  };

  const fetchDashboardData = async () => {
    setLoading(true);
    setErrMessage('');
    try {
      const [statsData, studentsData] = await Promise.all([
        mentorService.getDashboard(),
        mentorService.getStudents(page, 10),
      ]);
      setStats(statsData);
      setStudentsPage(studentsData);
    } catch (err) {
      console.error(err);
      setErrMessage(err.response?.data?.message || 'Failed to load mentor dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, [page]);

  const handleExportCsv = async () => {
    setCsvLoading(true);
    try {
      await mentorService.exportStudentsCsv();
      showToast('Student analytics exported to CSV successfully!');
    } catch (err) {
      showToast('Failed to export students CSV file.', 'error');
    } finally {
      setCsvLoading(false);
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

  if (loading) return <LoadingState message="Loading cohort data analytics..." />;
  if (errMessage) return <ErrorState message={errMessage} onRetry={fetchDashboardData} />;

  // Filter students locally based on search term and status filter
  const studentsList = studentsPage?.content || [];
  const filteredStudents = studentsList.filter(s => {
    const matchesSearch = s.student.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          s.student.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || s.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-8 animate-fadeIn relative">
      {/* Toast Notice Banner */}
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

      {/* Header Actions row */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-gray-900 tracking-tight">Assigned Student Cohort</h2>
          <p className="text-xs text-gray-500 font-medium">Overview of learning risk matrices</p>
        </div>

        <button
          onClick={handleExportCsv}
          disabled={csvLoading}
          className="flex items-center justify-center gap-2 py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl text-sm font-semibold shadow-xs transition cursor-pointer"
        >
          <Download className="w-4 h-4" />
          {csvLoading ? 'Exporting...' : 'Export CSV'}
        </button>
      </div>

      {/* 1. Summary statistics grid */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Students */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-indigo-50 rounded-2xl text-indigo-650 shadow-xs ring-4 ring-indigo-50/50 shrink-0">
            <Users className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Total Students</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {stats?.totalStudents || 0}
            </h3>
          </div>
        </div>

        {/* Active Students */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-emerald-50 rounded-2xl text-emerald-600 shadow-xs ring-4 ring-emerald-50/50 shrink-0">
            <UserCheck className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Active Students</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {stats?.activeStudents || 0}
            </h3>
          </div>
        </div>

        {/* Average Progress */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-violet-50 rounded-2xl text-violet-600 shadow-xs ring-4 ring-violet-50/50 shrink-0">
            <Play className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">Average Progress</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {stats?.averageProgress?.toFixed(1) || 0.0}%
            </h3>
          </div>
        </div>

        {/* At Risk */}
        <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs flex items-center gap-4 hover:shadow-xs transition duration-200">
          <div className="p-3.5 bg-red-50 rounded-2xl text-red-600 shadow-xs ring-4 ring-red-50/50 shrink-0">
            <ShieldAlert className="w-6 h-6" />
          </div>
          <div className="overflow-hidden">
            <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider">At Risk</span>
            <h3 className="text-xl font-black text-gray-900 mt-0.5 truncate">
              {stats?.atRiskStudents || 0}
            </h3>
          </div>
        </div>
      </section>

      {/* 2. Filters & Searches */}
      <section className="bg-white border border-gray-100 rounded-2xl p-4 flex flex-col md:flex-row gap-4 justify-between items-center shadow-2xs">
        {/* Search */}
        <div className="relative w-full md:w-80">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            placeholder="Search student or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="block w-full pl-9 pr-4 py-2 border border-gray-200 rounded-xl bg-gray-50/50 focus:bg-white text-sm focus:outline-hidden focus:ring-1 focus:ring-indigo-600 focus:border-indigo-600"
          />
        </div>

        {/* Filter dropdown */}
        <div className="flex items-center gap-2 w-full md:w-auto self-start md:self-center">
          <Filter className="w-4 h-4 text-gray-400" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="block w-full md:w-44 py-2 px-3 border border-gray-200 bg-white rounded-xl text-sm focus:outline-hidden focus:ring-1 focus:ring-indigo-600 focus:border-indigo-600"
          >
            <option value="ALL">All Risk Statuses</option>
            <option value="HEALTHY">Healthy</option>
            <option value="NEEDS_ATTENTION">Needs Attention</option>
            <option value="AT_RISK">At Risk</option>
          </select>
        </div>
      </section>

      {/* 3. Student Table */}
      <section className="bg-white border border-gray-100 rounded-2xl shadow-2xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-100 text-left">
            <thead className="bg-gray-550/5 text-xs font-bold text-gray-400 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4">Student</th>
                <th className="px-6 py-4">Enrolled Courses</th>
                <th className="px-6 py-4">Average Progress</th>
                <th className="px-6 py-4">Learning Time</th>
                <th className="px-6 py-4">Last Active</th>
                <th className="px-6 py-4">Risk Status</th>
                <th className="px-6 py-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 text-sm font-semibold text-gray-700">
              {filteredStudents.length > 0 ? (
                filteredStudents.map((summary) => (
                  <tr key={summary.student.id} className="hover:bg-gray-50/30 transition">
                    {/* Student Info */}
                    <td className="px-6 py-4">
                      <div>
                        <div className="text-gray-900 font-bold">{summary.student.name}</div>
                        <div className="text-xs text-gray-400 font-medium">{summary.student.email}</div>
                      </div>
                    </td>

                    {/* Courses Count */}
                    <td className="px-6 py-4 text-gray-650">{summary.courseCount} courses</td>

                    {/* Progress */}
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <span className="w-10 text-right">{summary.overallProgress?.toFixed(0)}%</span>
                        <div className="w-16 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                          <div 
                            className="h-full bg-indigo-650 rounded-full" 
                            style={{ width: `${summary.overallProgress}%` }}
                          />
                        </div>
                      </div>
                    </td>

                    {/* Learning Hours */}
                    <td className="px-6 py-4 text-gray-650">{(summary.learningMinutes / 60).toFixed(1)} hrs</td>

                    {/* Last Active */}
                    <td className="px-6 py-4 text-gray-500 font-medium">
                      {summary.lastActive ? new Date(summary.lastActive).toLocaleDateString() : 'Never'}
                    </td>

                    {/* Risk Status */}
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2.5 py-1 border rounded-lg text-xs font-bold ${getStatusBadge(summary.status)}`}>
                        {summary.status.replace('_', ' ')}
                      </span>
                    </td>

                    {/* Action button */}
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => navigate(`/mentor/students/${summary.student.id}`)}
                        className="text-xs font-bold px-3 py-1.5 border border-gray-200 hover:border-indigo-200 hover:bg-indigo-50/20 text-indigo-600 rounded-lg shadow-3xs transition cursor-pointer"
                      >
                        Monitor Profile
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-gray-400 font-medium">
                    No matching students found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination controls */}
        {studentsPage && studentsPage.totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-4 border-t border-gray-100 bg-gray-50/30">
            <span className="text-xs text-gray-400 font-bold">
              Page {page + 1} of {studentsPage.totalPages}
            </span>
            <div className="flex items-center gap-1.5">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="p-1.5 border border-gray-250 bg-white rounded-lg hover:bg-gray-50 disabled:opacity-50 transition cursor-pointer"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                disabled={page === studentsPage.totalPages - 1}
                onClick={() => setPage(page + 1)}
                className="p-1.5 border border-gray-250 bg-white rounded-lg hover:bg-gray-50 disabled:opacity-50 transition cursor-pointer"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
};

export default MentorDashboard;
