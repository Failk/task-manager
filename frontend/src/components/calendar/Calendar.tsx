import React, { useState, useEffect } from 'react';
import { format, addDays, addWeeks, addMonths, subDays, subWeeks, subMonths, startOfWeek, endOfWeek, startOfMonth, endOfMonth, eachDayOfInterval, isSameDay, isToday } from 'date-fns';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { CalendarView, TaskListItem, Priority } from '../../types';
import { calendarService } from '../../services/calendarService';
import { cn, getPriorityColor } from '../../utils';

type ViewType = 'daily' | 'weekly' | 'monthly';

interface CalendarProps {
  onTaskClick: (id: number) => void;
}

const Calendar: React.FC<CalendarProps> = ({ onTaskClick }) => {
  const [viewType, setViewType] = useState<ViewType>('weekly');
  const [currentDate, setCurrentDate] = useState(new Date());
  const [calendarData, setCalendarData] = useState<CalendarView | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCalendarData();
  }, [viewType, currentDate]);

  const loadCalendarData = async () => {
    setLoading(true);
    try {
      let data: CalendarView;
      switch (viewType) {
        case 'daily':
          data = await calendarService.getDailyView(format(currentDate, 'yyyy-MM-dd'));
          break;
        case 'weekly':
          data = await calendarService.getWeeklyView(format(currentDate, 'yyyy-MM-dd'));
          break;
        case 'monthly':
          data = await calendarService.getMonthlyView(
            currentDate.getFullYear(),
            currentDate.getMonth() + 1
          );
          break;
      }
      setCalendarData(data);
    } catch (error) {
      console.error('Failed to load calendar data:', error);
    } finally {
      setLoading(false);
    }
  };

  const navigatePrevious = () => {
    switch (viewType) {
      case 'daily':
        setCurrentDate(subDays(currentDate, 1));
        break;
      case 'weekly':
        setCurrentDate(subWeeks(currentDate, 1));
        break;
      case 'monthly':
        setCurrentDate(subMonths(currentDate, 1));
        break;
    }
  };

  const navigateNext = () => {
    switch (viewType) {
      case 'daily':
        setCurrentDate(addDays(currentDate, 1));
        break;
      case 'weekly':
        setCurrentDate(addWeeks(currentDate, 1));
        break;
      case 'monthly':
        setCurrentDate(addMonths(currentDate, 1));
        break;
    }
  };

  const goToToday = () => {
    setCurrentDate(new Date());
  };

  const getDateTitle = () => {
    switch (viewType) {
      case 'daily':
        return format(currentDate, 'EEEE, MMMM d, yyyy');
      case 'weekly':
        const weekStart = startOfWeek(currentDate, { weekStartsOn: 1 });
        const weekEnd = endOfWeek(currentDate, { weekStartsOn: 1 });
        return `${format(weekStart, 'MMM d')} - ${format(weekEnd, 'MMM d, yyyy')}`;
      case 'monthly':
        return format(currentDate, 'MMMM yyyy');
    }
  };

  const getTasksForDate = (date: Date): TaskListItem[] => {
    if (!calendarData) return [];
    return calendarData.tasks.filter((task) =>
      isSameDay(new Date(task.dueDate), date)
    );
  };

  const renderDailyView = () => {
    const tasks = calendarData?.tasks || [];
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="space-y-3">
          {tasks.length === 0 ? (
            <p className="text-gray-500 text-center py-8">No tasks for this day</p>
          ) : (
            tasks.map((task) => (
              <TaskItem key={task.id} task={task} onClick={onTaskClick} />
            ))
          )}
        </div>
      </div>
    );
  };

  const renderWeeklyView = () => {
    const weekStart = startOfWeek(currentDate, { weekStartsOn: 1 });
    const weekEnd = endOfWeek(currentDate, { weekStartsOn: 1 });
    const days = eachDayOfInterval({ start: weekStart, end: weekEnd });

    return (
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <div className="grid grid-cols-7 divide-x">
          {days.map((day) => (
            <div key={day.toISOString()} className="min-h-[300px]">
              <div
                className={cn(
                  'p-2 text-center border-b',
                  isToday(day) ? 'bg-blue-50' : 'bg-gray-50'
                )}
              >
                <div className="text-xs text-gray-500">{format(day, 'EEE')}</div>
                <div
                  className={cn(
                    'text-lg font-semibold',
                    isToday(day) ? 'text-blue-600' : 'text-gray-900'
                  )}
                >
                  {format(day, 'd')}
                </div>
              </div>
              <div className="p-2 space-y-1">
                {getTasksForDate(day).map((task) => (
                  <TaskBadge key={task.id} task={task} onClick={onTaskClick} />
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderMonthlyView = () => {
    const monthStart = startOfMonth(currentDate);
    const monthEnd = endOfMonth(currentDate);
    const calendarStart = startOfWeek(monthStart, { weekStartsOn: 1 });
    const calendarEnd = endOfWeek(monthEnd, { weekStartsOn: 1 });
    const days = eachDayOfInterval({ start: calendarStart, end: calendarEnd });

    return (
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        {/* Day headers */}
        <div className="grid grid-cols-7 divide-x bg-gray-50 border-b">
          {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((day) => (
            <div key={day} className="p-2 text-center text-sm font-medium text-gray-600">
              {day}
            </div>
          ))}
        </div>

        {/* Calendar grid */}
        <div className="grid grid-cols-7 divide-x">
          {days.map((day, index) => {
            const isCurrentMonth = day.getMonth() === currentDate.getMonth();
            const dayTasks = getTasksForDate(day);

            return (
              <div
                key={day.toISOString()}
                className={cn(
                  'min-h-[100px] p-2',
                  index >= 7 && 'border-t',
                  !isCurrentMonth && 'bg-gray-50'
                )}
              >
                <div
                  className={cn(
                    'text-sm mb-1',
                    isToday(day)
                      ? 'w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center'
                      : isCurrentMonth
                      ? 'text-gray-900'
                      : 'text-gray-400'
                  )}
                >
                  {format(day, 'd')}
                </div>
                <div className="space-y-1">
                  {dayTasks.slice(0, 3).map((task) => (
                    <TaskBadge key={task.id} task={task} onClick={onTaskClick} compact />
                  ))}
                  {dayTasks.length > 3 && (
                    <span className="text-xs text-gray-500">
                      +{dayTasks.length - 3} more
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <h1 className="text-2xl font-bold text-gray-900">{getDateTitle()}</h1>
          <button
            onClick={goToToday}
            className="px-3 py-1 text-sm text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          >
            Today
          </button>
        </div>

        <div className="flex items-center gap-4">
          {/* View selector */}
          <div className="flex gap-1 p-1 bg-gray-100 rounded-lg">
            {(['daily', 'weekly', 'monthly'] as ViewType[]).map((view) => (
              <button
                key={view}
                onClick={() => setViewType(view)}
                className={cn(
                  'px-3 py-1.5 text-sm font-medium rounded-md transition-colors capitalize',
                  viewType === view
                    ? 'bg-white shadow text-gray-900'
                    : 'text-gray-500 hover:text-gray-900'
                )}
              >
                {view}
              </button>
            ))}
          </div>

          {/* Navigation */}
          <div className="flex items-center gap-2">
            <button
              onClick={navigatePrevious}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <ChevronLeft className="w-5 h-5" />
            </button>
            <button
              onClick={navigateNext}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      {/* Calendar Content */}
      {loading ? (
        <div className="bg-white rounded-lg shadow-sm border p-8 text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        </div>
      ) : (
        <>
          {viewType === 'daily' && renderDailyView()}
          {viewType === 'weekly' && renderWeeklyView()}
          {viewType === 'monthly' && renderMonthlyView()}
        </>
      )}

      {/* Stats */}
      {calendarData && (
        <div className="grid grid-cols-4 gap-4">
          <div className="bg-white rounded-lg shadow-sm border p-4">
            <div className="text-sm text-gray-500">Total Tasks</div>
            <div className="text-2xl font-bold">{calendarData.metadata.totalTasks}</div>
          </div>
          <div className="bg-white rounded-lg shadow-sm border p-4">
            <div className="text-sm text-gray-500">Completed</div>
            <div className="text-2xl font-bold text-green-600">
              {calendarData.metadata.completedTasks}
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-sm border p-4">
            <div className="text-sm text-gray-500">Critical (A)</div>
            <div className="text-2xl font-bold text-red-500">
              {calendarData.metadata.tasksByPriority[Priority.A] || 0}
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-sm border p-4">
            <div className="text-sm text-gray-500">Important (B)</div>
            <div className="text-2xl font-bold text-orange-500">
              {calendarData.metadata.tasksByPriority[Priority.B] || 0}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// Helper components
interface TaskItemProps {
  task: TaskListItem;
  onClick: (id: number) => void;
}

const TaskItem: React.FC<TaskItemProps> = ({ task, onClick }) => (
  <div
    onClick={() => onClick(task.id)}
    className={cn(
      'p-3 rounded-lg border-l-4 cursor-pointer hover:bg-gray-50 transition-colors',
      task.priority === Priority.A && 'border-red-500',
      task.priority === Priority.B && 'border-orange-500',
      task.priority === Priority.C && 'border-yellow-500',
      task.priority === Priority.D && 'border-green-500'
    )}
  >
    <div className="font-medium text-gray-900">{task.title}</div>
    <div className="text-sm text-gray-500 mt-1">
      {format(new Date(task.dueDate), 'h:mm a')}
      {task.projectName && ` • ${task.projectName}`}
    </div>
  </div>
);

interface TaskBadgeProps {
  task: TaskListItem;
  onClick: (id: number) => void;
  compact?: boolean;
}

const TaskBadge: React.FC<TaskBadgeProps> = ({ task, onClick, compact }) => (
  <div
    onClick={() => onClick(task.id)}
    className={cn(
      'px-2 py-1 rounded text-xs cursor-pointer truncate',
      getPriorityColor(task.priority),
      'text-white hover:opacity-80 transition-opacity'
    )}
    title={task.title}
  >
    {task.title}
  </div>
);

export default Calendar;
