import { useEffect, useState } from 'react';
import {
    CheckCircle,
    XCircle,
    Clock,
    User,
    TrendingUp,
    AlertCircle,
    FileText,
    Plus,
} from 'lucide-react';
import { loanService, LoanResponse } from '../../services/loan';
import { fundService, FundRequestResponse } from '../../services/fund';
import { treasuryService, FundPoolResponse } from '../../services/treasury';
import DashboardLayout from '../../components/layout/DashboardLayout';
import FundRequestModal from '../../components/accountant/FundRequestModal';

export default function AccountantDashboard() {
    const [loading, setLoading] = useState(true);
    const [balance, setBalance] = useState<FundPoolResponse | null>(null);
    const [pendingLoans, setPendingLoans] = useState<LoanResponse[]>([]);
    const [myFundRequests, setMyFundRequests] = useState<FundRequestResponse[]>([]);
    const [actionLoading, setActionLoading] = useState<string | null>(null);
    const [showFundRequestModal, setShowFundRequestModal] = useState(false);
    const [stats, setStats] = useState({
        totalDisbursed: 0,
        activeLoans: 0,
        pendingApprovals: 0,
        overdueLoans: 0,
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [balanceData, loansData, fundRequestsData] = await Promise.all([
                treasuryService.getAccountantBalance(),
                loanService.getPendingLoans(0, 10),
                fundService.getMyRequests(0, 10),
            ]);
            setBalance(balanceData);
            setPendingLoans(loansData.content);
            setMyFundRequests(fundRequestsData.content);
            
            // Calculate stats
            setStats({
                totalDisbursed: balanceData?.totalDisbursed || 0,
                activeLoans: balanceData?.activeLoans || 0,
                pendingApprovals: loansData.content.length,
                overdueLoans: balanceData?.overdueLoans || 0,
            });
        } catch (error) {
            console.error('Error fetching dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (loanId: string) => {
        if (!confirm('Are you sure you want to approve this loan?')) return;

        setActionLoading(loanId);
        try {
            await loanService.approveLoan({ loanId, decision: 'APPROVE' });
            await fetchData();
            alert('Loan approved successfully!');
        } catch (error: any) {
            console.error('Error approving loan:', error);
            alert(error.response?.data?.message || 'Failed to approve loan. Ensure you have sufficient funds.');
        } finally {
            setActionLoading(null);
        }
    };

    const handleReject = async (loanId: string) => {
        const reason = prompt('Please provide a reason for rejection:');
        if (!reason) return;

        setActionLoading(loanId);
        try {
            await loanService.approveLoan({ loanId, decision: 'REJECT', comments: reason });
            await fetchData();
            alert('Loan rejected successfully!');
        } catch (error: any) {
            console.error('Error rejecting loan:', error);
            alert(error.response?.data?.message || 'Failed to reject loan');
        } finally {
            setActionLoading(null);
        }
    };

    const handleFundRequestCreated = async () => {
        setShowFundRequestModal(false);
        await fetchData();
        alert('Fund request submitted successfully!');
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
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2">Accountant Dashboard</h1>
                        <p className="text-gray-600">Manage loan approvals and fund requests</p>
                    </div>
                    <button
                        onClick={() => setShowFundRequestModal(true)}
                        className="px-6 py-3 bg-teal-600 text-white rounded-xl hover:bg-teal-700 transition-colors flex items-center gap-2 font-medium shadow-md shadow-teal-200"
                    >
                        <Plus size={20} />
                        Request Funds
                    </button>
                </div>

                {/* Stats Cards */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    {/* Available Balance */}
                    <div className="bg-gradient-to-br from-teal-500 to-teal-700 rounded-2xl shadow-lg p-6 text-white">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                                <Wallet size={24} />
                            </div>
                            <div>
                                <p className="text-teal-100 text-sm font-medium">Available Balance</p>
                                <h2 className="text-3xl font-bold">৳{balance?.balance?.toLocaleString() || '0'}</h2>
                            </div>
                        </div>
                        <div className="mt-4 pt-4 border-t border-white/20">
                            <p className="text-teal-100 text-xs">Total Disbursed: ৳{stats.totalDisbursed.toLocaleString()}</p>
                        </div>
                    </div>

                    {/* Pending Approvals */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-orange-50 rounded-xl">
                                <Clock className="text-orange-600" size={24} />
                            </div>
                            <div>
                                <p className="text-gray-500 text-sm font-medium">Pending Approvals</p>
                                <h2 className="text-3xl font-bold text-gray-900">{stats.pendingApprovals}</h2>
                            </div>
                        </div>
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <p className="text-gray-500 text-xs">Awaiting your decision</p>
                        </div>
                    </div>

                    {/* Active Loans */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-blue-50 rounded-xl">
                                <TrendingUp className="text-blue-600" size={24} />
                            </div>
                            <div>
                                <p className="text-gray-500 text-sm font-medium">Active Loans</p>
                                <h2 className="text-3xl font-bold text-gray-900">{stats.activeLoans}</h2>
                            </div>
                        </div>
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <p className="text-gray-500 text-xs">Currently disbursed</p>
                        </div>
                    </div>

                    {/* Overdue Loans */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-3 bg-red-50 rounded-xl">
                                <AlertCircle className="text-red-600" size={24} />
                            </div>
                            <div>
                                <p className="text-gray-500 text-sm font-medium">Overdue Loans</p>
                                <h2 className="text-3xl font-bold text-gray-900">{stats.overdueLoans}</h2>
                            </div>
                        </div>
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <p className="text-gray-500 text-xs">Requires attention</p>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    {/* Pending Loan Approvals */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                        <div className="p-6 border-b border-gray-100">
                            <h3 className="text-xl font-bold text-gray-900">Pending Loan Approvals</h3>
                            <p className="text-gray-500 text-sm mt-1">Review and approve loan applications</p>
                        </div>

                        {pendingLoans.length === 0 ? (
                            <div className="p-12 text-center">
                                <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                                    <CheckCircle className="text-gray-400" size={32} />
                                </div>
                                <h4 className="text-lg font-semibold text-gray-900 mb-2">No Pending Loans</h4>
                                <p className="text-gray-500">All loan applications have been processed</p>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead className="bg-gray-50 border-b border-gray-100">
                                        <tr>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Applicant
                                            </th>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Amount
                                            </th>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Tenure
                                            </th>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Applied On
                                            </th>
                                            <th className="px-6 py-4 text-right text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Actions
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {pendingLoans.map((loan) => (
                                            <tr key={loan.id} className="hover:bg-gray-50 transition-colors">
                                                <td className="px-6 py-4">
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                                                            <User className="text-blue-600" size={20} />
                                                        </div>
                                                        <div>
                                                            <p className="font-semibold text-gray-900">{loan.applicantName}</p>
                                                            <p className="text-sm text-gray-500">ID: {loan.applicantId}</p>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <span className="text-lg font-bold text-gray-900">৳{Number(loan.principal).toLocaleString()}</span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <span className="text-gray-700">{loan.tenureMonths} months</span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <p className="text-gray-600">
                                                        {new Date(loan.createdAt).toLocaleDateString('en-US', {
                                                            year: 'numeric',
                                                            month: 'short',
                                                            day: 'numeric'
                                                        })}
                                                    </p>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="flex items-center justify-end gap-2">
                                                        <button
                                                            onClick={() => handleApprove(loan.id)}
                                                            disabled={actionLoading === loan.id}
                                                            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 flex items-center gap-2 font-medium"
                                                        >
                                                            <CheckCircle size={16} />
                                                            {actionLoading === loan.id ? 'Processing...' : 'Approve'}
                                                        </button>
                                                        <button
                                                            onClick={() => handleReject(loan.id)}
                                                            disabled={actionLoading === loan.id}
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

                    {/* My Fund Requests */}
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                        <div className="p-6 border-b border-gray-100">
                            <h3 className="text-xl font-bold text-gray-900">My Fund Requests</h3>
                            <p className="text-gray-500 text-sm mt-1">Track your fund request status</p>
                        </div>

                        {myFundRequests.length === 0 ? (
                            <div className="p-12 text-center">
                                <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                                    <FileText className="text-gray-400" size={32} />
                                </div>
                                <h4 className="text-lg font-semibold text-gray-900 mb-2">No Fund Requests</h4>
                                <p className="text-gray-500">Request funds to disburse loans</p>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead className="bg-gray-50 border-b border-gray-100">
                                        <tr>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Amount
                                            </th>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Reason
                                            </th>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Status
                                            </th>
                                            <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">
                                                Requested On
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {myFundRequests.map((request) => (
                                            <tr key={request.id} className="hover:bg-gray-50 transition-colors">
                                                <td className="px-6 py-4">
                                                    <span className="text-lg font-bold text-gray-900">৳{Number(request.amount).toLocaleString()}</span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <p className="text-gray-700 max-w-xs truncate">{request.reason}</p>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <StatusBadge status={request.status} />
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
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>

                {/* Fund Request Modal */}
                {showFundRequestModal && (
                    <FundRequestModal
                        onClose={() => setShowFundRequestModal(false)}
                        onSubmit={handleFundRequestCreated}
                    />
                )}
            </div>
        </DashboardLayout>
    );
}

function StatusBadge({ status }: { status: string }) {
    const statusStyles: Record<string, string> = {
        PENDING: 'bg-yellow-100 text-yellow-800',
        APPROVED: 'bg-green-100 text-green-800',
        REJECTED: 'bg-red-100 text-red-800',
    };

    return (
        <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusStyles[status] || 'bg-gray-100 text-gray-800'}`}>
            {status}
        </span>
    );
}

function Wallet({ size, className }: { size: number; className?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width={size}
            height={size}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className={className}
        >
            <path d="M21 12V7H5a2 2 0 0 1 0-4h14v4" />
            <path d="M3 5v14a2 2 0 0 0 2 2h16v-5" />
            <path d="M18 12a2 2 0 0 0 0 4h4v-4Z" />
        </svg>
    );
}
