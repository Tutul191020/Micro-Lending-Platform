import api from './api';
import { ApiResponse } from '../types';

export interface AuthorityResponse {
    id: number;
    name: string;
    description: string;
    resource: string;
    action: string;
    createdAt: string;
}

export interface UserAuthorityAssignmentRequest {
    userId: number;
    authorityNames: string[];
}

export const authorityService = {
    // Get all system authorities
    getAllAuthorities: async (): Promise<AuthorityResponse[]> => {
        const response = await api.get<ApiResponse<AuthorityResponse[]>>('/authorities');
        return response.data.data;
    },

    // Get authorities for a specific user
    getUserAuthorities: async (userId: number): Promise<AuthorityResponse[]> => {
        const response = await api.get<ApiResponse<AuthorityResponse[]>>(`/authorities/user/${userId}`);
        return response.data.data;
    },

    // Assign authorities to a user
    assignAuthoritiesToUser: async (userId: number, authorityNames: string[]): Promise<void> => {
        await api.put('/users/authorities/assign', {
            userId,
            authorityNames
        });
    },

    // Get authorities by resource
    getAuthoritiesByResource: async (resource: string): Promise<AuthorityResponse[]> => {
        const response = await api.get<ApiResponse<AuthorityResponse[]>>(`/authorities/resource/${resource}`);
        return response.data.data;
    }
};
