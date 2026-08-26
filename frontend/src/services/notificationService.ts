import { request } from './apiClient';
import { PageResponse } from './discoveryService';

export interface NotificationResponse {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  status: string;
  payloadJson?: string;
  createdAt: string;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export interface MarkedReadResponse {
  markedReadCount: number;
}

export const notificationService = {
  async getMyNotifications(status?: string, page = 0, size = 20): Promise<PageResponse<NotificationResponse>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (status) params.append('status', status);
    return request<PageResponse<NotificationResponse>>(`/api/notifications?${params.toString()}`);
  },

  async getUnreadCount(): Promise<UnreadCountResponse> {
    return request<UnreadCountResponse>('/api/notifications/unread-count');
  },

  async markAsRead(notificationId: string): Promise<NotificationResponse> {
    return request<NotificationResponse>(`/api/notifications/${notificationId}/read`, {
      method: 'POST',
    });
  },

  async markAllAsRead(): Promise<MarkedReadResponse> {
    return request<MarkedReadResponse>('/api/notifications/read-all', {
      method: 'POST',
    });
  },
};
