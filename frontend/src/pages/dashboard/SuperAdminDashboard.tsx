import { useEffect, useState } from 'react';
import {
    Wallet,
    CheckCircle,
    XCircle,
    Clock,
    User,
    DollarSign,
} from 'lucide-react';
import { fundService, FundRequestResponse } from '../../services/fund';
import { treasuryService, FundPoolResponse } from '../../services/treasury';
import DashboardLayout from '../../components/layout/DashboardLayout';

export default function SuperAdminDashboard() {
    const [loading, setLoading] = useState(true);
    const [balance, setBalance] = useState<FundPoolResponse | null>(null);
    const [pendingRequests, setPendingRequests] = useState<FundRequestResponse[]>([]);
    const [actionLoading, setActionLoading] = useState<string | null>(null);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [balanceData, requestsData] = await Promise.all([
                treasuryService.getBalance(),
                fundService.getPendingRequests(0, 10)
            ]);
            setBalance(balanceData);
            setPendingRequests(requestsData.content);
        } catch (error) {
            console.error('Error fetching dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (requestId: string) => {
        if (!confirm('Are you sure you want to approve this fund request?')) return;

        setActionLoading(requestId);
        try {
            await fundService.approveFundRequest(requestId, 'Approved by SuperAdmin');
            await fetchData(); // Refresh data
            alert('Fund request approved successfully!');
        } catch (error: any) {
            console.error('Error approving request:', error);
            alert(error.response?.data?.message || 'Failed to approve request');
        } finally {
            setActionLoading(null);
        }
    };

    const handleReject = async (requestId: string) => {
        const reason = prompt('Please provide a reason for rejection:');
        if (!reason) return;

        setActionLoading(requestId);
        try {
            await fundService.rejectFundRequest(requestId, reason);
            await fetchData(); // Refresh data
            alert('Fund request rejected successfully!');
        } catch (error: any) {
            console.error('Error rejecting request:', error);
            alert(error.response?.data?.message || 'Failed to reject request');
        } finally {
            setActionLoading(null);
        }
    };

    if (loading) {
        return (
            <DashboardLayout>
                <div className="flex items-center justify-center h-screen">
                    <div className="text-gray-500">Loading...</div>
                </div>
            </DashboardLayout>
        );
    }

    return (
        <DashboardLayout>
            <div className="p-8">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">SuperAdmin Dashboard</h1>
                    <p className="text-gray-600">Manage fund requests and treasury operations</p>
                </div>

                {/* Stats Cards */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    {/* Treasury Balance */}
                    <div className="bg-gradient-to-br from-teal-500 to-teal-700 rounded-2xl shadow-lg p-6 text-white">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                                <Wallet size={24} />
                            </div>
                            <div>
                                <p className="text-teal-100 text-sm font-medium">Treasury Balance</p>
                                <h2 className="text-3xl font-bold">৳{balance?.balance?.toLocaleString() || '0'}</h2>
                            </div>
                        </div>
                    </div>

                    {/* Pending Requests */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-orange-50 rounded-xl">
                                <Clock className="text-orange-600" size={24} />
                            </div>
                            <div>
                                <p className="text-gray-500 text-sm font-medium">Pending Requests</p>
                                <h2 className="text-3xl font-bold text-gray-900">{pendingRequests.length}</h2>
                            </div>
                        </div>
                    </div>

                    {/* Total Requested */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-blue-50 rounded-xl">
                                <DollarSign className="text-blue-600" size={24} />
                            </div>
                            <div>
                                <p className="text-gray-500 text-sm font-medium">Total Requested</p>
                                <h2 className="text-3xl font-bold text-gray-900">
                                    ৳{pendingRequests.reduce((sum, req) => sum + Number(req.amount), 0).toLocaleString()}
                                </h2>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Pending Fund Requests Table */}
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                    <div className="p-6 border-b border-gray-100">
                        <h3 className="text-xl font-bold text-gray-900">Pending Fund Requests</h3>
                        <p className="text-gray-500 text-sm mt-1">Review and approve fund requests from accountants</p>
                    </div>

                    {pendingRequests.length === 0 ? (
                        <div className="p-12 text-center">
                            <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                                <CheckCircle className="text-gray-400" size={32} />
                            </div>
                            <h4 className="text-lg font-semibold text-gray-900 mb-2">No Pending Requests</h4>
                            <p className="text-gray-500">All fund requests have been processed</p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead className="bg-gray-50 border-b border-gray-100">
                                    <tr>
                                        <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                            Accountant
                                        </th>
                                        <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                            Amount
                                        </th>
                                        <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                            Reason
                                        </th>
                                        <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                            Requested On
                                        </th>
                                        <th className="px-6 py-4 text-right text-xs font-bold text-gray-600 uppercase tracking-wider">
                                            Actions
                                        </th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-100">
                                    {pendingRequests.map((request) => (
                                        <tr key={request.id} className="hover:bg-gray-50 transition-colors">
                                            <td className="px-6 py-4">
                                                <div className="flex items-center gap-3">
                                                    <div className="w-10 h-10 bg-teal-100 rounded-full flex items-center justify-center">
                                                        <User className="text-teal-600" size={20} />
                                                    </div>
                                                    <div>
                                                        <p className="font-semibold text-gray-900">{request.requestedByFullName}</p>
                                                        <p className="text-sm text-gray-500">ID: {request.requestedById}</p>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-lg font-bold text-gray-900">৳{Number(request.amount).toLocaleString()}</span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <p className="text-gray-700 max-w-xs truncate">{request.reason}</p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <p className="text-gray-600">
                                                    {new Date(request.createdAt).toLocaleDateString('en-US', {
                                                        year: 'numeric',
                                                        month: 'short',
                                                        day: 'numeric'
                                                    })}
                                                </p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center justify-end gap-2">
                                                    <button
                                                        onClick={() => handleApprove(request.id)}
                                                        disabled={actionLoading === request.id}
                                                        className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 flex items-center gap-2 font-medium"
                                                    >
                                                        <CheckCircle size={16} />
                                                        {actionLoading === request.id ? 'Processing...' : 'Approve'}
                                                    </button>
                                                    <button
                                                        onClick={() => handleReject(request.id)}
                                                        disabled={actionLoading === request.id}
                                                        className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 flex items-center gap-2 font-medium"
                                                    >
                                                        <XCircle size={16} />
                                                        Reject
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </DashboardLayout>
    );
}
