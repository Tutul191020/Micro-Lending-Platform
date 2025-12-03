import axios from 'axios';

const API_URL = '/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            if (!config.headers) {
                config.headers = new axios.AxiosHeaders();
            }
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor to handle errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Don't automatically clear tokens on 401/403
        // Let the components handle authentication errors appropriately
        // if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        //     localStorage.removeItem('token');
        //     localStorage.removeItem('user');
        // }
        return Promise.reject(error);
    }
);

export default api;
