import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  ArrowLeft,
  Calendar,
  Clock,
  Tag,
  Edit2,
  Trash2,
  CheckCircle2,
  Circle,
  AlertTriangle,
  Repeat,
  Bell,
  MoreVertical,
  MessageCircle,
  Send,
} from 'lucide-react';
import { taskService } from '../services/taskService';
import { TaskStatus, TaskComment } from '../types';
import { formatDate, formatRelativeDate, getPriorityColor, getPriorityLabel } from '../utils';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';

const TaskDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [showMenu, setShowMenu] = useState(false);
  const [newComment, setNewComment] = useState('');

  const taskId = id ? parseInt(id, 10) : 0;

  const { data: task, isLoading, error } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => taskService.getById(taskId),
    enabled: taskId > 0,
  });

  const { data: comments = [] } = useQuery({
    queryKey: ['task-comments', taskId],
    queryFn: () => taskService.getComments(taskId),
    enabled: taskId > 0,
  });

  const completeMutation = useMutation({
    mutationFn: () => taskService.complete(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
  });

  const reopenMutation = useMutation({
    mutationFn: () => taskService.reopen(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => taskService.delete(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      navigate('/tasks');
    },
  });

  const addCommentMutation = useMutation({
    mutationFn: (content: string) => taskService.addComment(taskId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task-comments', taskId] });
      setNewComment('');
    },
  });

  // Close menu when clicking outside
  useEffect(() => {
    const handleClick = () => setShowMenu(false);
    if (showMenu) {
      document.addEventListener('click', handleClick);
      return () => document.removeEventListener('click', handleClick);
    }
  }, [showMenu]);

  const handleSubmitComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (newComment.trim()) {
      addCommentMutation.mutate(newComment.trim());
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error || !task) {
    return (
      <div className="text-center py-12">
        <AlertTriangle className="w-12 h-12 text-yellow-500 mx-auto mb-4" />
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Task not found</h2>
        <p className="text-gray-600 mb-4">The task you're looking for doesn't exist or has been deleted.</p>
        <Button onClick={() => navigate('/tasks')}>Back to Tasks</Button>
      </div>
    );
  }

  const isCompleted = task.status === TaskStatus.COMPLETED;
  const isOverdue = !isCompleted && task.dueDate && new Date(task.dueDate) < new Date();

  const handleToggleComplete = () => {
    if (isCompleted) {
      reopenMutation.mutate();
    } else {
      completeMutation.mutate();
    }
  };

  const handleDelete = () => {
    deleteMutation.mutate();
  };

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="mb-6">
        <Link
          to="/tasks"
          className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Tasks
        </Link>
      </div>

      {/* Task Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {/* Priority indicator */}
        <div className={`h-2 ${getPriorityColor(task.priority)}`} />

        <div className="p-6">
          {/* Title and actions */}
          <div className="flex items-start justify-between gap-4 mb-6">
            <div className="flex items-start gap-4 flex-1">
              <button
                onClick={handleToggleComplete}
                className={`mt-1 flex-shrink-0 transition-colors ${
                  isCompleted
                    ? 'text-green-500 hover:text-green-600'
                    : 'text-gray-300 hover:text-gray-400'
                }`}
              >
                {isCompleted ? (
                  <CheckCircle2 className="w-7 h-7" />
                ) : (
                  <Circle className="w-7 h-7" />
                )}
              </button>
              <div className="flex-1">
                <h1
                  className={`text-2xl font-bold ${
                    isCompleted ? 'text-gray-400 line-through' : 'text-gray-900'
                  }`}
                >
                  {task.title}
                </h1>
                {task.description && (
                  <p className={`mt-2 text-gray-600 ${isCompleted ? 'line-through' : ''}`}>
                    {task.description}
                  </p>
                )}
              </div>
            </div>

            {/* Actions menu */}
            <div className="relative">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu(!showMenu);
                }}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg"
              >
                <MoreVertical className="w-5 h-5" />
              </button>
              {showMenu && (
                <div className="absolute right-0 mt-1 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-10">
                  <button
                    onClick={() => {
                      setShowMenu(false);
                      // TODO: Implement edit modal
                    }}
                    className="w-full px-4 py-2 text-left text-gray-700 hover:bg-gray-50 flex items-center gap-2"
                  >
                    <Edit2 className="w-4 h-4" />
                    Edit Task
                  </button>
                  <button
                    onClick={() => {
                      setShowMenu(false);
                      setIsDeleteModalOpen(true);
                    }}
                    className="w-full px-4 py-2 text-left text-red-600 hover:bg-red-50 flex items-center gap-2"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete Task
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Metadata grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            {/* Priority */}
            <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
              <div className={`w-3 h-3 rounded-full ${getPriorityColor(task.priority)}`} />
              <div>
                <p className="text-xs text-gray-500 uppercase tracking-wide">Priority</p>
                <p className="font-medium text-gray-900">{getPriorityLabel(task.priority)}</p>
              </div>
            </div>

            {/* Status */}
            <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
              {isCompleted ? (
                <CheckCircle2 className="w-5 h-5 text-green-500" />
              ) : isOverdue ? (
                <AlertTriangle className="w-5 h-5 text-red-500" />
              ) : (
                <Clock className="w-5 h-5 text-blue-500" />
              )}
              <div>
                <p className="text-xs text-gray-500 uppercase tracking-wide">Status</p>
                <p className={`font-medium ${
                  isCompleted ? 'text-green-600' : isOverdue ? 'text-red-600' : 'text-gray-900'
                }`}>
                  {isCompleted ? 'Completed' : isOverdue ? 'Overdue' : task.status.replace('_', ' ')}
                </p>
              </div>
            </div>

            {/* Due Date */}
            {task.dueDate && (
              <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <Calendar className={`w-5 h-5 ${isOverdue && !isCompleted ? 'text-red-500' : 'text-gray-400'}`} />
                <div>
                  <p className="text-xs text-gray-500 uppercase tracking-wide">Due Date</p>
                  <p className={`font-medium ${isOverdue && !isCompleted ? 'text-red-600' : 'text-gray-900'}`}>
                    {formatDate(task.dueDate)}
                  </p>
                </div>
              </div>
            )}

            {/* Project */}
            {task.project && (
              <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <div
                  className="w-5 h-5 rounded"
                  style={{ backgroundColor: task.project.color || '#6B7280' }}
                />
                <div>
                  <p className="text-xs text-gray-500 uppercase tracking-wide">Project</p>
                  <p className="font-medium text-gray-900">
                    {task.project.name}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Contexts */}
          {task.contexts && task.contexts.length > 0 && (
            <div className="mb-6">
              <p className="text-sm text-gray-500 mb-2 flex items-center gap-2">
                <Tag className="w-4 h-4" />
                Contexts
              </p>
              <div className="flex flex-wrap gap-2">
                {task.contexts.map((context) => (
                  <span
                    key={context.id}
                    className="px-3 py-1 text-sm rounded-full"
                    style={{
                      backgroundColor: `${context.color || '#6B7280'}20`,
                      color: context.color || '#6B7280',
                    }}
                  >
                    @{context.name}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Recurring info */}
          {task.recurringTaskId && (
            <div className="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-100">
              <div className="flex items-center gap-2 text-blue-700">
                <Repeat className="w-5 h-5" />
                <span className="font-medium">Recurring Task</span>
              </div>
              <p className="text-sm text-blue-600 mt-1">
                This task is part of a recurring series.
              </p>
            </div>
          )}

          {/* Reminders */}
          {task.reminders && task.reminders.length > 0 && (
            <div className="mb-6">
              <p className="text-sm text-gray-500 mb-2 flex items-center gap-2">
                <Bell className="w-4 h-4" />
                Reminders
              </p>
              <div className="space-y-2">
                {task.reminders.map((reminder) => (
                  <div
                    key={reminder.id}
                    className="flex items-center gap-2 text-sm text-gray-600"
                  >
                    <Clock className="w-4 h-4 text-gray-400" />
                    {formatDate(reminder.reminderTime)}
                    {reminder.sent && (
                      <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded">
                        Sent
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Timestamps */}
          <div className="pt-6 border-t border-gray-100 text-sm text-gray-500">
            <p>Created {formatRelativeDate(task.createdAt)}</p>
            {task.updatedAt !== task.createdAt && (
              <p>Updated {formatRelativeDate(task.updatedAt)}</p>
            )}
            {task.completedAt && (
              <p className="text-green-600">Completed {formatRelativeDate(task.completedAt)}</p>
            )}
          </div>
        </div>
      </div>

      {/* Comments Section */}
      <div className="mt-6 bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-4 border-b border-gray-100">
          <h2 className="font-semibold text-gray-900 flex items-center gap-2">
            <MessageCircle className="w-5 h-5" />
            Comments ({comments.length})
          </h2>
        </div>
        
        <div className="p-4">
          {/* Comments list */}
          {comments.length > 0 ? (
            <div className="space-y-4 mb-4">
              {comments.map((comment: TaskComment) => (
                <div key={comment.id} className="flex gap-3">
                  <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                    <span className="text-sm font-medium text-blue-600">
                      {comment.authorName?.[0]?.toUpperCase() || 'U'}
                    </span>
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-medium text-gray-900 text-sm">
                        {comment.authorName || 'Unknown'}
                      </span>
                      <span className="text-xs text-gray-400">
                        {formatRelativeDate(comment.createdAt)}
                      </span>
                    </div>
                    <p className="text-gray-600 text-sm">{comment.content}</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-sm mb-4">No comments yet.</p>
          )}

          {/* Add comment form */}
          <form onSubmit={handleSubmitComment} className="flex gap-2">
            <input
              type="text"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add a comment..."
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
            <Button
              type="submit"
              disabled={!newComment.trim() || addCommentMutation.isPending}
              size="sm"
            >
              <Send className="w-4 h-4" />
            </Button>
          </form>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Delete Task"
        size="sm"
      >
        <div className="text-center">
          <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <Trash2 className="w-6 h-6 text-red-600" />
          </div>
          <p className="text-gray-600 mb-6">
            Are you sure you want to delete "{task.title}"? This action cannot be undone.
          </p>
          <div className="flex gap-3 justify-center">
            <Button variant="secondary" onClick={() => setIsDeleteModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
            >
              {deleteMutation.isPending ? 'Deleting...' : 'Delete Task'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default TaskDetail;
