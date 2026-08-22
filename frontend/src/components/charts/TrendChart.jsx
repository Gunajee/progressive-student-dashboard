import React from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const TrendChart = ({ data, range, onRangeChange }) => {
  const formatXAxis = (tickItem) => {
    try {
      const date = new Date(tickItem);
      return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    } catch (e) {
      return tickItem;
    }
  };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-900 border border-slate-800 text-white p-3 rounded-xl shadow-lg text-xs">
          <p className="font-bold mb-1">{formatXAxis(label)}</p>
          <p className="text-indigo-400 font-semibold">{payload[0].value} minutes spent</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs h-full">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-base font-bold text-gray-900">Learning Activity</h3>
          <p className="text-xs text-gray-500 font-medium">Daily study time distribution</p>
        </div>

        {/* Range Buttons */}
        <div className="flex bg-gray-50 border border-gray-100 p-0.5 rounded-xl">
          <button
            onClick={() => onRangeChange('7d')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
              range === '7d'
                ? 'bg-white text-indigo-650 shadow-xs'
                : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            7 Days
          </button>
          <button
            onClick={() => onRangeChange('30d')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
              range === '30d'
                ? 'bg-white text-indigo-650 shadow-xs'
                : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            30 Days
          </button>
        </div>
      </div>

      <div className="h-64 w-full">
        {data && data.length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart
              data={data}
              margin={{ top: 10, right: 10, left: -25, bottom: 0 }}
            >
              <defs>
                <linearGradient id="colorMinutes" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.2} />
                  <stop offset="95%" stopColor="#4f46e5" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
              <XAxis 
                dataKey="date" 
                tickFormatter={formatXAxis} 
                tick={{ fill: '#94a3b8', fontSize: 10, fontWeight: 500 }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis 
                tick={{ fill: '#94a3b8', fontSize: 10, fontWeight: 500 }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<CustomTooltip />} />
              <Area
                type="monotone"
                dataKey="minutes"
                stroke="#4f46e5"
                strokeWidth={2.5}
                fillOpacity={1}
                fill="url(#colorMinutes)"
              />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-full flex items-center justify-center text-sm text-gray-500 font-medium">
            No activity events recorded in this range.
          </div>
        )}
      </div>
    </div>
  );
};

export default TrendChart;
