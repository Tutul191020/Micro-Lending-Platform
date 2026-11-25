import api from './api';
import { ApiResponse } from '../types';

export interface UserResponse {
    id: number;
    username: string;
    fullName: string;
    email: string;
    phone: string;
    enabled: boolean;
    verified: boolean;
    verificationStatus: string;
    onboardingStatus: string;
    creditScore: number;
    createdAt: string;
}

export interface UserSearchCriteria {
    role?: string;
    verificationStatus?: string;
    onboardingStatus?: string;
    username?: string;
    email?: string;
}

export interface UserListResponse {
    users: UserResponse[];
    totalCount: number;
}

export const userService = {
    // Search users with criteria
    searchUsers: async (criteria: UserSearchCriteria): Promise<UserListResponse> => {
        const params = new URLSearchParams();
        if (criteria.role) params.append('role', criteria.role);
        if (criteria.verificationStatus) params.append('verificationStatus', criteria.verificationStatus);
        if (criteria.onboardingStatus) params.append('onboardingStatus', criteria.onboardingStatus);
        if (criteria.username) params.append('username', criteria.username);
        if (criteria.email) params.append('email', criteria.email);

        const response = await api.post<ApiResponse<UserListResponse>>('/users/search', criteria);
        return response.data.data;
    },

    // Get all users (for dropdown)
    getAllUsers: async (): Promise<UserResponse[]> => {
        const response = await api.post<ApiResponse<UserListResponse>>('/users/search', {});
        return response.data.data.users;
    },

    // Get user by ID
    getUserById: async (userId: number): Promise<UserResponse> => {
        const response = await api.get<ApiResponse<UserResponse>>(`/users/${userId}`);
        return response.data.data;
    }
};
