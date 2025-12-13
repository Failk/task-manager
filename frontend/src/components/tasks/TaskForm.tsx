import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { Button, Input, Select, Modal } from '../common';
import {
  Priority,
  Frequency,
  EndCondition,
  ReminderType,
  Project,
  Context,
  CreateOneTimeTaskRequest,
  CreateRecurringTaskRequest,
  CreateReminderRequest,
} from '../../types';
import { projectService } from '../../services/projectService';
import { contextService } from '../../services/contextService';

interface TaskFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateOneTimeTaskRequest | CreateRecurringTaskRequest) => Promise<void>;
}

const TaskForm: React.FC<TaskFormProps> = ({ isOpen, onClose, onSubmit }) => {
  const [loading, setLoading] = useState(false);
  const [taskType, setTaskType] = useState<'one-time' | 'recurring'>('one-time');
  const [projects, setProjects] = useState<Project[]>([]);
  const [contexts, setContexts] = useState<Context[]>([]);
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    dueDate: format(new Date(), 'yyyy-MM-dd'),
    priority: Priority.B,
    estimatedDurationMinutes: 30,
    projectId: '',
    contextTags: [] as string[],
    // Recurring fields
    frequency: Frequency.DAILY,
    interval: 1,
    daysOfWeek: [] as number[],
    dayOfMonth: 1,
    endCondition: EndCondition.NEVER,
    occurrenceCount: 10,
    endDate: format(new Date(Date.now() + 90 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
    // Reminders
    addReminder: false,
    reminderType: ReminderType.POPUP,
    reminderLeadTime: 30,
  });

  useEffect(() => {
    if (isOpen) {
      loadData();
    }
  }, [isOpen]);

  const loadData = async () => {
    try {
      const [projectsData, contextsData] = await Promise.all([
        projectService.getAll(),
        contextService.getAll(),
      ]);
      setProjects(projectsData);
      setContexts(contextsData);
    } catch (error) {
      console.error('Failed to load data:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const reminders: CreateReminderRequest[] = formData.addReminder
        ? [{ type: formData.reminderType, leadTimeMinutes: formData.reminderLeadTime }]
        : [];

      if (taskType === 'one-time') {
        const data: CreateOneTimeTaskRequest = {
          title: formData.title,
          description: formData.description || undefined,
          dueDate: formData.dueDate + 'T09:00:00',
          priority: formData.priority,
          estimatedDurationMinutes: formData.estimatedDurationMinutes,
          projectId: formData.projectId ? parseInt(formData.projectId) : undefined,
          contextTags: formData.contextTags.length > 0 ? formData.contextTags : undefined,
          reminders: reminders.length > 0 ? reminders : undefined,
        };
        await onSubmit(data);
      } else {
        const data: CreateRecurringTaskRequest = {
          title: formData.title,
          description: formData.description || undefined,
          startDate: formData.dueDate,
          priority: formData.priority,
          estimatedDurationMinutes: formData.estimatedDurationMinutes,
          projectId: formData.projectId ? parseInt(formData.projectId) : undefined,
          contextTags: formData.contextTags.length > 0 ? formData.contextTags : undefined,
          reminders: reminders.length > 0 ? reminders : undefined,
          frequency: formData.frequency,
          interval: formData.interval,
          daysOfWeek: formData.frequency === Frequency.WEEKLY ? formData.daysOfWeek : undefined,
          dayOfMonth: formData.frequency === Frequency.MONTHLY ? formData.dayOfMonth : undefined,
          endCondition: formData.endCondition,
          occurrenceCount: formData.endCondition === EndCondition.AFTER_OCCURRENCES ? formData.occurrenceCount : undefined,
          endDate: formData.endCondition === EndCondition.BY_DATE ? formData.endDate : undefined,
        };
        await onSubmit(data);
      }

      // Reset form
      setFormData({
        title: '',
        description: '',
        dueDate: format(new Date(), 'yyyy-MM-dd'),
        priority: Priority.B,
        estimatedDurationMinutes: 30,
        projectId: '',
        contextTags: [],
        frequency: Frequency.DAILY,
        interval: 1,
        daysOfWeek: [],
        dayOfMonth: 1,
        endCondition: EndCondition.NEVER,
        occurrenceCount: 10,
        endDate: format(new Date(Date.now() + 90 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
        addReminder: false,
        reminderType: ReminderType.POPUP,
        reminderLeadTime: 30,
      });
      onClose();
    } catch (error) {
      console.error('Failed to create task:', error);
    } finally {
      setLoading(false);
    }
  };

  const toggleContext = (contextName: string) => {
    setFormData((prev) => ({
      ...prev,
      contextTags: prev.contextTags.includes(contextName)
        ? prev.contextTags.filter((c) => c !== contextName)
        : [...prev.contextTags, contextName],
    }));
  };

  const toggleDayOfWeek = (day: number) => {
    setFormData((prev) => ({
      ...prev,
      daysOfWeek: prev.daysOfWeek.includes(day)
        ? prev.daysOfWeek.filter((d) => d !== day)
        : [...prev.daysOfWeek, day],
    }));
  };

  const priorityOptions = [
    { value: Priority.A, label: 'A - Critical' },
    { value: Priority.B, label: 'B - Important' },
    { value: Priority.C, label: 'C - Complete When Possible' },
    { value: Priority.D, label: 'D - Delegate/Defer' },
  ];

  const frequencyOptions = [
    { value: Frequency.DAILY, label: 'Daily' },
    { value: Frequency.WEEKLY, label: 'Weekly' },
    { value: Frequency.MONTHLY, label: 'Monthly' },
  ];

  const endConditionOptions = [
    { value: EndCondition.NEVER, label: 'Never' },
    { value: EndCondition.AFTER_OCCURRENCES, label: 'After N occurrences' },
    { value: EndCondition.BY_DATE, label: 'By date' },
  ];

  const daysOfWeekLabels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Task" size="lg">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Task Type Toggle */}
        <div className="flex gap-2 p-1 bg-gray-100 rounded-lg">
          <button
            type="button"
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              taskType === 'one-time'
                ? 'bg-white shadow text-gray-900'
                : 'text-gray-500 hover:text-gray-900'
            }`}
            onClick={() => setTaskType('one-time')}
          >
            One-time Task
          </button>
          <button
            type="button"
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              taskType === 'recurring'
                ? 'bg-white shadow text-gray-900'
                : 'text-gray-500 hover:text-gray-900'
            }`}
            onClick={() => setTaskType('recurring')}
          >
            Recurring Task
          </button>
        </div>

        {/* Basic Fields */}
        <Input
          label="Title"
          required
          value={formData.title}
          onChange={(e) => setFormData({ ...formData, title: e.target.value })}
          placeholder="Enter task title"
        />

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description
          </label>
          <textarea
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Enter task description (optional)"
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label={taskType === 'one-time' ? 'Due Date' : 'Start Date'}
            type="date"
            required
            value={formData.dueDate}
            onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
          />

          <Select
            label="Priority"
            value={formData.priority}
            onChange={(e) => setFormData({ ...formData, priority: e.target.value as Priority })}
            options={priorityOptions}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Duration (minutes)"
            type="number"
            min={5}
            value={formData.estimatedDurationMinutes}
            onChange={(e) => setFormData({ ...formData, estimatedDurationMinutes: parseInt(e.target.value) })}
          />

          <Select
            label="Project"
            value={formData.projectId}
            onChange={(e) => setFormData({ ...formData, projectId: e.target.value })}
            options={[
              { value: '', label: 'No Project' },
              ...projects.map((p) => ({ value: p.id.toString(), label: p.name })),
            ]}
          />
        </div>

        {/* Context Tags */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Context Tags
          </label>
          <div className="flex flex-wrap gap-2">
            {contexts.map((context) => (
              <button
                key={context.id}
                type="button"
                onClick={() => toggleContext(context.name)}
                className={`px-3 py-1 text-sm rounded-full border transition-colors ${
                  formData.contextTags.includes(context.name)
                    ? 'bg-blue-100 border-blue-300 text-blue-700'
                    : 'bg-gray-50 border-gray-200 text-gray-600 hover:bg-gray-100'
                }`}
              >
                {context.name}
              </button>
            ))}
          </div>
        </div>

        {/* Recurring Task Fields */}
        {taskType === 'recurring' && (
          <div className="space-y-4 pt-4 border-t">
            <h4 className="font-medium text-gray-900">Recurrence Pattern</h4>

            <div className="grid grid-cols-2 gap-4">
              <Select
                label="Frequency"
                value={formData.frequency}
                onChange={(e) => setFormData({ ...formData, frequency: e.target.value as Frequency })}
                options={frequencyOptions}
              />

              <Input
                label="Every"
                type="number"
                min={1}
                value={formData.interval}
                onChange={(e) => setFormData({ ...formData, interval: parseInt(e.target.value) })}
              />
            </div>

            {formData.frequency === Frequency.WEEKLY && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Days of Week
                </label>
                <div className="flex gap-2">
                  {daysOfWeekLabels.map((day, index) => (
                    <button
                      key={day}
                      type="button"
                      onClick={() => toggleDayOfWeek(index)}
                      className={`w-10 h-10 rounded-full text-sm font-medium transition-colors ${
                        formData.daysOfWeek.includes(index)
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      {day}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {formData.frequency === Frequency.MONTHLY && (
              <Input
                label="Day of Month"
                type="number"
                min={1}
                max={31}
                value={formData.dayOfMonth}
                onChange={(e) => setFormData({ ...formData, dayOfMonth: parseInt(e.target.value) })}
              />
            )}

            <Select
              label="End Condition"
              value={formData.endCondition}
              onChange={(e) => setFormData({ ...formData, endCondition: e.target.value as EndCondition })}
              options={endConditionOptions}
            />

            {formData.endCondition === EndCondition.AFTER_OCCURRENCES && (
              <Input
                label="Number of Occurrences"
                type="number"
                min={1}
                value={formData.occurrenceCount}
                onChange={(e) => setFormData({ ...formData, occurrenceCount: parseInt(e.target.value) })}
              />
            )}

            {formData.endCondition === EndCondition.BY_DATE && (
              <Input
                label="End Date"
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
              />
            )}
          </div>
        )}

        {/* Reminder */}
        <div className="pt-4 border-t">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={formData.addReminder}
              onChange={(e) => setFormData({ ...formData, addReminder: e.target.checked })}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span className="text-sm font-medium text-gray-700">Add Reminder</span>
          </label>

          {formData.addReminder && (
            <div className="mt-3 grid grid-cols-2 gap-4">
              <Select
                label="Reminder Type"
                value={formData.reminderType}
                onChange={(e) => setFormData({ ...formData, reminderType: e.target.value as ReminderType })}
                options={[
                  { value: ReminderType.POPUP, label: 'Popup' },
                  { value: ReminderType.EMAIL, label: 'Email' },
                ]}
              />
              <Input
                label="Minutes Before"
                type="number"
                min={5}
                value={formData.reminderLeadTime}
                onChange={(e) => setFormData({ ...formData, reminderLeadTime: parseInt(e.target.value) })}
              />
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-3 pt-4">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Create Task
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default TaskForm;
