import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    LayoutDashboard,
    Wallet,
    Users,
    FileText,
    Shield,
    TrendingUp,
    AlertCircle,
    CheckCircle,
    XCircle,
    Plus,
    ExternalLink
} from 'lucide-react';
import {
    PieChart, Pie, Cell, ResponsiveContainer, Tooltip as RechartsTooltip,
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Legend
} from 'recharts';
import { fundService, FundRequestResponse } from '../../services/fund';
import { treasuryService, FundPoolResponse } from '../../services/treasury';

// Mock data for charts (replace with real API data later if available)
const ALLOCATION_DATA = [
    { name: 'Disbursed', value: 450000, color: '#10B981' },
    { name: 'Available', value: 300000, color: '#3B82F6' },
    { name: 'Pending', value: 150000, color: '#F59E0B' },
    { name: 'Reserved', value: 50000, color: '#6366F1' },
];

const TREND_DATA = [
    { name: 'Mon', requests: 4, approved: 3 },
    { name: 'Tue', requests: 7, approved: 5 },
    { name: 'Wed', requests: 5, approved: 4 },
    { name: 'Thu', requests: 12, approved: 8 },
    { name: 'Fri', requests: 9, approved: 7 },
    { name: 'Sat', requests: 3, approved: 2 },
    { name: 'Sun', requests: 2, approved: 1 },
];

export default function SuperAdminDashboard() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [balance, setBalance] = useState<FundPoolResponse | null>(null);
    const [pendingRequests, setPendingRequests] = useState<FundRequestResponse[]>([]);
    const [showMintModal, setShowMintModal] = useState(false);
    const [mintAmount, setMintAmount] = useState('');
    const [mintSource, setMintSource] = useState('Central Bank Reserve');
    const [mintDescription, setMintDescription] = useState('Quarterly fund injection');
    const [processingId, setProcessingId] = useState<string | null>(null);
    const [user, setUser] = useState<any>(null);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            setUser(JSON.parse(userData));
        }
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [balanceData, requestsData] = await Promise.all([
                treasuryService.getBalance(),
                fundService.getPendingRequests(0, 5)
            ]);
            setBalance(balanceData);
            setPendingRequests(requestsData.content);
        } catch (error) {
            console.error('Error fetching dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleMintFunds = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            await treasuryService.mintFund({
                amount: Number(mintAmount),
                source: mintSource,
                description: mintDescription
            });
            setShowMintModal(false);
            setMintAmount('');
            fetchData(); // Refresh balance
        } catch (error) {
            console.error('Error minting funds:', error);
            alert('Failed to mint funds');
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (id: string) => {
        if (!confirm('Are you sure you want to approve this request?')) return;
        setProcessingId(id);
        try {
            await fundService.approveRequest(id, 'Approved by SuperAdmin');
            fetchData(); // Refresh list
        } catch (error) {
            console.error('Error approving request:', error);
            alert('Failed to approve request');
        } finally {
            setProcessingId(null);
        }
    };

    const handleReject = async (id: string) => {
        if (!confirm('Are you sure you want to reject this request?')) return;
        setProcessingId(id);
        try {
            await fundService.rejectRequest(id, 'Rejected by SuperAdmin');
            fetchData(); // Refresh list
        } catch (error) {
            console.error('Error rejecting request:', error);
            alert('Failed to reject request');
        } finally {
            setProcessingId(null);
        }
    };

    const authorities = user?.roles || [];

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            {/* Header */}
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Financial Overview</h1>
                    <p className="text-gray-500">Welcome back, {user?.fullName || 'Super Admin'}</p>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={() => setShowMintModal(true)}
                        className="flex items-center gap-2 bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700 transition-colors"
                    >
                        <Plus size={20} />
                        Generate Virtual Money
                    </button>
                </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-2 bg-teal-50 rounded-lg">
                            <Wallet className="text-teal-600" size={24} />
                        </div>
                        <span className="text-xs font-medium text-teal-600 bg-teal-50 px-2 py-1 rounded-full">+12.5%</span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium">Total Treasury Balance</h3>
                    <p className="text-2xl font-bold text-gray-900">
                        ৳{balance?.balance?.toLocaleString() || '0'}
                    </p>
                </div>

                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-2 bg-blue-50 rounded-lg">
                            <AlertCircle className="text-blue-600" size={24} />
                        </div>
                        <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-1 rounded-full">{pendingRequests.length} New</span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium">Pending Requests</h3>
                    <p className="text-2xl font-bold text-gray-900">{pendingRequests.length}</p>
                </div>

                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-2 bg-purple-50 rounded-lg">
                            <Users className="text-purple-600" size={24} />
                        </div>
                        <span className="text-xs font-medium text-purple-600 bg-purple-50 px-2 py-1 rounded-full">+5</span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium">Active Accountants</h3>
                    <p className="text-2xl font-bold text-gray-900">12</p>
                </div>

                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-2 bg-orange-50 rounded-lg">
                            <TrendingUp className="text-orange-600" size={24} />
                        </div>
                        <span className="text-xs font-medium text-orange-600 bg-orange-50 px-2 py-1 rounded-full">+8.2%</span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium">Total Disbursed</h3>
                    <p className="text-2xl font-bold text-gray-900">৳4.5M</p>
                </div>
            </div>

            {/* Charts Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
                {/* Fund Allocation Pie Chart */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h3 className="text-lg font-bold text-gray-900 mb-6">Fund Allocation</h3>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={ALLOCATION_DATA}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="value"
                                >
                                    {ALLOCATION_DATA.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Pie>
                                <RechartsTooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Request Trends Bar Chart */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 lg:col-span-2">
                    <h3 className="text-lg font-bold text-gray-900 mb-6">Request Trends (Weekly)</h3>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={TREND_DATA}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} />
                                <YAxis axisLine={false} tickLine={false} />
                                <RechartsTooltip cursor={{ fill: '#F3F4F6' }} />
                                <Legend />
                                <Bar dataKey="requests" name="Total Requests" fill="#3B82F6" radius={[4, 4, 0, 0]} />
                                <Bar dataKey="approved" name="Approved" fill="#10B981" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Pending Requests Table */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-100 lg:col-span-2 overflow-hidden">
                    <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                        <h3 className="text-lg font-bold text-gray-900">Pending Fund Pools</h3>
                        <button className="text-sm text-teal-600 font-medium hover:text-teal-700">View All</button>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Accountant</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Reason</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {pendingRequests.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                                            No pending requests found.
                                        </td>
                                    </tr>
                                ) : (
                                    pendingRequests.map((request) => (
                                        <tr key={request.id} className="hover:bg-gray-50 transition-colors">
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="flex items-center">
                                                    <div className="h-8 w-8 rounded-full bg-teal-100 flex items-center justify-center text-teal-700 font-bold text-xs">
                                                        {request.requestedBy.fullName.charAt(0)}
                                                    </div>
                                                    <div className="ml-3">
                                                        <p className="text-sm font-medium text-gray-900">{request.requestedBy.fullName}</p>
                                                        <p className="text-xs text-gray-500">{request.requestedBy.username}</p>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span className="text-sm font-bold text-gray-900">৳{request.amount.toLocaleString()}</span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <p className="text-sm text-gray-500 truncate max-w-xs">{request.reason}</p>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span className="text-sm text-gray-500">
                                                    {new Date(request.createdAt).toLocaleDateString()}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                                <div className="flex justify-end gap-2">
                                                    <button
                                                        onClick={() => handleApprove(request.id)}
                                                        disabled={processingId === request.id}
                                                        className="p-1 text-green-600 hover:bg-green-50 rounded transition-colors"
                                                        title="Approve"
                                                    >
                                                        <CheckCircle size={20} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleReject(request.id)}
                                                        disabled={processingId === request.id}
                                                        className="p-1 text-red-600 hover:bg-red-50 rounded transition-colors"
                                                        title="Reject"
                                                    >
                                                        <XCircle size={20} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Authorities Panel */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                    <div className="flex items-center gap-2 mb-6">
                        <Shield className="text-teal-600" size={24} />
                        <h3 className="text-lg font-bold text-gray-900">Your Authorities</h3>
                    </div>
                    <div className="space-y-3 max-h-[400px] overflow-y-auto pr-2">
                        {authorities.map((auth: string, index: number) => (
                            <div
                                key={index}
                                className="group flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-teal-50 border border-transparent hover:border-teal-100 transition-all cursor-pointer"
                                onClick={() => alert(`Authority Details: ${auth}\n\nThis permission allows you to perform specific actions related to ${auth.split(':')[0]} management.`)}
                            >
                                <div className="flex items-center gap-3">
                                    <div className="h-2 w-2 rounded-full bg-teal-500"></div>
                                    <span className="text-sm font-medium text-gray-700 group-hover:text-teal-700">{auth}</span>
                                </div>
                                <ExternalLink size={14} className="text-gray-400 group-hover:text-teal-500 opacity-0 group-hover:opacity-100 transition-opacity" />
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Mint Funds Modal */}
            {showMintModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-in fade-in zoom-in duration-200">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-xl font-bold text-gray-900">Generate Virtual Money</h3>
                            <button
                                onClick={() => setShowMintModal(false)}
                                className="text-gray-400 hover:text-gray-600"
                            >
                                <XCircle size={24} />
                            </button>
                        </div>

                        <form onSubmit={handleMintFunds} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Amount (৳)</label>
                                <input
                                    type="number"
                                    required
                                    min="1"
                                    value={mintAmount}
                                    onChange={(e) => setMintAmount(e.target.value)}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all"
                                    placeholder="Enter amount to mint"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Source</label>
                                <select
                                    value={mintSource}
                                    onChange={(e) => setMintSource(e.target.value)}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all"
                                >
                                    <option value="Central Bank Reserve">Central Bank Reserve</option>
                                    <option value="Government Grant">Government Grant</option>
                                    <option value="External Investment">External Investment</option>
                                    <option value="System Adjustment">System Adjustment</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                                <textarea
                                    required
                                    value={mintDescription}
                                    onChange={(e) => setMintDescription(e.target.value)}
                                    rows={3}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all"
                                    placeholder="Reason for minting funds..."
                                />
                            </div>

                            <div className="flex gap-3 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setShowMintModal(false)}
                                    className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="flex-1 px-4 py-2 bg-teal-600 text-white rounded-lg hover:bg-teal-700 transition-colors disabled:opacity-50"
                                >
                                    {loading ? 'Processing...' : 'Mint Funds'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
