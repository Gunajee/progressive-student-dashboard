import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import LoadingState from './LoadingState';

export const ProtectedRoute = ({ allowedRoles }) => {
  const { currentUser, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <LoadingState message="Authenticating session..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
    if (currentUser.role === 'STUDENT') {
      return <Navigate to="/student/dashboard" replace />;
    } else if (currentUser.role === 'MENTOR') {
      return <Navigate to="/mentor/dashboard" replace />;
    } else if (currentUser.role === 'ADMIN') {
      return <Navigate to="/admin/dashboard" replace />;
    }
  }

  return <Outlet />;
};

export const PublicRoute = () => {
  const { currentUser, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <LoadingState message="Restoring session..." />
      </div>
    );
  }

  if (isAuthenticated) {
    if (currentUser.role === 'STUDENT') {
      return <Navigate to="/student/dashboard" replace />;
    } else if (currentUser.role === 'MENTOR') {
      return <Navigate to="/mentor/dashboard" replace />;
    } else if (currentUser.role === 'ADMIN') {
      return <Navigate to="/admin/dashboard" replace />;
    }
  }

  return <Outlet />;
};
