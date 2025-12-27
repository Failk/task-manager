import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { format, startOfWeek, endOfWeek, eachDayOfInterval, isSameDay, addDays, subDays } from 'date-fns';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import calendarService from '../services/calendar.service';

const Calendar = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [view, setView] = useState('WEEKLY');
  const [calendarData, setCalendarData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCalendarData();
  }, [currentDate, view]);

  const loadCalendarData = async () => {
    try {
      setLoading(true);
      const dateStr = format(currentDate, 'yyyy-MM-dd');

      let data;
      switch (view) {
        case 'DAILY':
          data = await calendarService.getDailyView(dateStr);
          break;
        case 'WEEKLY':
          data = await calendarService.getWeeklyView(dateStr);
          break;
        case 'MONTHLY':
          data = await calendarService.getMonthlyView(dateStr);
          break;
        default:
          data = await calendarService.getWeeklyView(dateStr);
      }

      setCalendarData(data);
    } catch (error) {
      toast.error('Failed to load calendar data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handlePrevious = () => {
    if (view === 'DAILY') {
      setCurrentDate(subDays(currentDate, 1));
    } else if (view === 'WEEKLY') {
      setCurrentDate(subDays(currentDate, 7));
    } else {
      setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    }
  };

  const handleNext = () => {
    if (view === 'DAILY') {
      setCurrentDate(addDays(currentDate, 1));
    } else if (view === 'WEEKLY') {
      setCurrentDate(addDays(currentDate, 7));
    } else {
      setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    }
  };

  const handleToday = () => {
    setCurrentDate(new Date());
  };

  const renderDailyView = () => {
    if (!calendarData?.tasksByDate) return null;

    const dateKey = format(currentDate, 'yyyy-MM-dd');
    const tasks = calendarData.tasksByDate[dateKey] || [];

    return (
      <div className="card">
        <h3 className="text-lg font-bold mb-4">
          {format(currentDate, 'EEEE, MMMM d, yyyy')}
        </h3>

        {tasks.length === 0 ? (
          <p className="text-gray-500 text-center py-8">No tasks for this day</p>
        ) : (
          <div className="space-y-3">
            {tasks.map(task => (
              <div
                key={task.id}
                className={`p-4 rounded-lg border-l-4 ${task.priority === 'A'
                    ? 'border-red-500 bg-red-50'
                    : task.priority === 'B'
                      ? 'border-amber-500 bg-amber-50'
                      : task.priority === 'C'
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-500 bg-gray-50'
                  }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <h4 className="font-medium text-gray-900">{task.title}</h4>
                    <div className="flex items-center gap-2 mt-1 text-sm text-gray-600">
                      {task.dueTime && <span>{task.dueTime}</span>}
                      {task.projectName && <span>• {task.projectName}</span>}
                      <span className={`badge-priority-${task.priority}`}>
                        Priority {task.priority}
                      </span>
                      {task.overdue && <span className="badge bg-red-100 text-red-800">Overdue</span>}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };

  const renderWeeklyView = () => {
    if (!calendarData?.tasksByDate) return null;

    const weekStart = startOfWeek(currentDate, { weekStartsOn: 1 });
    const weekEnd = endOfWeek(currentDate, { weekStartsOn: 1 });
    const days = eachDayOfInterval({ start: weekStart, end: weekEnd });

    return (
      <div className="grid grid-cols-7 gap-4">
        {days.map(day => {
          const dateKey = format(day, 'yyyy-MM-dd');
          const tasks = calendarData.tasksByDate[dateKey] || [];
          const isToday = isSameDay(day, new Date());

          return (
            <div key={dateKey} className="card min-h-32">
              <div className={`text-center mb-3 pb-2 border-b ${isToday ? 'border-primary-500' : ''}`}>
                <div className="text-xs text-gray-600 uppercase">
                  {format(day, 'EEE')}
                </div>
                <div className={`text-lg font-bold ${isToday ? 'text-primary-600' : 'text-gray-900'}`}>
                  {format(day, 'd')}
                </div>
              </div>

              <div className="space-y-2">
                {tasks.slice(0, 3).map(task => (
                  <div
                    key={task.id}
                    className={`text-xs p-2 rounded border-l-2 ${task.priority === 'A'
                        ? 'border-red-500 bg-red-50'
                        : task.priority === 'B'
                          ? 'border-amber-500 bg-amber-50'
                          : task.priority === 'C'
                            ? 'border-blue-500 bg-blue-50'
                            : 'border-gray-500 bg-gray-50'
                      }`}
                  >
                    <div className="font-medium text-gray-900 truncate">{task.title}</div>
                    {task.dueTime && (
                      <div className="text-gray-600 mt-1">{task.dueTime}</div>
                    )}
                  </div>
                ))}
                {tasks.length > 3 && (
                  <div className="text-xs text-gray-500 text-center">
                    +{tasks.length - 3} more
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  const renderMonthlyView = () => {
    if (!calendarData?.tasksByDate) return null;

    const monthStart = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    const monthEnd = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
    const startDate = startOfWeek(monthStart, { weekStartsOn: 1 });
    const endDate = endOfWeek(monthEnd, { weekStartsOn: 1 });
    const days = eachDayOfInterval({ start: startDate, end: endDate });

    return (
      <div className="card">
        <div className="grid grid-cols-7 gap-2">
          {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(day => (
            <div key={day} className="text-center font-medium text-gray-700 py-2">
              {day}
            </div>
          ))}

          {days.map(day => {
            const dateKey = format(day, 'yyyy-MM-dd');
            const tasks = calendarData.tasksByDate[dateKey] || [];
            const isToday = isSameDay(day, new Date());
            const isCurrentMonth = day.getMonth() === currentDate.getMonth();

            return (
              <div
                key={dateKey}
                className={`min-h-24 p-2 border rounded ${isToday ? 'border-primary-500 bg-primary-50' : 'border-gray-200'
                  } ${!isCurrentMonth ? 'opacity-40' : ''}`}
              >
                <div className={`text-sm font-medium mb-1 ${isToday ? 'text-primary-600' : 'text-gray-900'}`}>
                  {format(day, 'd')}
                </div>

                <div className="space-y-1">
                  {tasks.slice(0, 2).map(task => (
                    <div
                      key={task.id}
                      className={`text-xs p-1 rounded ${task.priority === 'A'
                          ? 'bg-red-100'
                          : task.priority === 'B'
                            ? 'bg-amber-100'
                            : task.priority === 'C'
                              ? 'bg-blue-100'
                              : 'bg-gray-100'
                        }`}
                    >
                      <div className="truncate">{task.title}</div>
                    </div>
                  ))}
                  {tasks.length > 2 && (
                    <div className="text-xs text-gray-500">+{tasks.length - 2}</div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Calendar</h1>

        <div className="flex items-center gap-4">
          <div className="flex gap-2">
            <button
              onClick={() => setView('DAILY')}
              className={`px-4 py-2 rounded-lg ${view === 'DAILY' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'
                }`}
            >
              Daily
            </button>
            <button
              onClick={() => setView('WEEKLY')}
              className={`px-4 py-2 rounded-lg ${view === 'WEEKLY' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'
                }`}
            >
              Weekly
            </button>
            <button
              onClick={() => setView('MONTHLY')}
              className={`px-4 py-2 rounded-lg ${view === 'MONTHLY' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'
                }`}
            >
              Monthly
            </button>
          </div>

          <div className="flex items-center gap-2">
            <button onClick={handlePrevious} className="btn-secondary p-2">
              <FiChevronLeft size={20} />
            </button>
            <div className="min-w-[180px] text-center">
              <span className="font-medium text-gray-900">
                {view === 'DAILY' && format(currentDate, 'MMMM d, yyyy')}
                {view === 'WEEKLY' && `${format(startOfWeek(currentDate, { weekStartsOn: 1 }), 'MMM d')} - ${format(endOfWeek(currentDate, { weekStartsOn: 1 }), 'MMM d, yyyy')}`}
                {view === 'MONTHLY' && format(currentDate, 'MMMM yyyy')}
              </span>
            </div>
            <button onClick={handleNext} className="btn-secondary p-2">
              <FiChevronRight size={20} />
            </button>
            <button onClick={handleToday} className="btn-secondary ml-2">
              Today
            </button>
          </div>
        </div>
      </div>

      <div className="mb-6 card">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <div className="text-sm text-gray-600">Total Tasks</div>
            <div className="text-2xl font-bold text-gray-900">
              {calendarData?.summary?.totalTasks || 0}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600">Completed</div>
            <div className="text-2xl font-bold text-green-600">
              {calendarData?.summary?.completedTasks || 0}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600">Overdue</div>
            <div className="text-2xl font-bold text-red-600">
              {calendarData?.summary?.overdueTasks || 0}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600">Priority A</div>
            <div className="text-2xl font-bold text-red-600">
              {calendarData?.summary?.tasksByPriority?.A || 0}
            </div>
          </div>
        </div>
      </div>

      {view === 'DAILY' && renderDailyView()}
      {view === 'WEEKLY' && renderWeeklyView()}
      {view === 'MONTHLY' && renderMonthlyView()}
    </div>
  );
};

export default Calendar;
