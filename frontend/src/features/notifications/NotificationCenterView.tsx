import { useEffect, useState } from 'react';
import api from '../../lib/api';

type NotificationType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ALERT';

interface NotificationDto {
  id: number;
  recipientId: number | null;
  recipientEmail: string;
  title: string;
  message: string;
  type: NotificationType;
  category: string;
  read: boolean;
  actionUrl: string | null;
  createdAt: string;
}

const TYPE_STYLES: Record<NotificationType, { badge: string; icon: string }> = {
  INFO: { badge: 'bg-sky-100 text-sky-800 border-sky-300', icon: 'ℹ️' },
  SUCCESS: { badge: 'bg-emerald-100 text-emerald-800 border-emerald-300', icon: '✅' },
  WARNING: { badge: 'bg-amber-100 text-amber-800 border-amber-300', icon: '⚠️' },
  ALERT: { badge: 'bg-rose-100 text-rose-800 border-rose-300', icon: '🚨' },
};

export default function NotificationCenterView() {
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // New notification broadcast form
  const [showBroadcastModal, setShowBroadcastModal] = useState(false);
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [type, setType] = useState<NotificationType>('INFO');
  const [category, setCategory] = useState('ANNOUNCEMENT');
  const [submitting, setSubmitting] = useState(false);

  const fetchNotifications = async () => {
    setLoading(true);
    setError(null);
    try {
      const [listRes, countRes] = await Promise.all([
        api.get<NotificationDto[]>('/notifications', { params: { unreadOnly } }),
        api.get<{ data?: { unreadCount: number }; unreadCount?: number }>('/notifications/unread-count'),
      ]);
      const listData = Array.isArray(listRes.data)
        ? listRes.data
        : (listRes.data as unknown as { data: NotificationDto[] }).data || [];
      setNotifications(listData);

      const count = countRes.data?.data?.unreadCount ?? countRes.data?.unreadCount ?? 0;
      setUnreadCount(count);
    } catch {
      setError('Failed to fetch notifications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [unreadOnly]);

  const handleMarkAsRead = async (id: number) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      fetchNotifications();
    } catch {
      setError('Failed to mark notification as read.');
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      fetchNotifications();
    } catch {
      setError('Failed to mark all notifications as read.');
    }
  };

  const handleBroadcast = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/notifications', {
        title,
        message,
        type,
        category,
      });
      setTitle('');
      setMessage('');
      setShowBroadcastModal(false);
      fetchNotifications();
    } catch {
      setError('Failed to dispatch broadcast announcement.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="p-8 max-w-5xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold tracking-tight text-slate-900">Notification Center</h1>
            {unreadCount > 0 && (
              <span className="bg-rose-600 text-white text-xs font-bold px-2.5 py-0.5 rounded-full">
                {unreadCount} unread
              </span>
            )}
          </div>
          <p className="text-slate-500 mt-1">
            Real-time event alerts, test execution regressions, and release promotions.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              className="px-3.5 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 shadow-sm transition-colors"
            >
              ✓ Mark All as Read
            </button>
          )}
          <button
            onClick={() => setShowBroadcastModal(true)}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-lg shadow-sm transition-colors"
          >
            📢 Broadcast Alert
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm">
          {error}
        </div>
      )}

      {/* Filter Bar */}
      <div className="flex items-center justify-between border-b border-slate-200 pb-3">
        <div className="flex gap-2">
          <button
            onClick={() => setUnreadOnly(false)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
              !unreadOnly ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            All Alerts ({notifications.length})
          </button>
          <button
            onClick={() => setUnreadOnly(true)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
              unreadOnly ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Unread Only ({unreadCount})
          </button>
        </div>
        <button
          onClick={fetchNotifications}
          className="text-xs text-slate-500 hover:text-slate-700 font-medium"
        >
          ↻ Refresh
        </button>
      </div>

      {/* Notification Stream */}
      {loading && notifications.length === 0 ? (
        <div className="text-center py-20 text-slate-400 animate-pulse">
          <div className="text-4xl mb-3">🔔</div>
          <p>Loading notification feed…</p>
        </div>
      ) : notifications.length === 0 ? (
        <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl shadow-sm text-slate-400">
          <div className="text-4xl mb-3">📭</div>
          <p className="text-base font-semibold text-slate-700">Inbox Zero!</p>
          <p className="text-xs text-slate-400 mt-1">No pending notifications at this time.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map(notif => {
            const style = TYPE_STYLES[notif.type] || TYPE_STYLES.INFO;
            return (
              <div
                key={notif.id}
                className={`p-5 rounded-2xl border transition-all flex items-start gap-4 ${
                  notif.read
                    ? 'bg-white/80 border-slate-200/80 text-slate-700'
                    : 'bg-white border-indigo-200 shadow-sm ring-1 ring-indigo-50'
                }`}
              >
                <div className="text-2xl mt-0.5">{style.icon}</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap mb-1">
                    <span className="font-bold text-slate-900 text-sm">{notif.title}</span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${style.badge}`}
                    >
                      {notif.type}
                    </span>
                    <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-100 text-slate-600">
                      {notif.category}
                    </span>
                    <span className="text-xs text-slate-400 ml-auto whitespace-nowrap">
                      {notif.createdAt ? new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </span>
                  </div>

                  <p className="text-xs text-slate-600 leading-relaxed mb-2">{notif.message}</p>

                  <div className="flex items-center gap-3">
                    {notif.actionUrl && (
                      <a
                        href={notif.actionUrl}
                        className="text-xs text-indigo-600 hover:text-indigo-800 font-semibold"
                      >
                        View Details →
                      </a>
                    )}
                    {!notif.read && (
                      <button
                        onClick={() => handleMarkAsRead(notif.id)}
                        className="text-xs text-slate-400 hover:text-slate-700 font-medium"
                      >
                        Dismiss
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Broadcast Modal */}
      {showBroadcastModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-xl border border-slate-200 space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">Broadcast Notification</h2>
              <button
                onClick={() => setShowBroadcastModal(false)}
                className="text-slate-400 hover:text-slate-600 text-sm font-semibold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleBroadcast} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Scheduled Gateway Maintenance"
                  value={title}
                  onChange={e => setTitle(e.target.value)}
                  className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Alert Level *</label>
                  <select
                    value={type}
                    onChange={e => setType(e.target.value as NotificationType)}
                    className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                  >
                    <option value="INFO">INFO</option>
                    <option value="SUCCESS">SUCCESS</option>
                    <option value="WARNING">WARNING</option>
                    <option value="ALERT">ALERT</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Category *</label>
                  <select
                    value={category}
                    onChange={e => setCategory(e.target.value)}
                    className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                  >
                    <option value="ANNOUNCEMENT">ANNOUNCEMENT</option>
                    <option value="RELEASE">RELEASE</option>
                    <option value="LIFECYCLE">LIFECYCLE</option>
                    <option value="SECURITY">SECURITY</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Message Content *</label>
                <textarea
                  rows={3}
                  required
                  placeholder="Provide complete description of the event or notification..."
                  value={message}
                  onChange={e => setMessage(e.target.value)}
                  className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowBroadcastModal(false)}
                  className="px-4 py-2 text-sm text-slate-600 hover:text-slate-800 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg disabled:opacity-50 shadow-sm"
                >
                  {submitting ? 'Broadcasting…' : 'Dispatch Notification'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
