import api from './api';
import {
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    ApiResponse,
    ResetPasswordRequest,
    OtpResponse,
    User
} from '../types';

export const authService = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', {
            username: credentials.username,
            password: credentials.passwordHash,
        });
        return response.data.data;
    },

    register: async (userData: RegisterRequest): Promise<User> => {
        const response = await api.post<ApiResponse<User>>('/auth/register', {
            ...userData,
            password: userData.passwordHash,
        });
        return response.data.data;
    },

    forgotPassword: async (identifier: string): Promise<OtpResponse> => {
        const response = await api.post<ApiResponse<OtpResponse>>('/auth/forgot-password', {
            identifier
        });
        return response.data.data;
    },

    verifyOtp: async (identifier: string, otpCode: string): Promise<void> => {
        await api.post<ApiResponse<void>>('/auth/verify-otp', {
            identifier,
            otpCode
        });
    },

    resetPassword: async (data: ResetPasswordRequest): Promise<void> => {
        await api.post<ApiResponse<void>>('/auth/reset-password', data);
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
