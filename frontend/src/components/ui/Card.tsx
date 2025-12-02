import React from 'react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
    children: React.ReactNode;
}

export function Card({ className, children, ...props }: CardProps) {
    return (
        <div
            className={cn(
                'rounded-xl border border-gray-100 bg-white shadow-sm',
                className
            )}
            {...props}
        >
            {children}
        </div>
    );
}

export function CardHeader({ className, children, ...props }: CardProps) {
    return (
        <div className={cn('flex flex-col space-y-1.5 p-6', className)} {...props}>
            {children}
        </div>
    );
}

export function CardTitle({ className, children, ...props }: CardProps) {
    return (
        <h3
            className={cn(
                'text-2xl font-semibold leading-none tracking-tight text-secondary',
                className
            )}
            {...props}
        >
            {children}
        </div>
    );
}

export function CardContent({ className, children, ...props }: CardProps) {
    return (
        <div className={cn('p-6 pt-0', className)} {...props}>
            {children}
        </div>
    );
}
