import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FiCheckSquare, FiClock, FiAlertCircle, FiTrendingUp } from 'react-icons/fi';
import taskService from '../services/task.service';
import projectService from '../services/project.service';
import TaskCard from '../components/tasks/TaskCard';

const Dashboard = () => {
  const [stats, setStats] = useState({
    todayTasks: 0,
    overdueTasks: 0,
    completedTasks: 0,
    inProgressTasks: 0,
  });
  const [todayTasks, setTodayTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [allTasks, today, overdue, projectsData] = await Promise.all([
        taskService.getAllTasks(),
        taskService.getTodayTasks(),
        taskService.getOverdueTasks(),
        projectService.getAllProjects(),
      ]);

      setTodayTasks(today);
      setProjects(projectsData.slice(0, 5));

      setStats({
        todayTasks: today.length,
        overdueTasks: overdue.length,
        completedTasks: allTasks.filter(t => t.status === 'COMPLETED').length,
        inProgressTasks: allTasks.filter(t => t.status === 'IN_PROGRESS').length,
      });
    } catch (error) {
      toast.error('Failed to load dashboard data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteTask = async (task) => {
    try {
      await taskService.completeTask(task.id);
      toast.success('Task completed!');
      loadDashboardData();
    } catch (error) {
      toast.error('Failed to complete task');
    }
  };

  const statCards = [
    {
      title: "Today's Tasks",
      value: stats.todayTasks,
      icon: FiCheckSquare,
      color: 'bg-blue-500',
      link: '/tasks',
    },
    {
      title: 'In Progress',
      value: stats.inProgressTasks,
      icon: FiTrendingUp,
      color: 'bg-amber-500',
      link: '/tasks',
    },
    {
      title: 'Overdue',
      value: stats.overdueTasks,
      icon: FiAlertCircle,
      color: 'bg-red-500',
      link: '/tasks',
    },
    {
      title: 'Completed',
      value: stats.completedTasks,
      icon: FiClock,
      color: 'bg-green-500',
      link: '/tasks',
    },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">Welcome back! Here's your task overview</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statCards.map((stat) => {
          const Icon = stat.icon;
          return (
            <Link key={stat.title} to={stat.link}>
              <div className="card hover:shadow-lg transition-shadow cursor-pointer">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600 mb-1">{stat.title}</p>
                    <p className="text-3xl font-bold text-gray-900">{stat.value}</p>
                  </div>
                  <div className={`${stat.color} p-3 rounded-lg`}>
                    <Icon className="text-white" size={24} />
                  </div>
                </div>
              </div>
            </Link>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold">Today's Tasks</h2>
            <Link to="/tasks" className="text-primary-600 hover:text-primary-700 text-sm font-medium">
              View All
            </Link>
          </div>

          {todayTasks.length === 0 ? (
            <div className="card text-center py-12">
              <p className="text-gray-500">No tasks for today</p>
              <p className="text-gray-400 mt-2 text-sm">You're all caught up!</p>
            </div>
          ) : (
            <div className="space-y-4">
              {todayTasks.slice(0, 5).map(task => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onComplete={handleCompleteTask}
                  onEdit={() => {}}
                  onDelete={() => {}}
                />
              ))}
            </div>
          )}
        </div>

        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold">Recent Projects</h2>
            <Link to="/projects" className="text-primary-600 hover:text-primary-700 text-sm font-medium">
              View All
            </Link>
          </div>

          {projects.length === 0 ? (
            <div className="card text-center py-12">
              <p className="text-gray-500">No projects yet</p>
            </div>
          ) : (
            <div className="space-y-3">
              {projects.map(project => (
                <div key={project.id} className="card hover:shadow-lg transition-shadow">
                  <h3 className="font-medium text-gray-900 mb-2">{project.name}</h3>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">
                      {project.completedTasks || 0} / {project.totalTasks || 0} tasks
                    </span>
                    <span className="text-primary-600 font-medium">
                      {project.completionPercentage || 0}%
                    </span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
                    <div
                      className="bg-primary-600 h-2 rounded-full transition-all"
                      style={{ width: `${project.completionPercentage || 0}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
