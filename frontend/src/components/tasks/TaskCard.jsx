import React from 'react';
import { format } from 'date-fns';
import { FiClock, FiFolder, FiTag, FiCheck, FiEdit, FiTrash2 } from 'react-icons/fi';

const TaskCard = ({ task, onComplete, onEdit, onDelete }) => {
  const priorityColors = {
    A: 'border-l-4 border-red-500',
    B: 'border-l-4 border-amber-500',
    C: 'border-l-4 border-blue-500',
    D: 'border-l-4 border-gray-500',
  };

  const isOverdue = task.overdue && task.status !== 'COMPLETED';

  return (
    <div className={`card hover:shadow-lg transition-shadow ${priorityColors[task.priority]} ${
      task.status === 'COMPLETED' ? 'opacity-60' : ''
    }`}>
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-start gap-3">
            <button
              onClick={() => onComplete(task)}
              disabled={task.status === 'COMPLETED'}
              className={`mt-1 flex-shrink-0 w-5 h-5 rounded border-2 flex items-center justify-center transition-colors ${
                task.status === 'COMPLETED'
                  ? 'bg-green-500 border-green-500'
                  : 'border-gray-300 hover:border-primary-500'
              }`}
            >
              {task.status === 'COMPLETED' && <FiCheck className="text-white" size={14} />}
            </button>

            <div className="flex-1 min-w-0">
              <h3 className={`font-medium text-gray-900 mb-1 ${
                task.status === 'COMPLETED' ? 'line-through' : ''
              }`}>
                {task.title}
              </h3>

              {task.description && (
                <p className="text-sm text-gray-600 mb-2 line-clamp-2">{task.description}</p>
              )}

              <div className="flex flex-wrap gap-2 text-xs">
                <span className={`badge-priority-${task.priority}`}>
                  Priority {task.priority}
                </span>
                <span className={`badge-status-${task.status}`}>
                  {task.status.replace('_', ' ')}
                </span>
                {isOverdue && (
                  <span className="badge bg-red-100 text-red-800">Overdue</span>
                )}
                {task.taskType === 'RECURRING' && (
                  <span className="badge bg-purple-100 text-purple-800">Recurring</span>
                )}
              </div>

              <div className="flex flex-wrap gap-4 mt-3 text-sm text-gray-600">
                {task.dueDate && (
                  <div className="flex items-center gap-1">
                    <FiClock size={14} />
                    <span>
                      {format(new Date(task.dueDate), 'MMM d, yyyy')}
                      {task.dueTime && ` at ${task.dueTime}`}
                    </span>
                  </div>
                )}

                {task.projectName && (
                  <div className="flex items-center gap-1">
                    <FiFolder size={14} />
                    <span>{task.projectName}</span>
                  </div>
                )}

                {task.contexts && task.contexts.length > 0 && (
                  <div className="flex items-center gap-1">
                    <FiTag size={14} />
                    <span>{task.contexts.map(c => c.name).join(', ')}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => onEdit(task)}
            className="p-2 text-gray-400 hover:text-primary-600 hover:bg-primary-50 rounded"
            title="Edit task"
          >
            <FiEdit size={16} />
          </button>
          <button
            onClick={() => onDelete(task)}
            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded"
            title="Delete task"
          >
            <FiTrash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaskCard;
