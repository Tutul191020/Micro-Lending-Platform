import api from './api';
import { ApiResponse, PageResponse } from '../types';

export interface FundRequestResponse {
    id: string;
    amount: number;
    reason: string;
    status: string;
    requestedById: number;
    requestedByFullName: string;
    reviewedById?: number;
    reviewedByUsername?: string;
    reviewedAt?: string;
    reviewComments?: string;
    createdAt: string;
}

export interface FundRequestReviewRequest {
    comments: string;
}

export const fundService = {
    getPendingRequests: async (page: number = 0, size: number = 10): Promise<PageResponse<FundRequestResponse>> => {
        const response = await api.get<ApiResponse<PageResponse<FundRequestResponse>>>(`/fund-requests?status=PENDING&page=${page}&size=${size}`);
        return response.data.data;
    },

    approveFundRequest: async (requestId: string, comments?: string): Promise<FundRequestResponse> => {
        const response = await api.post<ApiResponse<FundRequestResponse>>(`/fund-requests/${requestId}/approve`, { comments });
        return response.data.data;
    },

    rejectFundRequest: async (requestId: string, comments?: string): Promise<FundRequestResponse> => {
        const response = await api.post<ApiResponse<FundRequestResponse>>(`/fund-requests/${requestId}/reject`, { comments });
        return response.data.data;
    },
};
