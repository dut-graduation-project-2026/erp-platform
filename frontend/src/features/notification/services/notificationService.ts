import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';
import { Notification } from '../types';

export const getMyNotificationsApi = async (): Promise<Notification[]> => {
  const response = await apiClient.get<Notification[]>(API_ENDPOINTS.NOTIFICATIONS.BASE);
  return response.data;
};

export const deleteNotificationApi = async (id: string): Promise<void> => {
  await apiClient.delete(API_ENDPOINTS.NOTIFICATIONS.DELETE(id));
};
