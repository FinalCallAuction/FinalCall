// src/utils/authFetch.js

export const getToken = () => {
  return localStorage.getItem('token');
};

export const authFetch = async (url, options = {}) => {
  const token = getToken();
  const headers = options.headers || {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return fetch(url, { ...options, headers });
};
