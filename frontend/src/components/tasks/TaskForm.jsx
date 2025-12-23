import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { FiX, FiPlus, FiTrash2 } from 'react-icons/fi';
import projectService from '../../services/project.service';
import contextService from '../../services/context.service';

const TaskForm = ({ task, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'B',
    dueDate: '',
    dueTime: '',
    estimatedDuration: '',
    projectId: '',
    contextIds: [],
    taskType: 'ONE_TIME',
    recurrencePattern: {
      frequency: 'DAILY',
      interval: 1,
      daysOfWeek: [],
      dayOfMonth: null,
      startDate: '',
      endDate: '',
      occurrences: null,
    },
    reminders: [],
  });

  const [projects, setProjects] = useState([]);
  const [contexts, setContexts] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadProjects();
    loadContexts();
    if (task) {
      setFormData({
        title: task.title || '',
        description: task.description || '',
        priority: task.priority || 'B',
        dueDate: task.dueDate || '',
        dueTime: task.dueTime || '',
        estimatedDuration: task.estimatedDuration || '',
        projectId: task.projectId || '',
        contextIds: task.contexts?.map(c => c.id) || [],
        taskType: task.taskType || 'ONE_TIME',
        recurrencePattern: task.recurrencePattern || {
          frequency: 'DAILY',
          interval: 1,
          daysOfWeek: [],
          dayOfMonth: null,
          startDate: '',
          endDate: '',
          occurrences: null,
        },
        reminders: task.reminders || [],
      });
    }
  }, [task]);

  const loadProjects = async () => {
    try {
      const data = await projectService.getAllProjects();
      setProjects(data);
    } catch (error) {
      console.error('Failed to load projects:', error);
    }
  };

  const loadContexts = async () => {
    try {
      const data = await contextService.getAllContexts();
      setContexts(data);
    } catch (error) {
      console.error('Failed to load contexts:', error);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name.startsWith('recurrencePattern.')) {
      const field = name.split('.')[1];
      setFormData(prev => ({
        ...prev,
        recurrencePattern: {
          ...prev.recurrencePattern,
          [field]: value,
        },
      }));
    } else if (name === 'contextIds') {
      const contextId = parseInt(value);
      setFormData(prev => ({
        ...prev,
        contextIds: checked
          ? [...prev.contextIds, contextId]
          : prev.contextIds.filter(id => id !== contextId),
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: type === 'number' ? (value ? parseInt(value) : '') : value,
      }));
    }
  };

  const addReminder = () => {
    setFormData(prev => ({
      ...prev,
      reminders: [
        ...prev.reminders,
        { leadTimeMinutes: 30, notificationType: 'POPUP' },
      ],
    }));
  };

  const removeReminder = (index) => {
    setFormData(prev => ({
      ...prev,
      reminders: prev.reminders.filter((_, i) => i !== index),
    }));
  };

  const updateReminder = (index, field, value) => {
    setFormData(prev => ({
      ...prev,
      reminders: prev.reminders.map((reminder, i) =>
        i === index ? { ...reminder, [field]: value } : reminder
      ),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const submitData = {
        ...formData,
        projectId: formData.projectId || null,
        estimatedDuration: formData.estimatedDuration || null,
      };

      if (formData.taskType === 'ONE_TIME') {
        delete submitData.recurrencePattern;
      }

      await onSubmit(submitData);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save task');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
          <h2 className="text-xl font-bold">{task ? 'Edit Task' : 'Create Task'}</h2>
          <button onClick={onCancel} className="text-gray-400 hover:text-gray-600">
            <FiX size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Title *
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className="input"
              required
              maxLength={200}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="input"
              rows={3}
              maxLength={2000}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Priority
              </label>
              <select
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                className="input"
              >
                <option value="A">A - Critical (Must be done today)</option>
                <option value="B">B - Important (Should be done soon)</option>
                <option value="C">C - Nice to have (Can wait)</option>
                <option value="D">D - Delegate or Defer</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Task Type
              </label>
              <select
                name="taskType"
                value={formData.taskType}
                onChange={handleChange}
                className="input"
              >
                <option value="ONE_TIME">One-Time</option>
                <option value="RECURRING">Recurring</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Due Date
              </label>
              <input
                type="date"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleChange}
                className="input"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Due Time
              </label>
              <input
                type="time"
                name="dueTime"
                value={formData.dueTime}
                onChange={handleChange}
                className="input"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Estimated Duration (minutes)
              </label>
              <input
                type="number"
                name="estimatedDuration"
                value={formData.estimatedDuration}
                onChange={handleChange}
                className="input"
                min="0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Project
              </label>
              <select
                name="projectId"
                value={formData.projectId}
                onChange={handleChange}
                className="input"
              >
                <option value="">None</option>
                {projects.map(project => (
                  <option key={project.id} value={project.id}>
                    {project.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Contexts
            </label>
            <div className="flex flex-wrap gap-3">
              {contexts.map(context => (
                <label key={context.id} className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    name="contextIds"
                    value={context.id}
                    checked={formData.contextIds.includes(context.id)}
                    onChange={handleChange}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm">{context.name}</span>
                </label>
              ))}
            </div>
          </div>

          {formData.taskType === 'RECURRING' && (
            <div className="border rounded-lg p-4 space-y-4">
              <h3 className="font-medium">Recurrence Pattern</h3>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Frequency *
                  </label>
                  <select
                    name="recurrencePattern.frequency"
                    value={formData.recurrencePattern.frequency}
                    onChange={handleChange}
                    className="input"
                    required={formData.taskType === 'RECURRING'}
                  >
                    <option value="DAILY">Daily</option>
                    <option value="WEEKLY">Weekly</option>
                    <option value="BIWEEKLY">Bi-weekly</option>
                    <option value="MONTHLY">Monthly</option>
                    <option value="YEARLY">Yearly</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Interval
                  </label>
                  <input
                    type="number"
                    name="recurrencePattern.interval"
                    value={formData.recurrencePattern.interval}
                    onChange={handleChange}
                    className="input"
                    min="1"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Start Date *
                  </label>
                  <input
                    type="date"
                    name="recurrencePattern.startDate"
                    value={formData.recurrencePattern.startDate}
                    onChange={handleChange}
                    className="input"
                    required={formData.taskType === 'RECURRING'}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    End Date
                  </label>
                  <input
                    type="date"
                    name="recurrencePattern.endDate"
                    value={formData.recurrencePattern.endDate}
                    onChange={handleChange}
                    className="input"
                  />
                </div>
              </div>
            </div>
          )}

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-sm font-medium text-gray-700">
                Reminders
              </label>
              <button
                type="button"
                onClick={addReminder}
                className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1"
              >
                <FiPlus size={16} />
                Add Reminder
              </button>
            </div>
            {formData.reminders.map((reminder, index) => (
              <div key={index} className="flex gap-2 mb-2">
                <input
                  type="number"
                  value={reminder.leadTimeMinutes}
                  onChange={(e) => updateReminder(index, 'leadTimeMinutes', parseInt(e.target.value))}
                  className="input flex-1"
                  placeholder="Minutes before"
                  min="0"
                />
                <select
                  value={reminder.notificationType}
                  onChange={(e) => updateReminder(index, 'notificationType', e.target.value)}
                  className="input flex-1"
                >
                  <option value="POPUP">Popup</option>
                  <option value="EMAIL">Email</option>
                  <option value="BOTH">Both</option>
                </select>
                <button
                  type="button"
                  onClick={() => removeReminder(index)}
                  className="p-2 text-red-600 hover:bg-red-50 rounded"
                >
                  <FiTrash2 size={16} />
                </button>
              </div>
            ))}
          </div>

          <div className="flex gap-3 pt-4 border-t">
            <button type="submit" disabled={loading} className="btn-primary flex-1">
              {loading ? 'Saving...' : task ? 'Update Task' : 'Create Task'}
            </button>
            <button type="button" onClick={onCancel} className="btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TaskForm;
