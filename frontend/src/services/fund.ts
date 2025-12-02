import api from './api';
import { ApiResponse, PageResponse } from '../types';

export interface FundRequestResponse {
    id: string;
    amount: number;
    reason: string;
    status: string;
    requestedBy: {
        id: number;
        username: string;
        fullName: string;
    };
    reviewedBy?: {
        id: number;
        username: string;
        fullName: string;
    };
    reviewedAt?: string;
    reviewComments?: string;
    createdAt: string;
}

export interface FundRequestReviewRequest {
    comments: string;
}

export const fundService = {
    getPendingRequests: async (page = 0, size = 10): Promise<PageResponse<FundRequestResponse>> => {
        const response = await api.get<ApiResponse<PageResponse<FundRequestResponse>>>(
            `/fund-requests/pending?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    approveRequest: async (id: string, comments?: string): Promise<FundRequestResponse> => {
        const response = await api.post<ApiResponse<FundRequestResponse>>(
            `/fund-requests/${id}/approve`,
            { comments }
        );
        return response.data.data;
    },

    rejectRequest: async (id: string, comments?: string): Promise<FundRequestResponse> => {
        const response = await api.post<ApiResponse<FundRequestResponse>>(
            `/fund-requests/${id}/reject`,
            { comments }
        );
        return response.data.data;
    },
};
