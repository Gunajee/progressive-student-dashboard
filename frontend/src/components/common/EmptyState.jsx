import React from 'react';
import { Inbox } from 'lucide-react';

const EmptyState = ({ title = 'No data available', message = 'There is nothing to display here yet.', action }) => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[300px] p-8 text-center bg-gray-50/50 border border-gray-100 rounded-2xl">
      <Inbox className="w-12 h-12 text-gray-400" />
      <h3 className="mt-4 text-lg font-semibold text-gray-900">{title}</h3>
      <p className="mt-2 text-sm text-gray-500 max-w-sm">{message}</p>
      {action && (
        <div className="mt-6">
          {action}
        </div>
      )}
    </div>
  );
};

export default EmptyState;
