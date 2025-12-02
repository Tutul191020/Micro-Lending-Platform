import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ChangePassword from './pages/auth/ChangePassword';
import ForgotPassword from './pages/auth/ForgotPassword';
import BorrowerDashboard from './pages/borrower/Dashboard';
import PayScreen from './pages/borrower/PayScreen';
import SuperAdminDashboard from './pages/dashboard/SuperAdminDashboard';

// Simple Protected Route component
const ProtectedRoute = ({ children, allowedRoles }: { children: React.ReactNode, allowedRoles?: string[] }) => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (!token || !userStr) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles) {
        const user = JSON.parse(userStr);
        // Check if user has any of the allowed roles
        const hasRole = user.roles?.some((role: string) => allowedRoles.includes(role));

        if (!hasRole) {
            return <Navigate to="/dashboard" replace />;
        }
    }

    return <>{children}</>;
};

function App() {
    return (
        <Router>
            <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/change-password" element={<ChangePassword />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />

                {/* Redirect root to login */}
                <Route path="/" element={<Navigate to="/login" replace />} />

                {/* Borrower Routes */}
                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <BorrowerDashboard />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/pay"
                    element={
                        <ProtectedRoute>
                            <PayScreen />
                        </ProtectedRoute>
                    }
                />

                {/* Admin Routes */}
                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_USER']}>
                            <SuperAdminDashboard />
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </Router>
    );
}

export default App;
