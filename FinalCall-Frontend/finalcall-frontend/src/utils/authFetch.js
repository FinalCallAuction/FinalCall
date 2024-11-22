// src/utils/authFetch.js

export const authFetch = async (url, options = {}, logout) => {
  const token = localStorage.getItem('token');

  if (!token) {
    console.error('No token found. Redirecting to login.');
    logout();
    return;
  }

  // Set Authorization header
  options.headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  try {
    const response = await fetch(url, options);

    if (response.status === 401) {
      // Handle unauthorized (possibly expired token)
      console.error('Unauthorized. Token may have expired. Logging out...');
      logout();
      return;
    }

    return response;
  } catch (error) {
    console.error('authFetch Error:', error);
    throw error;
  }
};
