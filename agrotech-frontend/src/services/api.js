import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
});

// Request interceptor - agregar JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - manejo de errores y retry
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config;

    // Si es error 401, limpiar token y redirigir a login
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // Retry logic para errores de red temporales (máximo 3 intentos)
    if (!config.retry) {
      config.retry = 0;
    }

    if (config.retry < 3 && error.code !== 'ECONNABORTED' && (!error.response || error.response.status >= 500)) {
      config.retry += 1;
      // Exponential backoff: 1s, 2s, 4s
      const delayMs = Math.pow(2, config.retry - 1) * 1000;
      await new Promise((resolve) => setTimeout(resolve, delayMs));
      return api(config);
    }

    return Promise.reject(error);
  }
);

export default api;
