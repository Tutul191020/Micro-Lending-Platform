import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { treasuryService, FundPoolResponse } from '../../services/treasury';
import { Wallet, Plus, TrendingUp } from 'lucide-react';

export default function TreasuryPage() {
    const [loading, setLoading] = useState(false);
    const [balance, setBalance] = useState<FundPoolResponse | null>(null);
    const [mintAmount, setMintAmount] = useState('');
    const [mintSource, setMintSource] = useState('Central Bank Reserve');
    const [mintDescription, setMintDescription] = useState('Quarterly fund injection');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        fetchBalance();
    }, []);

    const fetchBalance = async () => {
        try {
            const balanceData = await treasuryService.getBalance();
            setBalance(balanceData);
        } catch (error) {
            console.error('Error fetching balance:', error);
        }
    };

    const handleMintFunds = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            await treasuryService.mintFund({
                amount: Number(mintAmount),
                source: mintSource,
                description: mintDescription
            });

            setSuccess(`Successfully generated ৳${Number(mintAmount).toLocaleString()} virtual money!`);
            setMintAmount('');
            setMintDescription('Quarterly fund injection');
            fetchBalance(); // Refresh balance
        } catch (err: any) {
            console.error('Error minting funds:', err);
            setError(err.response?.data?.message || 'Failed to generate virtual money. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <DashboardLayout>
            <div className="p-8">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">Treasury Management</h1>
                    <p className="text-gray-600">Generate virtual money for the fund pool</p>
                </div>

                {/* Current Balance Card */}
                <div className="bg-gradient-to-br from-teal-500 to-teal-700 rounded-2xl shadow-lg p-8 mb-8 text-white">
                    <div className="flex items-center gap-4 mb-4">
                        <div className="p-4 bg-white/20 rounded-xl backdrop-blur-sm">
                            <Wallet size={32} />
                        </div>
                        <div>
                            <p className="text-teal-100 text-sm font-medium">Current Fund Pool Balance</p>
                            <h2 className="text-4xl font-bold">
                                ৳{balance?.balance?.toLocaleString() || '0'}
                            </h2>
                        </div>
                    </div>
                    <div className="flex items-center gap-2 text-teal-100 text-sm">
                        <TrendingUp size={16} />
                        <span>Available for distribution to accountants</span>
                    </div>
                </div>

                {/* Generate Virtual Money Form */}
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
                    <div className="flex items-center gap-3 mb-6">
                        <div className="p-3 bg-teal-50 rounded-xl">
                            <Plus className="text-teal-600" size={24} />
                        </div>
                        <div>
                            <h3 className="text-xl font-bold text-gray-900">Generate Virtual Money</h3>
                            <p className="text-gray-500 text-sm">Add funds to the treasury pool</p>
                        </div>
                    </div>

                    <form onSubmit={handleMintFunds} className="space-y-6">
                        {/* Amount Input */}
                        <div>
                            <label className="block text-sm font-bold text-gray-700 mb-2">
                                Amount (৳) <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-lg">৳</span>
                                <input
                                    type="number"
                                    required
                                    min="1"
                                    step="0.01"
                                    value={mintAmount}
                                    onChange={(e) => setMintAmount(e.target.value)}
                                    className="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all font-bold text-lg"
                                    placeholder="0.00"
                                />
                            </div>
                        </div>

                        {/* Source Selection */}
                        <div>
                            <label className="block text-sm font-bold text-gray-700 mb-2">
                                Source <span className="text-red-500">*</span>
                            </label>
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

                        {/* Description */}
                        <div>
                            <label className="block text-sm font-bold text-gray-700 mb-2">
                                Description <span className="text-red-500">*</span>
                            </label>
                            <textarea
                                required
                                value={mintDescription}
                                onChange={(e) => setMintDescription(e.target.value)}
                                rows={3}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500 outline-none transition-all resize-none"
                                placeholder="Reason for generating funds..."
                            />
                        </div>

                        {/* Error Message */}
                        {error && (
                            <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                                {error}
                            </div>
                        )}

                        {/* Success Message */}
                        {success && (
                            <div className="p-4 bg-green-50 border border-green-200 rounded-xl text-green-700 text-sm">
                                {success}
                            </div>
                        )}

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full px-6 py-4 bg-teal-600 text-white font-bold rounded-xl hover:bg-teal-700 transition-colors disabled:opacity-70 shadow-lg shadow-teal-200 text-lg"
                        >
                            {loading ? 'Generating...' : 'Generate Virtual Money'}
                        </button>
                    </form>
                </div>

                {/* Info Box */}
                <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-xl">
                    <p className="text-blue-800 text-sm">
                        <strong>Note:</strong> Generated virtual money will be added to the fund pool and can be allocated to accountants for loan disbursement.
                    </p>
                </div>
            </div>
        </DashboardLayout>
    );
}
