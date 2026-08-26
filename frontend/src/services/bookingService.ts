import { request } from './apiClient';

export interface BookingResponse {
  id: string;
  requirementId: string;
  providerType: string;
  providerWorkerProfileId?: string;
  providerTeamId?: string;
  providerContractorProfileId?: string;
  quantity: number;
  status: string;
  requestedAt: string;
  respondedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  providerType: string;
  providerWorkerProfileId?: string;
  providerTeamId?: string;
  providerContractorProfileId?: string;
  quantity: number;
}

export const bookingService = {
  async createBooking(requirementId: string, req: CreateBookingRequest): Promise<BookingResponse> {
    return request<BookingResponse>(`/api/requirements/${requirementId}/bookings`, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getRequirementBookings(requirementId: string): Promise<BookingResponse[]> {
    return request<BookingResponse[]>(`/api/requirements/${requirementId}/bookings`);
  },

  async getBookingById(bookingId: string): Promise<BookingResponse> {
    return request<BookingResponse>(`/api/bookings/${bookingId}`);
  },

  async getProviderBookings(): Promise<BookingResponse[]> {
    return request<BookingResponse[]>('/api/bookings/provider');
  },

  async acceptBooking(bookingId: string): Promise<BookingResponse> {
    return request<BookingResponse>(`/api/bookings/${bookingId}/accept`, {
      method: 'POST',
    });
  },

  async rejectBooking(bookingId: string): Promise<BookingResponse> {
    return request<BookingResponse>(`/api/bookings/${bookingId}/reject`, {
      method: 'POST',
    });
  },
};
