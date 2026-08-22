import React, { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

const MainLayout = () => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const location = useLocation();

  // Dynamically resolve page titles based on routes
  const getPageTitle = () => {
    const path = location.pathname;
    if (path.startsWith('/student/dashboard')) return 'My Dashboard';
    if (path.startsWith('/student/courses')) return 'My Courses';
    if (path.startsWith('/mentor/dashboard')) return 'Mentor Console';
    if (path.startsWith('/mentor/students')) return 'Student Profile Details';
    return 'Dashboard';
  };

  return (
    <div className="min-h-screen flex bg-gray-50/50">
      {/* Sidebar navigation */}
      <Sidebar isOpen={isSidebarOpen} onClose={() => setIsSidebarOpen(false)} />

      {/* Main viewport */}
      <div className="flex-1 flex flex-col min-w-0">
        <Header onMenuToggle={() => setIsSidebarOpen(true)} title={getPageTitle()} />

        {/* Content container */}
        <main className="flex-1 p-6 md:p-8 overflow-y-auto max-w-7xl w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
