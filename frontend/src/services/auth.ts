import api from './api';
import { LoginRequest, LoginResponse, RegisterRequest, ApiResponse } from '../types';

export const authService = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        // The backend expects 'password' not 'passwordHash' in the request body usually
        // But types.ts defined it as passwordHash to match entity.
        // Let's assume the API expects { username, password }
        const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', {
            username: credentials.username,
            password: credentials.passwordHash,
        });
        return response.data.data;
    },

    register: async (userData: RegisterRequest): Promise<User> => {
        const response = await api.post<ApiResponse<User>>('/auth/register', {
            ...userData,
            password: userData.passwordHash, // Map passwordHash to password if needed
        });
        return response.data.data;
    },

    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    getCurrentUser: () => {
        const userStr = localStorage.getItem('user');
        if (userStr) return JSON.parse(userStr);
        return null;
    },
};
