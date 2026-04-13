'use client';

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { AuthContextType } from '@/types/auth';
import {
    getCurrentUser,
    login as loginService,
    logout as logoutService,
    refreshToken as refreshTokenService,
    register as registerService,
} from '@/services/authService';
import { useUserStore } from '@/stores/useUserStore';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isLoading, setIsLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const setUser = useUserStore((state) => state.setUser);
    const clearUser = useUserStore((state) => state.clearUser);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const currentUser = await getCurrentUser();
                if (currentUser) {
                    setUser(currentUser);
                    setIsAuthenticated(true);
                } else {
                    clearUser();
                    setIsAuthenticated(false);
                }
            } catch {
                clearUser();
                setIsAuthenticated(false);
            } finally {
                setIsLoading(false);
            }
        };

        checkAuth();
    }, [clearUser, setUser]);

    const loginCallBack = useCallback(async (email: string, password: string) => {
        setIsLoading(true);
        try {
            await loginService({ email, password });

            const currentUser = await getCurrentUser();

            if (!currentUser) {
                throw new Error('User not found after login');
            }

            setUser(currentUser);
            setIsAuthenticated(true);
        } catch (error) {
            clearUser();
            setIsAuthenticated(false);
            throw error;
        } finally {
            setIsLoading(false);
        }
    }, [setUser, clearUser]);

    const registerCallback = useCallback(
        async (fullName: string, email: string, password: string) => {
            await registerService({ fullName, email, password });
        },
        [],
    );

    const logout = useCallback(async () => {
        setIsLoading(true);
        try {
            await logoutService();
            clearUser();
            setIsAuthenticated(false);
        } finally {
            setIsLoading(false);
        }
    }, [clearUser]);

    const refreshToken = useCallback(async () => {
        try {
            await refreshTokenService();
            const currentUser = await getCurrentUser();
            setUser(currentUser);
            setIsAuthenticated(true);
        } catch (error: unknown) {
            clearUser();
            setIsAuthenticated(false);
            throw error;
        }
    }, [clearUser, setUser]);

    const value: AuthContextType = {
        isLoading,
        isAuthenticated,
        login: loginCallBack,
        register: registerCallback,
        logout,
        refreshToken,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};
