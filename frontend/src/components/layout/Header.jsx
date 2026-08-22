import React from 'react';
import { Menu, LogOut, GraduationCap } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const Header = ({ onMenuToggle, title = 'Student Dashboard' }) => {
  const { currentUser, logout } = useAuth();

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between h-16 px-6 bg-white border-b border-gray-100 shadow-xs">
      <div className="flex items-center gap-3">
        {/* Toggle Sidebar Button for Mobile */}
        <button
          onClick={onMenuToggle}
          className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-50 rounded-xl md:hidden transition"
        >
          <Menu className="w-5 h-5" />
        </button>

        <h1 className="text-xl font-bold text-gray-900 tracking-tight">{title}</h1>
      </div>

      <div className="flex items-center gap-4">
        {/* User Info (Desktop only) */}
        <div className="hidden sm:flex flex-col text-right">
          <span className="text-sm font-semibold text-gray-900 leading-tight">{currentUser?.name}</span>
          <span className="text-xs font-medium text-gray-500">{currentUser?.email}</span>
        </div>

        {/* Logout Button */}
        <button
          onClick={logout}
          title="Log Out"
          className="p-2.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition cursor-pointer"
        >
          <LogOut className="w-5 h-5" />
        </button>
      </div>
    </header>
  );
};

export default Header;
