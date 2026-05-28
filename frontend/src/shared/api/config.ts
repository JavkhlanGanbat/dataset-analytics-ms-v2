export const API_GATEWAY_URL =
  import.meta.env.VITE_API_GATEWAY_URL?.replace(/\/$/, '') ?? 'http://localhost:8080';
