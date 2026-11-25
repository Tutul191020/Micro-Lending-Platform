import api from './api';
import { ApiResponse, PageResponse } from '../types';

export interface LoanResponse {
    id: string;
    principal: number;
    interest: number;
    totalAmount: number;
    tenureMonths: number;
    status: string;
    applicantId: number;
    applicantName: string;
    createdAt: string;
    approvedAt?: string;
    approvedBy?: string;
}

export interface LoanDetailsResponse {
    id: string;
    principal: number;
    interest: number;
    totalAmount: number;
    tenureMonths: number;
    status: string;
    applicant: {
        id: number;
        username: string;
        fullName: string;
        email: string;
        phone: string;
    };
    repayments: LoanRepaymentResponse[];
    createdAt: string;
    approvedAt?: string;
    approvedBy?: string;
}

export interface LoanRepaymentResponse {
    id: string;
    amount: number;
    dueDate: string;
    paidDate?: string;
    status: string;
}

export interface LoanApplicationRequest {
    principal: number;
    tenureMonths: number;
    purpose?: string;
}

export interface LoanApprovalRequest {
    loanId: string;
    decision: 'APPROVE' | 'REJECT';
    comments?: string;
}

export const loanService = {
    applyForLoan: async (data: LoanApplicationRequest): Promise<LoanResponse> => {
        const response = await api.post<ApiResponse<LoanResponse>>('/loans/apply', data);
        return response.data.data;
    },

    approveLoan: async (data: LoanApprovalRequest): Promise<LoanResponse> => {
        const response = await api.post<ApiResponse<LoanResponse>>('/loans/approve', data);
        return response.data.data;
    },

    getLoanDetails: async (loanId: string): Promise<LoanDetailsResponse> => {
        const response = await api.get<ApiResponse<LoanDetailsResponse>>(`/loans/${loanId}`);
        return response.data.data;
    },

    getMyLoans: async (): Promise<LoanResponse[]> => {
        const response = await api.get<ApiResponse<LoanResponse[]>>('/loans/my-loans');
        return response.data.data;
    },

    getLoansByStatus: async (status: string, page: number = 0, size: number = 10): Promise<PageResponse<LoanResponse>> => {
        const response = await api.get<ApiResponse<PageResponse<LoanResponse>>>(
            `/loans/status/${status}?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getPendingLoans: async (page: number = 0, size: number = 10): Promise<PageResponse<LoanResponse>> => {
        const response = await api.get<ApiResponse<PageResponse<LoanResponse>>>(
            `/loans/status/PENDING?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getApprovedLoans: async (page: number = 0, size: number = 10): Promise<PageResponse<LoanResponse>> => {
        const response = await api.get<ApiResponse<PageResponse<LoanResponse>>>(
            `/loans/status/APPROVED?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getRejectedLoans: async (page: number = 0, size: number = 10): Promise<PageResponse<LoanResponse>> => {
        const response = await api.get<ApiResponse<PageResponse<LoanResponse>>>(
            `/loans/status/REJECTED?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getOverdueLoans: async (): Promise<LoanResponse[]> => {
        const response = await api.get<ApiResponse<LoanResponse[]>>('/loans/overdue');
        return response.data.data;
    },
};
