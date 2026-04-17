interface EnvironmentConfig {
    API_BASE_URL: string;
}

export const env: EnvironmentConfig = {
    API_BASE_URL: process.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
};
if (!env.API_BASE_URL) {
    throw new Error('Missing VITE_API_BASE_URL environment variable.');
}
