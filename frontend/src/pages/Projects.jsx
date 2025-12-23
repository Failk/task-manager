import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { FiPlus, FiFolder, FiEdit, FiTrash2, FiArchive } from 'react-icons/fi';
import projectService from '../services/project.service';

const Projects = () => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingProject, setEditingProject] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startDate: '',
    dueDate: '',
  });

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      setLoading(true);
      const data = await projectService.getAllProjects();
      setProjects(data);
    } catch (error) {
      toast.error('Failed to load projects');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (editingProject) {
        await projectService.updateProject(editingProject.id, formData);
        toast.success('Project updated successfully');
      } else {
        await projectService.createProject(formData);
        toast.success('Project created successfully');
      }

      setShowForm(false);
      setEditingProject(null);
      setFormData({ name: '', description: '', startDate: '', dueDate: '' });
      loadProjects();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save project');
    }
  };

  const handleEdit = (project) => {
    setEditingProject(project);
    setFormData({
      name: project.name,
      description: project.description || '',
      startDate: project.startDate || '',
      dueDate: project.dueDate || '',
    });
    setShowForm(true);
  };

  const handleDelete = async (project) => {
    if (!window.confirm(`Are you sure you want to delete "${project.name}"?`)) {
      return;
    }

    try {
      await projectService.deleteProject(project.id);
      toast.success('Project deleted');
      loadProjects();
    } catch (error) {
      toast.error('Failed to delete project');
    }
  };

  const handleArchive = async (project) => {
    try {
      if (project.archived) {
        await projectService.unarchiveProject(project.id);
        toast.success('Project unarchived');
      } else {
        await projectService.archiveProject(project.id);
        toast.success('Project archived');
      }
      loadProjects();
    } catch (error) {
      toast.error('Failed to archive project');
    }
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
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Projects</h1>
          <p className="text-gray-600 mt-1">{projects.length} projects</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="btn-primary flex items-center gap-2"
        >
          <FiPlus />
          New Project
        </button>
      </div>

      {projects.length === 0 ? (
        <div className="card text-center py-12">
          <FiFolder className="mx-auto text-gray-400 mb-4" size={48} />
          <p className="text-gray-500 text-lg">No projects yet</p>
          <p className="text-gray-400 mt-2">Create your first project to organize your tasks</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map(project => (
            <div
              key={project.id}
              className={`card hover:shadow-lg transition-shadow ${
                project.archived ? 'opacity-60' : ''
              }`}
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <h3 className="text-lg font-bold text-gray-900 mb-1">{project.name}</h3>
                  {project.archived && (
                    <span className="badge bg-gray-200 text-gray-700">Archived</span>
                  )}
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleEdit(project)}
                    className="p-2 text-gray-400 hover:text-primary-600 hover:bg-primary-50 rounded"
                    title="Edit project"
                  >
                    <FiEdit size={16} />
                  </button>
                  <button
                    onClick={() => handleArchive(project)}
                    className="p-2 text-gray-400 hover:text-amber-600 hover:bg-amber-50 rounded"
                    title={project.archived ? 'Unarchive' : 'Archive'}
                  >
                    <FiArchive size={16} />
                  </button>
                  <button
                    onClick={() => handleDelete(project)}
                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded"
                    title="Delete project"
                  >
                    <FiTrash2 size={16} />
                  </button>
                </div>
              </div>

              {project.description && (
                <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                  {project.description}
                </p>
              )}

              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Progress</span>
                  <span className="text-primary-600 font-medium">
                    {project.completionPercentage || 0}%
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-primary-600 h-2 rounded-full transition-all"
                    style={{ width: `${project.completionPercentage || 0}%` }}
                  />
                </div>
                <div className="flex items-center justify-between text-sm text-gray-600">
                  <span>{project.completedTasks || 0} completed</span>
                  <span>{project.totalTasks || 0} total tasks</span>
                </div>
              </div>

              {(project.startDate || project.dueDate) && (
                <div className="mt-4 pt-4 border-t text-sm text-gray-600">
                  {project.startDate && (
                    <div>Start: {new Date(project.startDate).toLocaleDateString()}</div>
                  )}
                  {project.dueDate && (
                    <div>Due: {new Date(project.dueDate).toLocaleDateString()}</div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg w-full max-w-md">
            <div className="border-b px-6 py-4">
              <h2 className="text-xl font-bold">
                {editingProject ? 'Edit Project' : 'Create Project'}
              </h2>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Project Name *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="input"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="input"
                  rows={3}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Start Date
                  </label>
                  <input
                    type="date"
                    value={formData.startDate}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                    className="input"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Due Date
                  </label>
                  <input
                    type="date"
                    value={formData.dueDate}
                    onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                    className="input"
                  />
                </div>
              </div>

              <div className="flex gap-3 pt-4 border-t">
                <button type="submit" className="btn-primary flex-1">
                  {editingProject ? 'Update' : 'Create'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowForm(false);
                    setEditingProject(null);
                    setFormData({ name: '', description: '', startDate: '', dueDate: '' });
                  }}
                  className="btn-secondary"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Projects;
