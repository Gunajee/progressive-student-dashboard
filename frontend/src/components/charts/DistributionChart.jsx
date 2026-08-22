import React from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const DistributionChart = ({ data }) => {
  // Expected structure: { completed, inProgress, notStarted }
  const chartData = [
    { name: 'Completed', value: data?.completed || 0, color: '#10b981' },
    { name: 'In Progress', value: data?.inProgress || 0, color: '#f59e0b' },
    { name: 'Not Started', value: data?.notStarted || 0, color: '#94a3b8' },
  ].filter(item => item.value > 0); // Only show statuses with non-zero counts

  const hasData = chartData.length > 0;

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-900 border border-slate-800 text-white p-3 rounded-xl shadow-lg text-xs">
          <p className="font-bold mb-0.5">{payload[0].name}</p>
          <p className="text-indigo-400 font-semibold">{payload[0].value} {payload[0].value === 1 ? 'course' : 'courses'}</p>
        </div>
      );
    }
    return null;
  };

  const renderLegend = (props) => {
    const { payload } = props;
    return (
      <div className="flex flex-wrap justify-center gap-x-4 gap-y-2 mt-4">
        {payload.map((entry, index) => (
          <div key={`item-${index}`} className="flex items-center gap-1.5 text-xs font-semibold text-gray-600">
            <span 
              className="w-2.5 h-2.5 rounded-full shrink-0" 
              style={{ backgroundColor: entry.color }} 
            />
            <span>{entry.value} ({entry.payload.value})</span>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-2xs h-full flex flex-col justify-between">
      <div>
        <h3 className="text-base font-bold text-gray-900">Course Distribution</h3>
        <p className="text-xs text-gray-500 font-medium">Completion status overview</p>
      </div>

      <div className="h-48 w-full flex items-center justify-center mt-4">
        {hasData ? (
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={55}
                outerRadius={75}
                paddingAngle={4}
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
              <Legend content={renderLegend} />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-center py-6 text-sm text-gray-400 font-semibold">
            No enrolled courses to display distribution.
          </div>
        )}
      </div>
    </div>
  );
};

export default DistributionChart;
