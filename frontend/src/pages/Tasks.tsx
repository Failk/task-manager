import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Filter, Search } from 'lucide-react';
import { TaskList } from '../components/tasks';
import { Button, Select, Input } from '../components/common';
import { taskService } from '../services/taskService';
import { projectService } from '../services/projectService';
import { TaskListItem, Project, Priority, TaskStatus, TaskFilter } from '../types';

const Tasks: React.FC = () => {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState<TaskListItem[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<TaskFilter>({});

  useEffect(() => {
    loadProjects();
  }, []);

  useEffect(() => {
    loadTasks();
  }, [filters]);

  const loadProjects = async () => {
    try {
      const data = await projectService.getAll();
      setProjects(data);
    } catch (error) {
      console.error('Failed to load projects:', error);
    }
  };

  const loadTasks = async () => {
    setLoading(true);
    try {
      const data = await taskService.getAll(filters);
      setTasks(data);
    } catch (error) {
      console.error('Failed to load tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteTask = async (id: number) => {
    try {
      await taskService.complete(id);
      loadTasks();
    } catch (error) {
      console.error('Failed to complete task:', error);
    }
  };

  const handleTaskClick = (id: number) => {
    navigate(`/tasks/${id}`);
  };

  const handleFilterChange = (key: keyof TaskFilter, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value || undefined,
    }));
  };

  const clearFilters = () => {
    setFilters({});
  };

  const priorityOptions = [
    { value: '', label: 'All Priorities' },
    { value: Priority.A, label: 'A - Critical' },
    { value: Priority.B, label: 'B - Important' },
    { value: Priority.C, label: 'C - Complete When Possible' },
    { value: Priority.D, label: 'D - Delegate/Defer' },
  ];

  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: TaskStatus.NOT_STARTED, label: 'Not Started' },
    { value: TaskStatus.IN_PROGRESS, label: 'In Progress' },
    { value: TaskStatus.COMPLETED, label: 'Completed' },
    { value: TaskStatus.DEFERRED, label: 'Deferred' },
  ];

  const projectOptions = [
    { value: '', label: 'All Projects' },
    ...projects.map((p) => ({ value: p.id.toString(), label: p.name })),
  ];

  const hasFilters = Object.values(filters).some((v) => v !== undefined && v !== '');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Tasks</h1>
          <p className="text-gray-500 mt-1">
            Manage and organize all your tasks
          </p>
        </div>
        <Button
          variant={showFilters ? 'primary' : 'secondary'}
          onClick={() => setShowFilters(!showFilters)}
        >
          <Filter className="w-4 h-4 mr-2" />
          Filters
          {hasFilters && (
            <span className="ml-2 w-2 h-2 bg-blue-400 rounded-full" />
          )}
        </Button>
      </div>

      {/* Search and Filters */}
      <div className="space-y-4">
        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search tasks..."
            value={filters.search || ''}
            onChange={(e) => handleFilterChange('search', e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        {/* Filter Panel */}
        {showFilters && (
          <div className="bg-gray-50 rounded-lg p-4 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <Select
                label="Priority"
                value={filters.priority || ''}
                onChange={(e) => handleFilterChange('priority', e.target.value)}
                options={priorityOptions}
              />

              <Select
                label="Status"
                value={filters.status || ''}
                onChange={(e) => handleFilterChange('status', e.target.value)}
                options={statusOptions}
              />

              <Select
                label="Project"
                value={filters.projectId?.toString() || ''}
                onChange={(e) => handleFilterChange('projectId', e.target.value ? parseInt(e.target.value) : undefined)}
                options={projectOptions}
              />

              <Input
                label="Context"
                placeholder="e.g., @home"
                value={filters.contextName || ''}
                onChange={(e) => handleFilterChange('contextName', e.target.value)}
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Start Date"
                type="date"
                value={filters.startDate || ''}
                onChange={(e) => handleFilterChange('startDate', e.target.value)}
              />

              <Input
                label="End Date"
                type="date"
                value={filters.endDate || ''}
                onChange={(e) => handleFilterChange('endDate', e.target.value)}
              />
            </div>

            {hasFilters && (
              <div className="flex justify-end">
                <Button variant="ghost" size="sm" onClick={clearFilters}>
                  Clear Filters
                </Button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Results count */}
      <div className="text-sm text-gray-500">
        {loading ? 'Loading...' : `${tasks.length} task${tasks.length !== 1 ? 's' : ''} found`}
      </div>

      {/* Task List */}
      <TaskList
        tasks={tasks}
        loading={loading}
        onComplete={handleCompleteTask}
        onClick={handleTaskClick}
        emptyMessage="No tasks match your filters"
      />
    </div>
  );
};

export default Tasks;
