export interface User {
    id: number;
    username: string;
    fullName: string;
    email: string;
    phone: string;
    nationalId: string;
    dateOfBirth: string;
    address: string;
    city?: string;
    postalCode?: string;
    enabled: boolean;
    verified: boolean;
    verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
    onboardingStatus: 'REGISTERED' | 'KYC_SUBMITTED' | 'KYC_APPROVED' | 'COMPLETED';
    createdAt: string;
    updatedAt: string;
    roles?: string[]; // Assuming roles are sent in some responses
}

export interface Loan {
    id: string; // UUID
    userId: number; // mapped from User entity
    principal: number;
    approvedInterestRate: number;
    interestType: string;
    tenureMonths: number;
    status: 'PENDING' | 'APPROVED' | 'ACTIVE' | 'REJECTED' | 'FULLY_PAID' | 'DEFAULTED';
    outstandingAmount: number;
    currency: string;
    disbursedAt?: string;
    createdAt: string;
    approvedBy?: number;
    approvedAt?: string;
}

export interface LoanRepayment {
    id: string; // UUID
    loanId: string; // UUID
    dueDate: string;
    principalDue: number;
    interestDue: number;
    feeDue: number;
    totalDue: number;
    paidAmount: number;
    originalTotalDue?: number;
    hasDeficitFromPreviousMonth: boolean;
    status: 'PENDING' | 'PAID' | 'PARTIALLY_PAID' | 'OVERDUE';
    paidAt?: string;
    createdAt: string;
}

export interface PaymentTransaction {
    id: string; // UUID
    repaymentId: string;
    loanId: string;
    userId: number;
    gateway: 'BKASH' | 'NAGAD';
    gatewayTxId?: string;
    amount: number;
    status: 'PENDING' | 'SUCCESS' | 'FAILED';
    initiatedAt: string;
    completedAt?: string;
    metadata?: string;
}

// Auth Types
export interface LoginRequest {
    username: string; // or email
    passwordHash: string; // In frontend this is usually just 'password' sent to API
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
}

export interface JwtPayload {
    sub: string; // username
    id: number;
    email: string;
    roles: string[];
    fullName?: string;
    phone?: string;
    exp: number;
    iat: number;
}

export interface RegisterRequest {
    username: string;
    passwordHash: string; // password
    fullName: string;
    email: string;
    phone: string;
    nationalId: string;
    dateOfBirth: string;
    address: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

// Forgot Password Types
export interface ForgotPasswordRequest {
    identifier: string; // email or phone
}

export interface VerifyOtpRequest {
    identifier: string;
    otpCode: string;
}

export interface ResetPasswordRequest {
    identifier: string;
    otpCode: string;
    newPassword: string;
    confirmPassword: string;
}

export interface OtpResponse {
    message: string;
    expiresIn: number; // seconds
    otpSentTo: string; // masked identifier
}
