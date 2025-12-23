import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { FiUser, FiLock, FiBell, FiSave } from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';
import userService from '../services/user.service';

const Settings = () => {
  const { user, updateUser } = useAuth();
  const [activeTab, setActiveTab] = useState('profile');

  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    email: '',
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [notificationPrefs, setNotificationPrefs] = useState({
    emailEnabled: true,
    popupEnabled: true,
    dailyDigestEnabled: false,
    dailyDigestTime: '09:00',
    reminderLeadTimeMinutes: 30,
    overdueNotificationsEnabled: true,
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      setProfileData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
      });
    }
    loadNotificationPreferences();
  }, [user]);

  const loadNotificationPreferences = async () => {
    try {
      const prefs = await userService.getNotificationPreferences();
      setNotificationPrefs(prefs);
    } catch (error) {
      console.error('Failed to load notification preferences:', error);
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const updated = await userService.updateProfile(profileData);
      updateUser(updated);
      toast.success('Profile updated successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }

    if (passwordData.newPassword.length < 8) {
      toast.error('Password must be at least 8 characters long');
      return;
    }

    setLoading(true);

    try {
      await userService.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      toast.success('Password changed successfully');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to change password');
    } finally {
      setLoading(false);
    }
  };

  const handleNotificationSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await userService.updateNotificationPreferences(notificationPrefs);
      toast.success('Notification preferences updated');
    } catch (error) {
      toast.error('Failed to update notification preferences');
    } finally {
      setLoading(false);
    }
  };

  const tabs = [
    { id: 'profile', label: 'Profile', icon: FiUser },
    { id: 'password', label: 'Password', icon: FiLock },
    { id: 'notifications', label: 'Notifications', icon: FiBell },
  ];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Settings</h1>
        <p className="text-gray-600 mt-1">Manage your account settings and preferences</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div className="lg:col-span-1">
          <div className="card space-y-2">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    activeTab === tab.id
                      ? 'bg-primary-50 text-primary-600 font-medium'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <Icon size={20} />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="lg:col-span-3">
          {activeTab === 'profile' && (
            <div className="card">
              <h2 className="text-xl font-bold mb-6">Profile Information</h2>

              <form onSubmit={handleProfileSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      First Name
                    </label>
                    <input
                      type="text"
                      value={profileData.firstName}
                      onChange={(e) =>
                        setProfileData({ ...profileData, firstName: e.target.value })
                      }
                      className="input"
                      required
                      minLength={2}
                      maxLength={50}
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Last Name
                    </label>
                    <input
                      type="text"
                      value={profileData.lastName}
                      onChange={(e) =>
                        setProfileData({ ...profileData, lastName: e.target.value })
                      }
                      className="input"
                      required
                      minLength={2}
                      maxLength={50}
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Email Address
                  </label>
                  <input
                    type="email"
                    value={profileData.email}
                    onChange={(e) =>
                      setProfileData({ ...profileData, email: e.target.value })
                    }
                    className="input"
                    required
                  />
                </div>

                <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2">
                  <FiSave />
                  {loading ? 'Saving...' : 'Save Changes'}
                </button>
              </form>
            </div>
          )}

          {activeTab === 'password' && (
            <div className="card">
              <h2 className="text-xl font-bold mb-6">Change Password</h2>

              <form onSubmit={handlePasswordSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Current Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.currentPassword}
                    onChange={(e) =>
                      setPasswordData({ ...passwordData, currentPassword: e.target.value })
                    }
                    className="input"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    New Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.newPassword}
                    onChange={(e) =>
                      setPasswordData({ ...passwordData, newPassword: e.target.value })
                    }
                    className="input"
                    required
                    minLength={8}
                  />
                  <p className="text-xs text-gray-500 mt-1">Minimum 8 characters</p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.confirmPassword}
                    onChange={(e) =>
                      setPasswordData({ ...passwordData, confirmPassword: e.target.value })
                    }
                    className="input"
                    required
                  />
                </div>

                <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2">
                  <FiSave />
                  {loading ? 'Changing...' : 'Change Password'}
                </button>
              </form>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="card">
              <h2 className="text-xl font-bold mb-6">Notification Preferences</h2>

              <form onSubmit={handleNotificationSubmit} className="space-y-6">
                <div>
                  <h3 className="text-sm font-medium text-gray-900 mb-3">Notification Channels</h3>
                  <div className="space-y-3">
                    <label className="flex items-center gap-3">
                      <input
                        type="checkbox"
                        checked={notificationPrefs.emailEnabled}
                        onChange={(e) =>
                          setNotificationPrefs({
                            ...notificationPrefs,
                            emailEnabled: e.target.checked,
                          })
                        }
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                      <div>
                        <div className="font-medium text-gray-900">Email Notifications</div>
                        <div className="text-sm text-gray-600">
                          Receive notifications via email
                        </div>
                      </div>
                    </label>

                    <label className="flex items-center gap-3">
                      <input
                        type="checkbox"
                        checked={notificationPrefs.popupEnabled}
                        onChange={(e) =>
                          setNotificationPrefs({
                            ...notificationPrefs,
                            popupEnabled: e.target.checked,
                          })
                        }
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                      <div>
                        <div className="font-medium text-gray-900">Popup Notifications</div>
                        <div className="text-sm text-gray-600">
                          Show in-app popup notifications
                        </div>
                      </div>
                    </label>
                  </div>
                </div>

                <div>
                  <h3 className="text-sm font-medium text-gray-900 mb-3">Daily Digest</h3>
                  <label className="flex items-center gap-3 mb-3">
                    <input
                      type="checkbox"
                      checked={notificationPrefs.dailyDigestEnabled}
                      onChange={(e) =>
                        setNotificationPrefs({
                          ...notificationPrefs,
                          dailyDigestEnabled: e.target.checked,
                        })
                      }
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                    <div>
                      <div className="font-medium text-gray-900">Enable Daily Digest</div>
                      <div className="text-sm text-gray-600">
                        Receive a daily summary of your tasks
                      </div>
                    </div>
                  </label>

                  {notificationPrefs.dailyDigestEnabled && (
                    <div className="ml-7">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Digest Time
                      </label>
                      <input
                        type="time"
                        value={notificationPrefs.dailyDigestTime || '09:00'}
                        onChange={(e) =>
                          setNotificationPrefs({
                            ...notificationPrefs,
                            dailyDigestTime: e.target.value,
                          })
                        }
                        className="input max-w-xs"
                      />
                    </div>
                  )}
                </div>

                <div>
                  <h3 className="text-sm font-medium text-gray-900 mb-3">Reminder Settings</h3>
                  <div className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Default Reminder Lead Time
                      </label>
                      <select
                        value={notificationPrefs.reminderLeadTimeMinutes}
                        onChange={(e) =>
                          setNotificationPrefs({
                            ...notificationPrefs,
                            reminderLeadTimeMinutes: parseInt(e.target.value),
                          })
                        }
                        className="input max-w-xs"
                      >
                        <option value={15}>15 minutes</option>
                        <option value={30}>30 minutes</option>
                        <option value={60}>1 hour</option>
                        <option value={120}>2 hours</option>
                        <option value={1440}>1 day</option>
                      </select>
                    </div>

                    <label className="flex items-center gap-3">
                      <input
                        type="checkbox"
                        checked={notificationPrefs.overdueNotificationsEnabled}
                        onChange={(e) =>
                          setNotificationPrefs({
                            ...notificationPrefs,
                            overdueNotificationsEnabled: e.target.checked,
                          })
                        }
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                      <div>
                        <div className="font-medium text-gray-900">Overdue Notifications</div>
                        <div className="text-sm text-gray-600">
                          Get notified about overdue tasks
                        </div>
                      </div>
                    </label>
                  </div>
                </div>

                <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2">
                  <FiSave />
                  {loading ? 'Saving...' : 'Save Preferences'}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Settings;
