import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import {
  User,
  Bell,
  Palette,
  Shield,
  Save,
  Check,
  LogOut,
} from 'lucide-react';
import { userService } from '../services/userService';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import Input from '../components/common/Input';

type TabType = 'profile' | 'notifications' | 'appearance' | 'security';

const Settings: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('profile');
  const { logout } = useAuthStore();
  const navigate = useNavigate();

  const tabs = [
    { id: 'profile' as TabType, label: 'Profile', icon: User },
    { id: 'notifications' as TabType, label: 'Notifications', icon: Bell },
    { id: 'appearance' as TabType, label: 'Appearance', icon: Palette },
    { id: 'security' as TabType, label: 'Security', icon: Shield },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Settings</h1>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="flex border-b border-gray-200">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-6 py-4 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? 'text-blue-600 border-b-2 border-blue-600 -mb-px'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <tab.icon className="w-4 h-4" />
              {tab.label}
            </button>
          ))}
        </div>

        <div className="p-6">
          {activeTab === 'profile' && <ProfileSettings />}
          {activeTab === 'notifications' && <NotificationSettings />}
          {activeTab === 'appearance' && <AppearanceSettings />}
          {activeTab === 'security' && <SecuritySettings onLogout={handleLogout} />}
        </div>
      </div>
    </div>
  );
};

const ProfileSettings: React.FC = () => {
  const { user } = useAuthStore();
  const [formData, setFormData] = useState({
    fullName: user?.fullName || '',
    username: user?.username || '',
    email: user?.email || '',
  });
  const [saved, setSaved] = useState(false);

  const updateMutation = useMutation({
    mutationFn: (data: typeof formData) => userService.updateProfile(data),
    onSuccess: () => {
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Profile Information</h3>
        <p className="text-sm text-gray-500 mb-6">
          Update your personal information and how others see you on the platform.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Input
          label="Full Name"
          value={formData.fullName}
          onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
          placeholder="Enter your full name"
        />
        <Input
          label="Username"
          value={formData.username}
          onChange={(e) => setFormData({ ...formData, username: e.target.value })}
          placeholder="Enter your username"
        />
      </div>

      <Input
        label="Email Address"
        type="email"
        value={formData.email}
        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
        placeholder="Enter your email"
        disabled
        helperText="Email cannot be changed"
      />

      <div className="flex items-center gap-4">
        <Button type="submit" disabled={updateMutation.isPending}>
          {updateMutation.isPending ? (
            'Saving...'
          ) : saved ? (
            <>
              <Check className="w-4 h-4 mr-2" />
              Saved
            </>
          ) : (
            <>
              <Save className="w-4 h-4 mr-2" />
              Save Changes
            </>
          )}
        </Button>
      </div>
    </form>
  );
};

const NotificationSettings: React.FC = () => {
  const [settings, setSettings] = useState({
    emailReminders: true,
    emailDueDates: true,
    emailOverdue: true,
    pushReminders: true,
    pushDueDates: false,
    pushOverdue: true,
    reminderTime: '09:00',
  });
  const [saved, setSaved] = useState(false);

  const handleToggle = (key: keyof typeof settings) => {
    if (typeof settings[key] === 'boolean') {
      setSettings({ ...settings, [key]: !settings[key] });
    }
  };

  const handleSave = () => {
    // TODO: Implement API call
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const requestNotificationPermission = async () => {
    if ('Notification' in window) {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        new Notification('Notifications enabled!', {
          body: 'You will now receive push notifications for your tasks.',
        });
      }
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Notification Preferences</h3>
        <p className="text-sm text-gray-500 mb-6">
          Choose how and when you want to be notified about your tasks.
        </p>
      </div>

      {/* Email Notifications */}
      <div className="space-y-4">
        <h4 className="font-medium text-gray-900">Email Notifications</h4>
        
        <label className="flex items-center justify-between p-4 bg-gray-50 rounded-lg cursor-pointer">
          <div>
            <p className="font-medium text-gray-900">Task Reminders</p>
            <p className="text-sm text-gray-500">Get email reminders for upcoming tasks</p>
          </div>
          <input
            type="checkbox"
            checked={settings.emailReminders}
            onChange={() => handleToggle('emailReminders')}
            className="w-5 h-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
          />
        </label>

        <label className="flex items-center justify-between p-4 bg-gray-50 rounded-lg cursor-pointer">
          <div>
            <p className="font-medium text-gray-900">Due Date Alerts</p>
            <p className="text-sm text-gray-500">Get notified when tasks are due today</p>
          </div>
          <input
            type="checkbox"
            checked={settings.emailDueDates}
            onChange={() => handleToggle('emailDueDates')}
            className="w-5 h-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
          />
        </label>

        <label className="flex items-center justify-between p-4 bg-gray-50 rounded-lg cursor-pointer">
          <div>
            <p className="font-medium text-gray-900">Overdue Notifications</p>
            <p className="text-sm text-gray-500">Get notified when tasks become overdue</p>
          </div>
          <input
            type="checkbox"
            checked={settings.emailOverdue}
            onChange={() => handleToggle('emailOverdue')}
            className="w-5 h-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
          />
        </label>
      </div>

      {/* Push Notifications */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h4 className="font-medium text-gray-900">Push Notifications</h4>
          <Button variant="secondary" size="sm" onClick={requestNotificationPermission}>
            Enable Browser Notifications
          </Button>
        </div>

        <label className="flex items-center justify-between p-4 bg-gray-50 rounded-lg cursor-pointer">
          <div>
            <p className="font-medium text-gray-900">Task Reminders</p>
            <p className="text-sm text-gray-500">Get push notifications for task reminders</p>
          </div>
          <input
            type="checkbox"
            checked={settings.pushReminders}
            onChange={() => handleToggle('pushReminders')}
            className="w-5 h-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
          />
        </label>

        <label className="flex items-center justify-between p-4 bg-gray-50 rounded-lg cursor-pointer">
          <div>
            <p className="font-medium text-gray-900">Overdue Alerts</p>
            <p className="text-sm text-gray-500">Get push notifications for overdue tasks</p>
          </div>
          <input
            type="checkbox"
            checked={settings.pushOverdue}
            onChange={() => handleToggle('pushOverdue')}
            className="w-5 h-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
          />
        </label>
      </div>

      {/* Default Reminder Time */}
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          Default Daily Reminder Time
        </label>
        <input
          type="time"
          value={settings.reminderTime}
          onChange={(e) => setSettings({ ...settings, reminderTime: e.target.value })}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
        <p className="text-sm text-gray-500">
          Time when daily task summaries are sent
        </p>
      </div>

      <Button onClick={handleSave}>
        {saved ? (
          <>
            <Check className="w-4 h-4 mr-2" />
            Saved
          </>
        ) : (
          <>
            <Save className="w-4 h-4 mr-2" />
            Save Preferences
          </>
        )}
      </Button>
    </div>
  );
};

const AppearanceSettings: React.FC = () => {
  const [theme, setTheme] = useState('light');
  const [compactMode, setCompactMode] = useState(false);

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Appearance</h3>
        <p className="text-sm text-gray-500 mb-6">
          Customize how the application looks and feels.
        </p>
      </div>

      <div className="space-y-4">
        <h4 className="font-medium text-gray-900">Theme</h4>
        <div className="grid grid-cols-3 gap-4">
          {['light', 'dark', 'system'].map((t) => (
            <button
              key={t}
              onClick={() => setTheme(t)}
              className={`p-4 border-2 rounded-lg text-center transition-colors ${
                theme === t
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <div
                className={`w-8 h-8 mx-auto mb-2 rounded-full ${
                  t === 'light'
                    ? 'bg-yellow-400'
                    : t === 'dark'
                    ? 'bg-gray-800'
                    : 'bg-gradient-to-r from-yellow-400 to-gray-800'
                }`}
              />
              <p className="font-medium capitalize">{t}</p>
            </button>
          ))}
        </div>
        <p className="text-sm text-gray-500">
          Note: Dark mode is coming soon!
        </p>
      </div>

      <label className="flex items-center justify-between p-4 bg-gray-50 rounded-lg cursor-pointer">
        <div>
          <p className="font-medium text-gray-900">Compact Mode</p>
          <p className="text-sm text-gray-500">Show more content with reduced spacing</p>
        </div>
        <input
          type="checkbox"
          checked={compactMode}
          onChange={() => setCompactMode(!compactMode)}
          className="w-5 h-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
        />
      </label>
    </div>
  );
};

interface SecuritySettingsProps {
  onLogout: () => void;
}

const SecuritySettings: React.FC<SecuritySettingsProps> = ({ onLogout }) => {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);

  const handleChangePassword = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    // TODO: Implement API call
    setSaved(true);
    setTimeout(() => {
      setSaved(false);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    }, 3000);
  };

  return (
    <div className="space-y-8">
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Security Settings</h3>
        <p className="text-sm text-gray-500 mb-6">
          Manage your account security and authentication.
        </p>
      </div>

      {/* Change Password */}
      <form onSubmit={handleChangePassword} className="space-y-4">
        <h4 className="font-medium text-gray-900">Change Password</h4>

        <Input
          label="Current Password"
          type="password"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          placeholder="Enter current password"
        />

        <Input
          label="New Password"
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          placeholder="Enter new password"
          helperText="Must be at least 8 characters"
        />

        <Input
          label="Confirm New Password"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="Confirm new password"
          error={error}
        />

        <Button type="submit">
          {saved ? (
            <>
              <Check className="w-4 h-4 mr-2" />
              Password Changed
            </>
          ) : (
            'Change Password'
          )}
        </Button>
      </form>

      {/* Danger Zone */}
      <div className="pt-6 border-t border-gray-200">
        <h4 className="font-medium text-red-600 mb-4">Danger Zone</h4>
        
        <div className="p-4 border border-red-200 rounded-lg bg-red-50">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium text-gray-900">Sign out of all devices</p>
              <p className="text-sm text-gray-500">
                This will log you out from all devices and sessions
              </p>
            </div>
            <Button variant="danger" onClick={onLogout}>
              <LogOut className="w-4 h-4 mr-2" />
              Sign Out
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Settings;
