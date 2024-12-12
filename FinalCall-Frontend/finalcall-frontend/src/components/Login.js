// src/components/Login.js
import React from 'react';

const Login = () => {
  const handleLogin = () => {
    const clientId = 'frontend-client';
    const redirectUri = 'http://localhost:3000/callback';
    const authorizationEndpoint = 'http://localhost:8081/oauth2/authorize';
    const scope = 'read write';
    const responseType = 'code';
    const state = generateRandomString(16); // Optional: for CSRF protection

    const url = `${authorizationEndpoint}?client_id=${clientId}&redirect_uri=${encodeURIComponent(
      redirectUri
    )}&response_type=${responseType}&scope=${encodeURIComponent(scope)}&state=${state}`;

    // Optionally, store the state in localStorage to verify later
    localStorage.setItem('oauth_state', state);

    window.location.href = url;
  };

  const generateRandomString = (length) => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Login</h1>
      <p>Click the button below to login via FinalCall Authentication Service.</p>
      <button
        onClick={handleLogin}
        className="mt-4 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
      >
        Login with FinalCall
      </button>
    </div>
  );
};

export default Login;
