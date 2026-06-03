/**
 * Base URL de la API. En desarrollo usa el proxy de Vite (/api) para evitar CORS y permitir SSE.
 */
export function getApiBase() {
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL.replace(/\/$/, '');
  }
  return '/api';
}

export function getAlertStreamUrl() {
  const token = localStorage.getItem('token');
  const base = getApiBase();
  const url = `${base}/alertas/stream`;
  if (!token) {
    return url;
  }
  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}token=${encodeURIComponent(token)}`;
}
