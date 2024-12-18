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

    if (response.status === 401) {
      if (logout) logout();
      // Optionally redirect to login or show error
      throw new Error('Unauthorized');
    }

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText);
    }

    return response;
  } catch (error) {
    console.error('authFetch error:', error);
    throw error;
  }
};