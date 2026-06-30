import { create } from 'zustand';
import { env } from '@/config/env';
import { Notification } from '../types';
import { getMyNotificationsApi, deleteNotificationApi } from '../services/notificationService';

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  eventSource: EventSource | null;
  
  fetchNotifications: () => Promise<void>;
  deleteNotification: (id: string) => Promise<void>;
  initializeSSE: () => void;
  cleanupSSE: () => void;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,
  eventSource: null,

  fetchNotifications: async () => {
    set({ isLoading: true });
    try {
      const data = await getMyNotificationsApi();
      // Backend marks all notifications as read when queried, so reset unreadCount to 0
      set({ notifications: data, unreadCount: 0 });
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      set({ isLoading: false });
    }
  },

  deleteNotification: async (id: string) => {
    try {
      await deleteNotificationApi(id);
      set((state) => ({
        notifications: state.notifications.filter((n) => n.id !== id),
      }));
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  },

  initializeSSE: () => {
    // Commented out to disable active SSE notification stream connection
    /*
    const { eventSource } = get();
    if (eventSource) return;

    if (typeof window === 'undefined') return;

    const url = `${env.API_BASE_URL}/notifications/stream`;
    const es = new EventSource(url, { withCredentials: true });

    es.addEventListener('unread-count', (event: MessageEvent) => {
      const count = parseInt(event.data, 10);
      set({ unreadCount: isNaN(count) ? 0 : count });
    });

    es.onerror = (error) => {
      if (es.readyState === EventSource.CLOSED) {
        console.error('SSE connection closed permanently:', error);
        get().cleanupSSE();
      } else if (es.readyState === EventSource.CONNECTING) {
        console.warn('SSE connection lost. Attempting to reconnect...');
      }
    };

    set({ eventSource: es });
    */
  },

  cleanupSSE: () => {
    const { eventSource } = get();
    if (eventSource) {
      eventSource.close();
      set({ eventSource: null });
    }
  },
}));
