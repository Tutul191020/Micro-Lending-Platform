import api from './api';
import { ApiResponse } from '../types';

export interface FundPoolResponse {
    id: string;
    balance: number;
    ownerId: number;
    lastUpdated: string;
}

export interface TreasuryMintRequest {
    amount: number;
    source: string;
    description: string;
}

export interface AccountantFundPoolResponse extends FundPoolResponse {
    totalDisbursed: number;
    activeLoans: number;
    overdueLoans: number;
}

export const treasuryService = {
    mintFund: async (data: TreasuryMintRequest): Promise<FundPoolResponse> => {
        const response = await api.post<ApiResponse<FundPoolResponse>>('/treasury/mint', data);
        return response.data.data;
    },

    getBalance: async (): Promise<FundPoolResponse> => {
        const response = await api.get<ApiResponse<FundPoolResponse>>('/treasury/balance');
        return response.data.data;
    },

    getAccountantBalance: async (): Promise<AccountantFundPoolResponse> => {
        const response = await api.get<ApiResponse<AccountantFundPoolResponse>>('/treasury/accountant/balance');
        return response.data.data;
    },
};
