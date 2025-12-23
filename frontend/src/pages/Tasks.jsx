import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { FiPlus, FiFilter } from 'react-icons/fi';
import taskService from '../services/task.service';
import TaskCard from '../components/tasks/TaskCard';
import TaskForm from '../components/tasks/TaskForm';

const Tasks = () => {
  const [tasks, setTasks] = useState([]);
  const [filteredTasks, setFilteredTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [filters, setFilters] = useState({
    status: 'ALL',
    priority: 'ALL',
    view: 'ALL',
  });

  useEffect(() => {
    loadTasks();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [tasks, filters]);

  const loadTasks = async () => {
    try {
      setLoading(true);
      const data = await taskService.getAllTasks();
      setTasks(data);
    } catch (error) {
      toast.error('Failed to load tasks');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...tasks];

    if (filters.view === 'TODAY') {
      const today = new Date().toISOString().split('T')[0];
      filtered = filtered.filter(task => task.dueDate === today);
    } else if (filters.view === 'OVERDUE') {
      filtered = filtered.filter(task => task.overdue && task.status !== 'COMPLETED');
    }

    if (filters.status !== 'ALL') {
      filtered = filtered.filter(task => task.status === filters.status);
    }

    if (filters.priority !== 'ALL') {
      filtered = filtered.filter(task => task.priority === filters.priority);
    }

    setFilteredTasks(filtered);
  };

  const handleCreateTask = async (taskData) => {
    try {
      if (taskData.taskType === 'ONE_TIME') {
        await taskService.createOneTimeTask(taskData);
      } else {
        await taskService.createRecurringTask(taskData);
      }
      toast.success('Task created successfully');
      setShowForm(false);
      loadTasks();
    } catch (error) {
      throw error;
    }
  };

  const handleUpdateTask = async (taskData) => {
    try {
      await taskService.updateTask(editingTask.id, taskData);
      toast.success('Task updated successfully');
      setShowForm(false);
      setEditingTask(null);
      loadTasks();
    } catch (error) {
      throw error;
    }
  };

  const handleCompleteTask = async (task) => {
    try {
      await taskService.completeTask(task.id);
      toast.success('Task completed!');
      loadTasks();
    } catch (error) {
      toast.error('Failed to complete task');
    }
  };

  const handleDeleteTask = async (task) => {
    if (!window.confirm('Are you sure you want to delete this task?')) {
      return;
    }

    try {
      await taskService.deleteTask(task.id);
      toast.success('Task deleted');
      loadTasks();
    } catch (error) {
      toast.error('Failed to delete task');
    }
  };

  const handleEditTask = (task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const groupTasksByPriority = () => {
    const groups = {
      A: [],
      B: [],
      C: [],
      D: [],
    };

    filteredTasks.forEach(task => {
      if (groups[task.priority]) {
        groups[task.priority].push(task);
      }
    });

    return groups;
  };

  const taskGroups = groupTasksByPriority();

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
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Tasks</h1>
          <p className="text-gray-600 mt-1">
            {filteredTasks.length} {filteredTasks.length === 1 ? 'task' : 'tasks'}
          </p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary flex items-center gap-2">
          <FiPlus />
          New Task
        </button>
      </div>

      <div className="card mb-6">
        <div className="flex items-center gap-4">
          <FiFilter className="text-gray-400" />

          <select
            value={filters.view}
            onChange={(e) => setFilters({ ...filters, view: e.target.value })}
            className="input max-w-xs"
          >
            <option value="ALL">All Tasks</option>
            <option value="TODAY">Today</option>
            <option value="OVERDUE">Overdue</option>
          </select>

          <select
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            className="input max-w-xs"
          >
            <option value="ALL">All Status</option>
            <option value="NOT_STARTED">Not Started</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <select
            value={filters.priority}
            onChange={(e) => setFilters({ ...filters, priority: e.target.value })}
            className="input max-w-xs"
          >
            <option value="ALL">All Priorities</option>
            <option value="A">Priority A - Critical</option>
            <option value="B">Priority B - Important</option>
            <option value="C">Priority C - Nice to have</option>
            <option value="D">Priority D - Delegate</option>
          </select>
        </div>
      </div>

      {filteredTasks.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-gray-500 text-lg">No tasks found</p>
          <p className="text-gray-400 mt-2">Create your first task to get started</p>
        </div>
      ) : (
        <div className="space-y-6">
          {Object.entries(taskGroups).map(([priority, priorityTasks]) => {
            if (priorityTasks.length === 0) return null;

            const priorityLabels = {
              A: 'Critical - Must be done today',
              B: 'Important - Should be done soon',
              C: 'Nice to have - Can wait',
              D: 'Delegate or Defer',
            };

            return (
              <div key={priority}>
                <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
                  <span className={`badge-priority-${priority}`}>Priority {priority}</span>
                  <span className="text-gray-600 text-base font-normal">
                    {priorityLabels[priority]}
                  </span>
                  <span className="text-gray-400 text-sm font-normal">
                    ({priorityTasks.length})
                  </span>
                </h2>
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                  {priorityTasks.map(task => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      onComplete={handleCompleteTask}
                      onEdit={handleEditTask}
                      onDelete={handleDeleteTask}
                    />
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask ? handleUpdateTask : handleCreateTask}
          onCancel={() => {
            setShowForm(false);
            setEditingTask(null);
          }}
        />
      )}
    </div>
  );
};

export default Tasks;
