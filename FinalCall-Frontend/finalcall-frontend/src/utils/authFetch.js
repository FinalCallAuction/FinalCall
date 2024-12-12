// src/utils/authFetch.js
export const authFetch = async (url, options = {}, logout) => {
  const userStr = localStorage.getItem('finalcall_user');
  const user = userStr ? JSON.parse(userStr) : null;

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (user && user.accessToken) {
    headers.Authorization = `Bearer ${user.accessToken}`;
  }

  try {
    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 && logout) {
      // Token is invalid or expired
      logout();
      // Optionally, alert the user or navigate to login
      // alert('Session expired. Please login again.');
    }

    return response;
  } catch (error) {
    console.error('authFetch error:', error);
    throw error;
  }
};
