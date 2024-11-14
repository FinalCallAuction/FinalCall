// src/utils/authFetch.js

export const authFetch = async (url, options = {}) => {
  const auth = JSON.parse(localStorage.getItem('auth')) || {};
  const { token } = auth;

  const authHeaders = token
    ? {
        Authorization: `Bearer ${token}`,
      }
    : {};

  // Determine if the body is FormData
  const isFormData = options.body instanceof FormData;

  const headers = isFormData
    ? { ...authHeaders } // Let the browser set the correct Content-Type for FormData
    : {
        'Content-Type': 'application/json',
        ...authHeaders,
      };

  return fetch(url, {
    ...options,
    headers,
  });
};
