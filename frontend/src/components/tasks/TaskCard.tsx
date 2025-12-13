import React from 'react';
import { Check, MoreVertical, Calendar, Clock, Tag } from 'lucide-react';
import { TaskListItem, Priority, TaskStatus } from '../../types';
import {
  cn,
  formatRelativeDate,
  getPriorityColor,
  getPriorityBorderColor,
  isOverdue,
} from '../../utils';

interface TaskCardProps {
  task: TaskListItem;
  onComplete: (id: number) => void;
  onClick: (id: number) => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, onComplete, onClick }) => {
  const overdue = isOverdue(task.dueDate, task.status);

  return (
    <div
      className={cn(
        'bg-white rounded-lg border-l-4 shadow-sm p-4 hover:shadow-md transition-shadow cursor-pointer',
        getPriorityBorderColor(task.priority),
        task.status === TaskStatus.COMPLETED && 'opacity-60'
      )}
      onClick={() => onClick(task.id)}
    >
      <div className="flex items-start gap-3">
        {/* Checkbox */}
        <button
          onClick={(e) => {
            e.stopPropagation();
            onComplete(task.id);
          }}
          className={cn(
            'flex-shrink-0 w-5 h-5 rounded border-2 flex items-center justify-center transition-colors',
            task.status === TaskStatus.COMPLETED
              ? 'bg-green-500 border-green-500 text-white'
              : 'border-gray-300 hover:border-gray-400'
          )}
        >
          {task.status === TaskStatus.COMPLETED && <Check className="w-3 h-3" />}
        </button>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <h3
              className={cn(
                'font-medium text-gray-900',
                task.status === TaskStatus.COMPLETED && 'line-through text-gray-500'
              )}
            >
              {task.title}
            </h3>
            <button
              onClick={(e) => e.stopPropagation()}
              className="p-1 text-gray-400 hover:text-gray-600 rounded"
            >
              <MoreVertical className="w-4 h-4" />
            </button>
          </div>

          {/* Meta info */}
          <div className="flex flex-wrap items-center gap-3 mt-2 text-sm text-gray-500">
            {/* Due date */}
            <div
              className={cn(
                'flex items-center gap-1',
                overdue && 'text-red-500'
              )}
            >
              <Calendar className="w-4 h-4" />
              <span>{formatRelativeDate(task.dueDate)}</span>
            </div>

            {/* Project */}
            {task.projectName && (
              <div className="flex items-center gap-1">
                <span className="text-gray-400">•</span>
                <span>{task.projectName}</span>
              </div>
            )}

            {/* Priority badge */}
            <span
              className={cn(
                'px-2 py-0.5 text-xs font-medium rounded-full text-white',
                getPriorityColor(task.priority)
              )}
            >
              {task.priority}
            </span>
          </div>

          {/* Contexts */}
          {task.contextNames.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              {task.contextNames.map((context) => (
                <span
                  key={context}
                  className="inline-flex items-center gap-1 px-2 py-0.5 text-xs bg-gray-100 text-gray-600 rounded"
                >
                  <Tag className="w-3 h-3" />
                  {context}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TaskCard;
