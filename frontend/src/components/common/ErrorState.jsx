import React from 'react';
import { AlertCircle, RotateCcw } from 'lucide-react';

const ErrorState = ({ message = 'An error occurred while fetching data.', onRetry }) => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[300px] p-8 text-center bg-red-50/30 border border-red-100/50 rounded-2xl max-w-lg mx-auto my-6">
      <AlertCircle className="w-12 h-12 text-red-500" />
      <h3 className="mt-4 text-lg font-semibold text-gray-900">Oops, something went wrong</h3>
      <p className="mt-2 text-sm text-gray-600 max-w-sm">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-6 inline-flex items-center gap-2 px-4 py-2 bg-white hover:bg-gray-50 text-gray-700 border border-gray-200 rounded-xl shadow-xs text-sm font-medium transition cursor-pointer"
        >
          <RotateCcw className="w-4 h-4" />
          Try Again
        </button>
      )}
    </div>
  );
};

export default ErrorState;
