import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/auth';
import { Eye, EyeOff, ArrowLeft, CheckCircle } from 'lucide-react';

export default function ForgotPassword() {
    const navigate = useNavigate();
    const [step, setStep] = useState<'identifier' | 'otp' | 'password' | 'success'>('identifier');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [formData, setFormData] = useState({
        identifier: '',
        otpCode: '',
        newPassword: '',
        confirmPassword: '',
    });

    const [otpInfo, setOtpInfo] = useState({
        expiresIn: 0,
        otpSentTo: '',
    });

    const handleSendOtp = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await authService.forgotPassword(formData.identifier);
            setOtpInfo({
                expiresIn: response.expiresIn,
                otpSentTo: response.otpSentTo,
            });
            setStep('otp');
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Failed to send OTP. Please check your email/phone.');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyOtp = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (formData.otpCode.length !== 4) {
            setError('OTP must be 4 digits');
            return;
        }

        setStep('password');
    };

    const handleResetPassword = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (formData.newPassword !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (formData.newPassword.length < 8) {
            setError('Password must be at least 8 characters');
            return;
        }

        setLoading(true);

        try {
            await authService.resetPassword({
                identifier: formData.identifier,
                otpCode: formData.otpCode,
                newPassword: formData.newPassword,
                confirmPassword: formData.confirmPassword,
            });
            setStep('success');
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Failed to reset password. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const renderStepContent = () => {
        switch (step) {
            case 'identifier':
                return (
                    <form onSubmit={handleSendOtp} className="space-y-6">
                        <div className="space-y-4">
                            <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                                <input
                                    type="text"
                                    placeholder="Email or Phone Number"
                                    className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400"
                                    value={formData.identifier}
                                    onChange={(e) => setFormData({ ...formData, identifier: e.target.value })}
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
                            className="w-full bg-auth-teal-btn hover:bg-opacity-90 text-white font-medium rounded-full py-3 transition-colors shadow-sm disabled:opacity-70"
                        >
                            {loading ? 'Sending OTP...' : 'Send OTP'}
                        </button>
                    </form>
                );

            case 'otp':
                return (
                    <form onSubmit={handleVerifyOtp} className="space-y-6">
                        <div className="text-center space-y-2 mb-6">
                            <p className="text-sm text-gray-600">
                                We've sent a 4-digit code to
                            </p>
                            <p className="font-semibold text-gray-800">{otpInfo.otpSentTo}</p>
                            <p className="text-xs text-gray-500">
                                Code expires in {Math.floor(otpInfo.expiresIn / 60)} minutes
                            </p>
                        </div>

                        <div className="space-y-4">
                            <div className="bg-white rounded-full px-6 py-3 shadow-sm">
                                <input
                                    type="text"
                                    placeholder="Enter 4-digit OTP"
                                    maxLength={4}
                                    pattern="[0-9]{4}"
                                    className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400 text-center text-2xl tracking-widest"
                                    value={formData.otpCode}
                                    onChange={(e) => {
                                        const value = e.target.value.replace(/\D/g, '');
                                        setFormData({ ...formData, otpCode: value });
                                    }}
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
                            className="w-full bg-auth-teal-btn hover:bg-opacity-90 text-white font-medium rounded-full py-3 transition-colors shadow-sm"
                        >
                            Continue
                        </button>

                        <button
                            type="button"
                            onClick={() => setStep('identifier')}
                            className="w-full text-gray-600 hover:text-gray-800 font-medium py-2 transition-colors flex items-center justify-center gap-2"
                        >
                            <ArrowLeft size={16} />
                            Back
                        </button>
                    </form>
                );

            case 'password':
                return (
                    <form onSubmit={handleResetPassword} className="space-y-6">
                        <div className="space-y-4">
                            <div className="bg-white rounded-full px-6 py-3 shadow-sm relative">
                                <input
                                    type={showPassword ? "text" : "password"}
                                    placeholder="New Password"
                                    className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400 pr-10"
                                    value={formData.newPassword}
                                    onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
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
                            <div className="bg-white rounded-full px-6 py-3 shadow-sm relative">
                                <input
                                    type={showConfirmPassword ? "text" : "password"}
                                    placeholder="Confirm Password"
                                    className="w-full bg-transparent outline-none text-gray-700 placeholder-gray-400 pr-10"
                                    value={formData.confirmPassword}
                                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none"
                                >
                                    {showConfirmPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                </button>
                            </div>
                        </div>

                        <div className="text-xs text-gray-500 space-y-1">
                            <p>Password must contain:</p>
                            <ul className="list-disc list-inside space-y-0.5 ml-2">
                                <li>At least 8 characters</li>
                                <li>One uppercase letter</li>
                                <li>One lowercase letter</li>
                                <li>One number</li>
                                <li>One special character (@#$%^&+=)</li>
                            </ul>
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
                            {loading ? 'Resetting Password...' : 'Reset Password'}
                        </button>

                        <button
                            type="button"
                            onClick={() => setStep('otp')}
                            className="w-full text-gray-600 hover:text-gray-800 font-medium py-2 transition-colors flex items-center justify-center gap-2"
                        >
                            <ArrowLeft size={16} />
                            Back
                        </button>
                    </form>
                );

            case 'success':
                return (
                    <div className="space-y-6 text-center">
                        <div className="flex justify-center">
                            <CheckCircle size={64} className="text-green-500" />
                        </div>
                        <div className="space-y-2">
                            <h2 className="text-2xl font-bold text-gray-800">Password Reset Successful!</h2>
                            <p className="text-gray-600">
                                Your password has been reset successfully.
                            </p>
                        </div>
                        <button
                            onClick={() => navigate('/login')}
                            className="w-full bg-auth-teal-btn hover:bg-opacity-90 text-white font-medium rounded-full py-3 transition-colors shadow-sm"
                        >
                            Go to Login
                        </button>
                    </div>
                );
        }
    };

    const getStepTitle = () => {
        switch (step) {
            case 'identifier':
                return 'Forgot Password';
            case 'otp':
                return 'Enter OTP';
            case 'password':
                return 'Reset Password';
            case 'success':
                return '';
            default:
                return 'Forgot Password';
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-auth-teal p-4">
            <div className="w-full max-w-md space-y-8">
                {step !== 'success' && (
                    <div className="text-center space-y-2">
                        <h1 className="text-4xl font-bold text-gray-800">{getStepTitle()}</h1>
                        <p className="text-gray-600">
                            {step === 'identifier' && "Enter your email or phone number to receive an OTP"}
                            {step === 'otp' && "Check your email/phone for the code"}
                            {step === 'password' && "Create a new strong password"}
                        </p>
                    </div>
                )}

                {renderStepContent()}

                {step === 'identifier' && (
                    <div className="text-center text-sm text-gray-600">
                        Remember your password?{' '}
                        <Link to="/login" className="text-gray-800 font-semibold hover:underline">
                            Log in
                        </Link>
                    </div>
                )}
            </div>
        </div>
    );
}
