import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';


export default function ChangePassword() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (formData.newPassword !== formData.confirmPassword) {
            setError('New passwords do not match');
            return;
        }

        setLoading(true);

        try {
            // Assuming authService has a changePassword method or similar. 
            // If not, I might need to implement it or this is just a UI mockup for now.
            // For now, I'll simulate a success or call a placeholder.
            // await authService.changePassword(formData);

            // Simulating API call
            await new Promise(resolve => setTimeout(resolve, 1000));

            setSuccess('Password changed successfully');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Failed to change password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-auth-purple p-4">
            <div className="w-full max-w-md space-y-8">
                <div className="text-center space-y-2">
                    <h1 className="text-4xl font-bold text-white">Change Password</h1>
                    <p className="text-indigo-100">Create a new secure password</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="space-y-4">
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="password"
                                name="currentPassword"
                                placeholder="Current Password"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.currentPassword}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="password"
                                name="newPassword"
                                placeholder="New Password"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.newPassword}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                            <input
                                type="password"
                                name="confirmPassword"
                                placeholder="Confirm New Password"
                                className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                value={formData.confirmPassword}
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

                    {success && (
                        <div className="p-3 rounded-lg bg-green-50 text-green-600 text-sm text-center">
                            {success}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-auth-teal-btn hover:bg-opacity-90 text-white font-medium rounded-full py-3 transition-colors shadow-sm disabled:opacity-70"
                    >
                        {loading ? 'Updating...' : 'Update Password'}
                    </button>

                    <div className="text-center text-sm text-indigo-100">
                        Remember your password?{' '}
                        <Link to="/login" className="text-white font-semibold hover:underline">
                            Log in
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
