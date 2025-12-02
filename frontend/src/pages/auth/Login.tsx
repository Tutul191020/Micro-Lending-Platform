import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/auth';
import { Eye, EyeOff } from 'lucide-react';
import { jwtDecode } from 'jwt-decode';
import { JwtPayload } from '../../types';

export default function Login() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await authService.login({
                username: formData.username,
                passwordHash: formData.password, // Mapping to match type definition
            });

            // Decode token to get user info
            const decodedUser = jwtDecode<JwtPayload>(response.accessToken);

            // Store token and user info
            localStorage.setItem('token', response.accessToken);
            localStorage.setItem('user', JSON.stringify(decodedUser));

            // Redirect based on role
            if (decodedUser.roles.includes('ROLE_ADMIN') || decodedUser.roles.includes('ROLE_SUPER_USER')) {
                navigate('/admin');
            } else {
                navigate('/dashboard');
            }
        } catch (err: any) {
            console.error(err);
            // Clear any invalid tokens that might cause issues
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setError(err.response?.data?.message || 'Failed to login. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-auth-teal p-4">
            <div className="w-full max-w-md space-y-8">
                <div className="text-center space-y-2">
                    <h1 className="text-4xl font-bold text-gray-800">Log in</h1>
                    <p className="text-gray-600">Hello, Welcome back</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="space-y-4">
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="text"
                                placeholder="Email"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.username}
                                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm relative">
                            <input
                                type={showPassword ? "text" : "password"}
                                placeholder="Password"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400 pr-10"
                                value={formData.password}
                                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                required
                            />
                            <button
                                type="button"
                                onClick={() => setShowPassword(!showPassword)}
                                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none"
                            >
                                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                            </button>
                        </div>
                    </div>

                    {error && (
                        <div className="p-3 rounded-lg bg-red-50 text-red-500 text-sm text-center">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-auth-teal-btn hover:bg-opacity-90 text-white font-medium rounded-full py-3 transition-colors shadow-sm disabled:opacity-70"
                    >
                        {loading ? 'Logging in...' : 'login'}
                    </button>

                    <div className="text-center text-sm">
                        <Link to="/forgot-password" className="text-gray-600 hover:text-gray-800 hover:underline">
                            Forgot Password?
                        </Link>
                    </div>

                    <div className="text-center text-sm text-gray-600">
                        Don't have an account?{' '}
                        <Link to="/register" className="text-gray-800 font-semibold hover:underline">
                            Sign up
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
