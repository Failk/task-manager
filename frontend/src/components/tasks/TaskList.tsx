import React from 'react';
import TaskCard from './TaskCard';
import { TaskListItem } from '../../types';
import { ClipboardList } from 'lucide-react';

interface TaskListProps {
  tasks: TaskListItem[];
  loading?: boolean;
  onComplete: (id: number) => void;
  onClick: (id: number) => void;
  emptyMessage?: string;
}

const TaskList: React.FC<TaskListProps> = ({
  tasks,
  loading,
  onComplete,
  onClick,
  emptyMessage = 'No tasks found',
}) => {
  if (loading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="bg-white rounded-lg border-l-4 border-gray-200 shadow-sm p-4 animate-pulse"
          >
            <div className="flex items-start gap-3">
              <div className="w-5 h-5 rounded bg-gray-200" />
              <div className="flex-1">
                <div className="h-5 bg-gray-200 rounded w-3/4 mb-2" />
                <div className="h-4 bg-gray-200 rounded w-1/2" />
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (tasks.length === 0) {
    return (
      <div className="text-center py-12">
        <ClipboardList className="w-12 h-12 text-gray-300 mx-auto mb-4" />
        <p className="text-gray-500">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {tasks.map((task) => (
        <TaskCard
          key={task.id}
          task={task}
          onComplete={onComplete}
          onClick={onClick}
        />
      ))}
    </div>
  );
};

export default TaskList;
