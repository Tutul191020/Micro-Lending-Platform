import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/auth';

export default function Register() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        fullName: '',
        email: '',
        phone: '',
        nationalId: '',
        dateOfBirth: '',
        address: '',
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await authService.register({
                ...formData,
                passwordHash: formData.password,
            });
            // Redirect to login on success
            navigate('/login');
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Failed to register. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-auth-cream p-4">
            <div className="w-full max-w-lg space-y-8">
                <div className="text-center space-y-2">
                    <h1 className="text-4xl font-bold text-gray-800">Sign up</h1>
                    <p className="text-gray-600">Just a few quick things to get started</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="space-y-4">
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="text"
                                name="fullName"
                                placeholder="Full Name"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.fullName}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="text"
                                name="username"
                                placeholder="Username"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.username}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="email"
                                name="email"
                                placeholder="Email"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="password"
                                name="password"
                                placeholder="Password"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                                <input
                                    type="text"
                                    name="phone"
                                    placeholder="Phone"
                                    className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                                <input
                                    type="text"
                                    name="nationalId"
                                    placeholder="National ID"
                                    className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                    value={formData.nationalId}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="date"
                                name="dateOfBirth"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.dateOfBirth}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="text"
                                name="address"
                                placeholder="Address"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.address}
                                onChange={handleChange}
                                required
                            />
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
                        className="w-full bg-auth-teal hover:bg-opacity-90 text-white font-medium rounded-full py-3 transition-colors shadow-sm disabled:opacity-70"
                    >
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>

                    <div className="text-center text-sm text-gray-600">
                        Already have an account?{' '}
                        <Link to="/login" className="text-auth-purple font-semibold hover:underline">
                            Log in
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
