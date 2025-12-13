import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, Clock, AlertTriangle, TrendingUp, FolderKanban } from 'lucide-react';
import { TaskList } from '../components/tasks';
import { Card, CardBody } from '../components/common';
import { taskService } from '../services/taskService';
import { projectService } from '../services/projectService';
import { TaskListItem, Project, Priority } from '../types';
import { cn, getPriorityColor } from '../utils';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [todayTasks, setTodayTasks] = useState<TaskListItem[]>([]);
  const [overdueTasks, setOverdueTasks] = useState<TaskListItem[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [today, overdue, projectsData] = await Promise.all([
        taskService.getToday(),
        taskService.getOverdue(),
        projectService.getAll(),
      ]);
      setTodayTasks(today);
      setOverdueTasks(overdue);
      setProjects(projectsData);
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteTask = async (id: number) => {
    try {
      await taskService.complete(id);
      loadData();
    } catch (error) {
      console.error('Failed to complete task:', error);
    }
  };

  const handleTaskClick = (id: number) => {
    navigate(`/tasks/${id}`);
  };

  const stats = {
    totalTasks: todayTasks.length + overdueTasks.length,
    completedToday: todayTasks.filter((t) => t.status === 'COMPLETED').length,
    overdue: overdueTasks.length,
    inProgress: todayTasks.filter((t) => t.status === 'IN_PROGRESS').length,
  };

  const priorityStats = {
    [Priority.A]: todayTasks.filter((t) => t.priority === Priority.A).length,
    [Priority.B]: todayTasks.filter((t) => t.priority === Priority.B).length,
    [Priority.C]: todayTasks.filter((t) => t.priority === Priority.C).length,
    [Priority.D]: todayTasks.filter((t) => t.priority === Priority.D).length,
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 mt-1">
          Welcome back! Here's your productivity overview.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardBody className="flex items-center gap-4">
            <div className="p-3 bg-blue-100 rounded-lg">
              <Clock className="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Tasks Today</p>
              <p className="text-2xl font-bold">{stats.totalTasks}</p>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="flex items-center gap-4">
            <div className="p-3 bg-green-100 rounded-lg">
              <CheckCircle className="w-6 h-6 text-green-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Completed</p>
              <p className="text-2xl font-bold text-green-600">{stats.completedToday}</p>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="flex items-center gap-4">
            <div className="p-3 bg-red-100 rounded-lg">
              <AlertTriangle className="w-6 h-6 text-red-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Overdue</p>
              <p className="text-2xl font-bold text-red-600">{stats.overdue}</p>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="flex items-center gap-4">
            <div className="p-3 bg-purple-100 rounded-lg">
              <TrendingUp className="w-6 h-6 text-purple-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">In Progress</p>
              <p className="text-2xl font-bold text-purple-600">{stats.inProgress}</p>
            </div>
          </CardBody>
        </Card>
      </div>

      {/* Priority Distribution */}
      <Card>
        <CardBody>
          <h3 className="font-semibold text-gray-900 mb-4">Today's Priority Distribution</h3>
          <div className="flex gap-4">
            {Object.entries(priorityStats).map(([priority, count]) => (
              <div key={priority} className="flex-1">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-gray-600">
                    {priority === 'A' ? 'Critical' : priority === 'B' ? 'Important' : priority === 'C' ? 'Normal' : 'Low'}
                  </span>
                  <span className="text-sm text-gray-500">{count}</span>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className={cn('h-full rounded-full', getPriorityColor(priority as Priority))}
                    style={{
                      width: `${stats.totalTasks > 0 ? (count / stats.totalTasks) * 100 : 0}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </CardBody>
      </Card>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Today's Tasks */}
        <div className="lg:col-span-2 space-y-4">
          <h2 className="text-lg font-semibold text-gray-900">Today's Tasks</h2>
          <TaskList
            tasks={todayTasks}
            loading={loading}
            onComplete={handleCompleteTask}
            onClick={handleTaskClick}
            emptyMessage="No tasks scheduled for today"
          />
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Overdue Tasks */}
          {overdueTasks.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-red-600 mb-4 flex items-center gap-2">
                <AlertTriangle className="w-5 h-5" />
                Overdue Tasks
              </h2>
              <TaskList
                tasks={overdueTasks}
                onComplete={handleCompleteTask}
                onClick={handleTaskClick}
              />
            </div>
          )}

          {/* Projects */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <FolderKanban className="w-5 h-5" />
              Projects
            </h2>
            <div className="space-y-3">
              {projects.slice(0, 5).map((project) => (
                <Card
                  key={project.id}
                  hover
                  onClick={() => navigate(`/projects/${project.id}`)}
                >
                  <CardBody className="py-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div
                          className="w-3 h-3 rounded-full"
                          style={{ backgroundColor: project.color }}
                        />
                        <span className="font-medium text-gray-900">{project.name}</span>
                      </div>
                      <span className="text-sm text-gray-500">
                        {project.completionPercentage}%
                      </span>
                    </div>
                    <div className="mt-2 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-blue-600 rounded-full"
                        style={{ width: `${project.completionPercentage}%` }}
                      />
                    </div>
                  </CardBody>
                </Card>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
