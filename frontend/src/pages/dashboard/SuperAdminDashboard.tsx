import React, { useEffect, useState } from 'react';

import {
    Wallet,
    TrendingDown,
    DollarSign,
    Plus,
    MoreHorizontal,
    ArrowUpRight,
} from 'lucide-react';
import {
    AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell
} from 'recharts';
import { fundService, FundRequestResponse } from '../../services/fund';
import { treasuryService, FundPoolResponse } from '../../services/treasury';
import DashboardLayout from '../../components/layout/DashboardLayout';

// Mock data for charts
const MONEY_STATS_DATA = [
    { name: 'Mon', income: 4000, expense: 2400 },
    { name: 'Tue', income: 3000, expense: 1398 },
    { name: 'Wed', income: 2000, expense: 9800 },
    { name: 'Thu', income: 2780, expense: 3908 },
    { name: 'Fri', income: 1890, expense: 4800 },
    { name: 'Sat', income: 2390, expense: 3800 },
    { name: 'Sun', income: 3490, expense: 4300 },
];

const EXPENSE_BREAKDOWN_DATA = [
    { name: 'Disbursed', value: 4500, color: '#10B981' }, // Green
    { name: 'Operational', value: 1200, color: '#3B82F6' }, // Blue
    { name: 'Reserved', value: 800, color: '#F59E0B' }, // Yellow
    { name: 'Other', value: 500, color: '#6366F1' }, // Indigo
];

export default function SuperAdminDashboard() {
    const [loading, setLoading] = useState(true);
    const [balance, setBalance] = useState<FundPoolResponse | null>(null);
    const [pendingRequests, setPendingRequests] = useState<FundRequestResponse[]>([]);
    const [showMintModal, setShowMintModal] = useState(false);
    const [mintAmount, setMintAmount] = useState('');
    const [mintSource, setMintSource] = useState('Central Bank Reserve');
    const [mintDescription, setMintDescription] = useState('Quarterly fund injection');

    useEffect(() => {
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
            fetchData();
        } catch (error) {
            console.error('Error minting funds:', error);
            alert('Failed to mint funds');
        } finally {
            setLoading(false);
        }
    };

    return (
        <DashboardLayout>
            {/* Stats Cards Row */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                {/* Total Balance */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-3 bg-teal-50 rounded-xl">
                            <Wallet className="text-teal-600" size={24} />
                        </div>
                        <span className="flex items-center gap-1 text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-full">
                            <ArrowUpRight size={12} /> +15%
                        </span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium mb-1">Total Balance</h3>
                    <p className="text-3xl font-bold text-gray-900">
                        ৳{balance?.balance?.toLocaleString() || '0'}
                    </p>
                    <p className="text-xs text-gray-400 mt-2">Updated just now</p>
                </div>

                {/* Total Spending (Disbursed) */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-3 bg-blue-50 rounded-xl">
                            <TrendingDown className="text-blue-600" size={24} />
                        </div>
                        <span className="flex items-center gap-1 text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-full">
                            <ArrowUpRight size={12} /> +8%
                        </span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium mb-1">Total Disbursed</h3>
                    <p className="text-3xl font-bold text-gray-900">৳4.5M</p>
                    <p className="text-xs text-gray-400 mt-2">Since last month</p>
                </div>

                {/* Total Earning */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <div className="p-3 bg-purple-50 rounded-xl">
                            <DollarSign className="text-purple-600" size={24} />
                        </div>
                        <span className="flex items-center gap-1 text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-full">
                            <ArrowUpRight size={12} /> +2%
                        </span>
                    </div>
                    <h3 className="text-gray-500 text-sm font-medium mb-1">Total Earning</h3>
                    <p className="text-3xl font-bold text-gray-900">৳680.4K</p>
                    <p className="text-xs text-gray-400 mt-2">Since last month</p>
                </div>

                {/* Quick Action Card (Mint) */}
                <div className="bg-gradient-to-br from-teal-500 to-teal-700 p-6 rounded-2xl shadow-lg text-white relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -mr-10 -mt-10"></div>
                    <div className="absolute bottom-0 left-0 w-24 h-24 bg-white opacity-10 rounded-full -ml-10 -mb-10"></div>

                    <div className="relative z-10">
                        <div className="flex justify-between items-start mb-6">
                            <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                                <Plus className="text-white" size={24} />
                            </div>
                            <button className="p-1 hover:bg-white/10 rounded-lg transition-colors">
                                <MoreHorizontal size={20} />
                            </button>
                        </div>
                        <h3 className="text-teal-100 text-sm font-medium mb-1">Treasury</h3>
                        <p className="text-2xl font-bold mb-4">Generate Funds</p>
                        <button
                            onClick={() => setShowMintModal(true)}
                            className="w-full py-2 bg-white text-teal-700 font-bold rounded-lg hover:bg-teal-50 transition-colors text-sm"
                        >
                            Add Virtual Money
                        </button>
                    </div>
                </div>
            </div>

            {/* Charts Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
                {/* Money Statistics (Line/Area Chart) */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 lg:col-span-2">
                    <div className="flex justify-between items-center mb-6">
                        <h3 className="text-lg font-bold text-gray-900">Money Statistics</h3>
                        <div className="flex gap-2">
                            <span className="flex items-center gap-2 text-sm text-gray-500">
                                <span className="w-2 h-2 rounded-full bg-teal-500"></span> Income
                            </span>
                            <span className="flex items-center gap-2 text-sm text-gray-500">
                                <span className="w-2 h-2 rounded-full bg-orange-500"></span> Expense
                            </span>
                            <select className="ml-4 text-sm border-none bg-gray-50 rounded-lg px-2 py-1 outline-none text-gray-600">
                                <option>Weekly</option>
                                <option>Monthly</option>
                                <option>Yearly</option>
                            </select>
                        </div>
                    </div>
                    <div className="h-72">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={MONEY_STATS_DATA}>
                                <defs>
                                    <linearGradient id="colorIncome" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#14b8a6" stopOpacity={0.1} />
                                        <stop offset="95%" stopColor="#14b8a6" stopOpacity={0} />
                                    </linearGradient>
                                    <linearGradient id="colorExpense" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#f97316" stopOpacity={0.1} />
                                        <stop offset="95%" stopColor="#f97316" stopOpacity={0} />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#9ca3af', fontSize: 12 }} dy={10} />
                                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#9ca3af', fontSize: 12 }} />
                                <Tooltip
                                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                                />
                                <Area type="monotone" dataKey="income" stroke="#14b8a6" strokeWidth={3} fillOpacity={1} fill="url(#colorIncome)" />
                                <Area type="monotone" dataKey="expense" stroke="#f97316" strokeWidth={3} fillOpacity={1} fill="url(#colorExpense)" />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Expense Breakdown (Pie Chart) */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-center mb-6">
                        <h3 className="text-lg font-bold text-gray-900">Expense Breakdown</h3>
                        <select className="text-sm border-none bg-gray-50 rounded-lg px-2 py-1 outline-none text-gray-600">
                            <option>Today</option>
                            <option>This Week</option>
                        </select>
                    </div>
                    <div className="h-64 relative">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={EXPENSE_BREAKDOWN_DATA}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="value"
                                >
                                    {EXPENSE_BREAKDOWN_DATA.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} strokeWidth={0} />
                                    ))}
                                </Pie>
                                <Tooltip />
                            </PieChart>
                        </ResponsiveContainer>
                        {/* Center Text */}
                        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-center">
                            <p className="text-xs text-gray-400">Total</p>
                            <p className="text-xl font-bold text-gray-900">৳7,000</p>
                        </div>
                    </div>

                    <div className="space-y-3 mt-4">
                        {EXPENSE_BREAKDOWN_DATA.map((item, index) => (
                            <div key={index} className="flex justify-between items-center">
                                <div className="flex items-center gap-2">
                                    <div className="w-2 h-2 rounded-full" style={{ backgroundColor: item.color }}></div>
                                    <span className="text-sm text-gray-600">{item.name}</span>
                                </div>
                                <span className="text-sm font-bold text-gray-900">{Math.round((item.value / 7000) * 100)}%</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Recent Transactions Table */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                    <h3 className="text-lg font-bold text-gray-900">Recent Transactions</h3>
                    <div className="flex gap-2">
                        <select className="text-sm border border-gray-200 rounded-lg px-3 py-2 outline-none text-gray-600">
                            <option>All Transactions</option>
                            <option>Pending</option>
                            <option>Completed</option>
                        </select>
                    </div>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead className="bg-gray-50/50">
                            <tr>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Transaction Name</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Date & Time</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Amount</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
                                <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Action</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {pendingRequests.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                                        No recent transactions found.
                                    </td>
                                </tr>
                            ) : (
                                pendingRequests.map((request) => (
                                    <tr key={request.id} className="hover:bg-gray-50/50 transition-colors">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center">
                                                <div className="h-10 w-10 rounded-full bg-teal-100 flex items-center justify-center text-teal-700 font-bold text-sm">
                                                    {request.requestedBy.fullName.charAt(0)}
                                                </div>
                                                <div className="ml-3">
                                                    <p className="text-sm font-bold text-gray-900">{request.reason}</p>
                                                    <p className="text-xs text-gray-500">{request.requestedBy.fullName}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">{new Date(request.createdAt).toLocaleDateString()}</div>
                                            <div className="text-xs text-gray-500">{new Date(request.createdAt).toLocaleTimeString()}</div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm font-bold text-gray-900">৳{request.amount.toLocaleString()}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                                                Pending
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <button className="text-teal-600 hover:text-teal-900 font-medium">View Details</button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Mint Funds Modal */}
            {showMintModal && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8 animate-in fade-in zoom-in duration-200">
                        <div className="flex justify-between items-center mb-8">
                            <div>
                                <h3 className="text-2xl font-bold text-gray-900">Generate Funds</h3>
                                <p className="text-gray-500 text-sm mt-1">Add virtual currency to the treasury</p>
                            </div>
                            <button
                                onClick={() => setShowMintModal(false)}
                                className="p-2 bg-gray-100 rounded-full text-gray-500 hover:bg-gray-200 transition-colors"
                            >
                                <Plus className="rotate-45" size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleMintFunds} className="space-y-6">
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-2">Amount (৳)</label>
                                <div className="relative">
                                    <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold">৳</span>
                                    <input
                                        type="number"
                                        required
                                        min="1"
                                        value={mintAmount}
                                        onChange={(e) => setMintAmount(e.target.value)}
                                        className="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all font-bold text-lg"
                                        placeholder="0.00"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-2">Source</label>
                                <select
                                    value={mintSource}
                                    onChange={(e) => setMintSource(e.target.value)}
                                    className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all"
                                >
                                    <option value="Central Bank Reserve">Central Bank Reserve</option>
                                    <option value="Government Grant">Government Grant</option>
                                    <option value="External Investment">External Investment</option>
                                    <option value="System Adjustment">System Adjustment</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-2">Description</label>
                                <textarea
                                    required
                                    value={mintDescription}
                                    onChange={(e) => setMintDescription(e.target.value)}
                                    rows={3}
                                    className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all resize-none"
                                    placeholder="Reason for minting funds..."
                                />
                            </div>

                            <div className="flex gap-4 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setShowMintModal(false)}
                                    className="flex-1 px-6 py-3 border border-gray-200 text-gray-700 font-bold rounded-xl hover:bg-gray-50 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="flex-1 px-6 py-3 bg-teal-600 text-white font-bold rounded-xl hover:bg-teal-700 transition-colors disabled:opacity-70 shadow-lg shadow-teal-200"
                                >
                                    {loading ? 'Processing...' : 'Confirm'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
