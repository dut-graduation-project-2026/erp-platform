interface EnvironmentConfig {
    API_BASE_URL: string;
}

export const env: EnvironmentConfig = {
    API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || process.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
};
if (!env.API_BASE_URL) {
    throw new Error('Missing NEXT_PUBLIC_API_BASE_URL or VITE_API_BASE_URL environment variable.');
}
