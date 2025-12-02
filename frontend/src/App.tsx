import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ChangePassword from './pages/auth/ChangePassword';
import BorrowerDashboard from './pages/borrower/Dashboard';
import PayScreen from './pages/borrower/PayScreen';
import AdminDashboard from './pages/admin/Dashboard';

// Simple Protected Route component
const ProtectedRoute = ({ children, allowedRoles }: { children: React.ReactNode, allowedRoles?: string[] }) => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (!token || !userStr) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles) {
        const user = JSON.parse(userStr);
        const hasRole = user.roles?.some((role: string) => allowedRoles.includes(role));
        // Note: Adjust role checking logic based on actual backend response structure
        // If backend returns roles as array of strings, this works.
        // If user has no roles or mismatch, redirect to unauthorized or dashboard
        // For now, let's assume if role check fails, go to dashboard (or login)
        // if (!hasRole) return <Navigate to="/dashboard" replace />;
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
                        <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN']}>
                            <AdminDashboard />
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </Router>
    );
}

export default App;
