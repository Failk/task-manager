import React, { useState, useEffect } from 'react';
import { Plus, Archive, MoreVertical, Trash2, Edit } from 'lucide-react';
import { Button, Modal, Input, Card, CardBody } from '../components/common';
import { projectService } from '../services/projectService';
import { Project, CreateProjectRequest } from '../types';
import { cn } from '../utils';

const Projects: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [showArchived, setShowArchived] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [formData, setFormData] = useState<CreateProjectRequest>({
    name: '',
    description: '',
    color: '#3B82F6',
  });

  useEffect(() => {
    loadProjects();
  }, [showArchived]);

  const loadProjects = async () => {
    setLoading(true);
    try {
      const data = await projectService.getAll(showArchived);
      setProjects(data);
    } catch (error) {
      console.error('Failed to load projects:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      await projectService.create(formData);
      setShowCreateModal(false);
      setFormData({ name: '', description: '', color: '#3B82F6' });
      loadProjects();
    } catch (error) {
      console.error('Failed to create project:', error);
    }
  };

  const handleUpdate = async () => {
    if (!editingProject) return;
    try {
      await projectService.update(editingProject.id, formData);
      setEditingProject(null);
      setFormData({ name: '', description: '', color: '#3B82F6' });
      loadProjects();
    } catch (error) {
      console.error('Failed to update project:', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this project?')) return;
    try {
      await projectService.delete(id);
      loadProjects();
    } catch (error) {
      console.error('Failed to delete project:', error);
    }
  };

  const handleArchive = async (id: number) => {
    try {
      await projectService.archive(id);
      loadProjects();
    } catch (error) {
      console.error('Failed to archive project:', error);
    }
  };

  const handleUnarchive = async (id: number) => {
    try {
      await projectService.unarchive(id);
      loadProjects();
    } catch (error) {
      console.error('Failed to unarchive project:', error);
    }
  };

  const openEditModal = (project: Project) => {
    setEditingProject(project);
    setFormData({
      name: project.name,
      description: project.description || '',
      color: project.color,
    });
  };

  const closeModal = () => {
    setShowCreateModal(false);
    setEditingProject(null);
    setFormData({ name: '', description: '', color: '#3B82F6' });
  };

  const colorOptions = [
    '#EF4444', '#F97316', '#EAB308', '#22C55E',
    '#14B8A6', '#3B82F6', '#8B5CF6', '#EC4899',
  ];

  const activeProjects = projects.filter((p) => !p.archived);
  const archivedProjects = projects.filter((p) => p.archived);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Projects</h1>
          <p className="text-gray-500 mt-1">
            Organize your tasks into projects
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant={showArchived ? 'primary' : 'secondary'}
            onClick={() => setShowArchived(!showArchived)}
          >
            <Archive className="w-4 h-4 mr-2" />
            {showArchived ? 'Hide Archived' : 'Show Archived'}
          </Button>
          <Button onClick={() => setShowCreateModal(true)}>
            <Plus className="w-4 h-4 mr-2" />
            New Project
          </Button>
        </div>
      </div>

      {/* Projects Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardBody className="animate-pulse">
                <div className="h-6 bg-gray-200 rounded w-3/4 mb-2" />
                <div className="h-4 bg-gray-200 rounded w-full mb-4" />
                <div className="h-2 bg-gray-200 rounded w-full" />
              </CardBody>
            </Card>
          ))}
        </div>
      ) : (
        <>
          {/* Active Projects */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {activeProjects.map((project) => (
              <ProjectCard
                key={project.id}
                project={project}
                onEdit={() => openEditModal(project)}
                onDelete={() => handleDelete(project.id)}
                onArchive={() => handleArchive(project.id)}
              />
            ))}
          </div>

          {activeProjects.length === 0 && (
            <div className="text-center py-12 text-gray-500">
              No projects yet. Create your first project to get started!
            </div>
          )}

          {/* Archived Projects */}
          {showArchived && archivedProjects.length > 0 && (
            <div className="mt-8">
              <h2 className="text-lg font-semibold text-gray-500 mb-4">Archived Projects</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {archivedProjects.map((project) => (
                  <ProjectCard
                    key={project.id}
                    project={project}
                    onEdit={() => openEditModal(project)}
                    onDelete={() => handleDelete(project.id)}
                    onUnarchive={() => handleUnarchive(project.id)}
                    archived
                  />
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={showCreateModal || editingProject !== null}
        onClose={closeModal}
        title={editingProject ? 'Edit Project' : 'Create New Project'}
      >
        <div className="space-y-4">
          <Input
            label="Project Name"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="Enter project name"
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Enter project description (optional)"
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Color
            </label>
            <div className="flex gap-2">
              {colorOptions.map((color) => (
                <button
                  key={color}
                  type="button"
                  onClick={() => setFormData({ ...formData, color })}
                  className={cn(
                    'w-8 h-8 rounded-full transition-transform',
                    formData.color === color && 'ring-2 ring-offset-2 ring-gray-900 scale-110'
                  )}
                  style={{ backgroundColor: color }}
                />
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4">
            <Button variant="secondary" onClick={closeModal}>
              Cancel
            </Button>
            <Button onClick={editingProject ? handleUpdate : handleCreate}>
              {editingProject ? 'Update' : 'Create'} Project
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

// Project Card Component
interface ProjectCardProps {
  project: Project;
  onEdit: () => void;
  onDelete: () => void;
  onArchive?: () => void;
  onUnarchive?: () => void;
  archived?: boolean;
}

const ProjectCard: React.FC<ProjectCardProps> = ({
  project,
  onEdit,
  onDelete,
  onArchive,
  onUnarchive,
  archived,
}) => {
  const [showMenu, setShowMenu] = useState(false);

  return (
    <Card className={cn(archived && 'opacity-60')}>
      <CardBody>
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div
              className="w-4 h-4 rounded-full"
              style={{ backgroundColor: project.color }}
            />
            <h3 className="font-semibold text-gray-900">{project.name}</h3>
          </div>
          <div className="relative">
            <button
              onClick={() => setShowMenu(!showMenu)}
              className="p-1 text-gray-400 hover:text-gray-600 rounded"
            >
              <MoreVertical className="w-4 h-4" />
            </button>
            {showMenu && (
              <>
                <div
                  className="fixed inset-0 z-10"
                  onClick={() => setShowMenu(false)}
                />
                <div className="absolute right-0 top-full mt-1 w-36 bg-white rounded-lg shadow-lg border z-20">
                  <button
                    onClick={() => {
                      onEdit();
                      setShowMenu(false);
                    }}
                    className="flex items-center gap-2 w-full px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                  >
                    <Edit className="w-4 h-4" />
                    Edit
                  </button>
                  {archived ? (
                    <button
                      onClick={() => {
                        onUnarchive?.();
                        setShowMenu(false);
                      }}
                      className="flex items-center gap-2 w-full px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      <Archive className="w-4 h-4" />
                      Unarchive
                    </button>
                  ) : (
                    <button
                      onClick={() => {
                        onArchive?.();
                        setShowMenu(false);
                      }}
                      className="flex items-center gap-2 w-full px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      <Archive className="w-4 h-4" />
                      Archive
                    </button>
                  )}
                  <button
                    onClick={() => {
                      onDelete();
                      setShowMenu(false);
                    }}
                    className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete
                  </button>
                </div>
              </>
            )}
          </div>
        </div>

        {project.description && (
          <p className="text-sm text-gray-500 mt-2 line-clamp-2">
            {project.description}
          </p>
        )}

        <div className="mt-4">
          <div className="flex items-center justify-between text-sm mb-1">
            <span className="text-gray-500">Progress</span>
            <span className="font-medium">{project.completionPercentage}%</span>
          </div>
          <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all"
              style={{
                width: `${project.completionPercentage}%`,
                backgroundColor: project.color,
              }}
            />
          </div>
          <p className="text-xs text-gray-400 mt-1">
            {project.completedTaskCount} of {project.taskCount} tasks completed
          </p>
        </div>
      </CardBody>
    </Card>
  );
};

export default Projects;
