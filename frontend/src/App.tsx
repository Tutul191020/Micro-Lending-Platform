import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ChangePassword from './pages/auth/ChangePassword';
import ForgotPassword from './pages/auth/ForgotPassword';
import BorrowerDashboard from './pages/borrower/Dashboard';
import PayScreen from './pages/borrower/PayScreen';
import SuperAdminDashboard from './pages/dashboard/SuperAdminDashboard';
import AccountantDashboard from './pages/accountant/Dashboard';
import TestJwt from './pages/TestJwt';
import TransactionsPage from './pages/admin/Transactions';
import UserManagementPage from './pages/admin/UserManagement';
import TreasuryPage from './pages/admin/Treasury';
import AuthorityManagement from './pages/admin/AuthorityManagement';

// Simple Protected Route component
const ProtectedRoute = ({ children, allowedRoles, redirectPath }: { children: React.ReactNode, allowedRoles?: string[], redirectPath?: string }) => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (!token || !userStr) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles) {
        try {
            const user = JSON.parse(userStr);
            // Check if user has any of the allowed roles
            // The roles array contains both authorities and role names
            const hasRole = Array.isArray(user.roles) &&
                user.roles.some((role: string) => allowedRoles.includes(role));

            if (!hasRole) {
                // Redirect to appropriate dashboard based on user's role
                if (user.roles.includes('ROLE_SUPER_USER') || user.roles.includes('ROLE_ADMIN')) {
                    return <Navigate to="/admin" replace />;
                } else if (user.roles.includes('ROLE_ACCOUNTANT')) {
                    return <Navigate to="/accountant" replace />;
                }
                return <Navigate to={redirectPath || "/dashboard"} replace />;
            }
        } catch (error) {
            console.error('Error parsing user data:', error);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            return <Navigate to="/login" replace />;
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
                <Route path="/test-jwt" element={<TestJwt />} />

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

                {/* Accountant Routes */}
                <Route
                    path="/accountant"
                    element={
                        <ProtectedRoute allowedRoles={['ROLE_ACCOUNTANT']}>
                            <AccountantDashboard />
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
                <Route
                    path="/admin/transactions"
                    element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_USER']}>
                            <TransactionsPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/users"
                    element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_USER']}>
                            <UserManagementPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/treasury"
                    element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_USER']}>
                            <TreasuryPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/authorities"
                    element={
                        <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_USER']}>
                            <AuthorityManagement />
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </Router>
    );
}

export default App;
