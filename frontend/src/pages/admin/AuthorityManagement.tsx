import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authorityService, AuthorityResponse } from '../../services/authority';
import { userService } from '../../services/user';
import { Shield, Search, Users, CheckCircle2, XCircle } from 'lucide-react';

interface UserOption {
    id: number;
    username: string;
    fullName: string;
    email: string;
}

const AuthorityManagement = () => {
    const navigate = useNavigate();
    const [authorities, setAuthorities] = useState<AuthorityResponse[]>([]);
    const [users, setUsers] = useState<UserOption[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
    const [userAuthorities, setUserAuthorities] = useState<AuthorityResponse[]>([]);
    const [selectedAuthorities, setSelectedAuthorities] = useState<Set<string>>(new Set());
    const [searchTerm, setSearchTerm] = useState('');
    const [filterResource, setFilterResource] = useState<string>('ALL');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Load all authorities on mount
    useEffect(() => {
        loadAuthorities();
        loadUsers();
    }, []);

    // Load user authorities when user is selected
    useEffect(() => {
        if (selectedUserId) {
            loadUserAuthorities(selectedUserId);
        } else {
            setUserAuthorities([]);
            setSelectedAuthorities(new Set());
        }
    }, [selectedUserId]);

    const loadAuthorities = async () => {
        try {
            setLoading(true);
            const data = await authorityService.getAllAuthorities();
            setAuthorities(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load authorities');
        } finally {
            setLoading(false);
        }
    };

    const loadUsers = async () => {
        try {
            // Fetch users with ACCOUNTANT role or all users
            const response = await userService.searchUsers({ role: 'ACCOUNTANT' });
            setUsers(response.users);
        } catch (err: any) {
            console.error('Failed to load users:', err);
        }
    };

    const loadUserAuthorities = async (userId: number) => {
        try {
            setLoading(true);
            const data = await authorityService.getUserAuthorities(userId);
            setUserAuthorities(data);
            // Pre-select current authorities
            setSelectedAuthorities(new Set(data.map(a => a.name)));
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load user authorities');
        } finally {
            setLoading(false);
        }
    };

    const handleAuthorityToggle = (authorityName: string) => {
        const newSelected = new Set(selectedAuthorities);
        if (newSelected.has(authorityName)) {
            newSelected.delete(authorityName);
        } else {
            newSelected.add(authorityName);
        }
        setSelectedAuthorities(newSelected);
    };

    const handleAssignAuthorities = async () => {
        if (!selectedUserId) {
            setError('Please select a user first');
            return;
        }

        if (selectedAuthorities.size === 0) {
            setError('Please select at least one authority');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            await authorityService.assignAuthoritiesToUser(
                selectedUserId,
                Array.from(selectedAuthorities)
            );
            setSuccess('Authorities assigned successfully!');
            // Reload user authorities
            await loadUserAuthorities(selectedUserId);
            setTimeout(() => setSuccess(null), 3000);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to assign authorities');
        } finally {
            setLoading(false);
        }
    };

    // Group authorities by resource
    const groupedAuthorities = authorities.reduce((acc, authority) => {
        if (!acc[authority.resource]) {
            acc[authority.resource] = [];
        }
        acc[authority.resource].push(authority);
        return acc;
    }, {} as Record<string, AuthorityResponse[]>);

    // Filter authorities
    const filteredAuthorities = authorities.filter(authority => {
        const matchesSearch = authority.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            authority.description.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesResource = filterResource === 'ALL' || authority.resource === filterResource;
        return matchesSearch && matchesResource;
    });

    const resources = ['ALL', ...Array.from(new Set(authorities.map(a => a.resource)))];

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center gap-3 mb-2">
                        <Shield className="w-8 h-8 text-blue-600" />
                        <h1 className="text-3xl font-bold text-gray-900">Authority Management</h1>
                    </div>
                    <p className="text-gray-600">Manage user permissions and system authorities</p>
                </div>

                {/* Alerts */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center gap-3">
                        <XCircle className="w-5 h-5 text-red-600" />
                        <p className="text-red-800">{error}</p>
                    </div>
                )}

                {success && (
                    <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-center gap-3">
                        <CheckCircle2 className="w-5 h-5 text-green-600" />
                        <p className="text-green-800">{success}</p>
                    </div>
                )}

                {/* User Selection */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="flex items-center gap-3 mb-4">
                        <Users className="w-5 h-5 text-gray-600" />
                        <h2 className="text-lg font-semibold text-gray-900">Select User</h2>
                    </div>

                    <select
                        value={selectedUserId || ''}
                        onChange={(e) => setSelectedUserId(e.target.value ? Number(e.target.value) : null)}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                        <option value="">-- Select a user --</option>
                        {users.map(user => (
                            <option key={user.id} value={user.id}>
                                {user.fullName} ({user.email})
                            </option>
                        ))}
                    </select>

                    {/* Current Authorities */}
                    {selectedUserId && userAuthorities.length > 0 && (
                        <div className="mt-4">
                            <p className="text-sm font-medium text-gray-700 mb-2">
                                Current Authorities ({userAuthorities.length}):
                            </p>
                            <div className="flex flex-wrap gap-2">
                                {userAuthorities.map(auth => (
                                    <span
                                        key={auth.id}
                                        className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium"
                                    >
                                        {auth.name}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* Search and Filter */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                            <input
                                type="text"
                                placeholder="Search authorities..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>
                        <select
                            value={filterResource}
                            onChange={(e) => setFilterResource(e.target.value)}
                            className="px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {resources.map(resource => (
                                <option key={resource} value={resource}>
                                    {resource === 'ALL' ? 'All Resources' : resource}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* Authorities List */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-semibold text-gray-900 mb-4">
                        Available Authorities ({filteredAuthorities.length})
                    </h2>

                    {loading ? (
                        <div className="text-center py-8">
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                            <p className="mt-4 text-gray-600">Loading authorities...</p>
                        </div>
                    ) : (
                        <div className="space-y-6">
                            {Object.entries(groupedAuthorities).map(([resource, auths]) => {
                                const resourceAuthorities = auths.filter(a =>
                                    filteredAuthorities.some(fa => fa.id === a.id)
                                );

                                if (resourceAuthorities.length === 0) return null;

                                return (
                                    <div key={resource} className="border-b border-gray-200 pb-4 last:border-0">
                                        <h3 className="text-md font-semibold text-gray-800 mb-3 flex items-center gap-2">
                                            <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded text-sm">
                                                {resource}
                                            </span>
                                            <span className="text-sm text-gray-500">
                                                ({resourceAuthorities.length} authorities)
                                            </span>
                                        </h3>
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                            {resourceAuthorities.map(authority => (
                                                <label
                                                    key={authority.id}
                                                    className="flex items-start gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={selectedAuthorities.has(authority.name)}
                                                        onChange={() => handleAuthorityToggle(authority.name)}
                                                        disabled={!selectedUserId}
                                                        className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                                    />
                                                    <div className="flex-1">
                                                        <p className="font-medium text-gray-900 text-sm">
                                                            {authority.name}
                                                        </p>
                                                        <p className="text-xs text-gray-600 mt-1">
                                                            {authority.description}
                                                        </p>
                                                    </div>
                                                </label>
                                            ))}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>

                {/* Action Buttons */}
                <div className="flex gap-4">
                    <button
                        onClick={handleAssignAuthorities}
                        disabled={!selectedUserId || selectedAuthorities.size === 0 || loading}
                        className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                    >
                        {loading ? 'Assigning...' : `Assign Selected Authorities (${selectedAuthorities.size})`}
                    </button>
                    <button
                        onClick={() => navigate('/admin/dashboard')}
                        className="px-6 py-3 border border-gray-300 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                    >
                        Back to Dashboard
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AuthorityManagement;
