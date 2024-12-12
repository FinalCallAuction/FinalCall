// src/components/Callback.js
import React, { useEffect, useContext, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Callback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { setUser } = useContext(AuthContext);
  const [error, setError] = useState('');

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const storedState = localStorage.getItem('oauth_state');

    if (state !== storedState) {
      setError('Invalid state parameter');
      return;
    }

    if (!code) {
      setError('No authorization code provided');
      return;
    }

    const exchangeCode = async () => {
      try {
        const data = new URLSearchParams();
        data.append('grant_type', 'authorization_code');
        data.append('code', code);
        data.append('redirect_uri', 'http://localhost:3000/callback');
        data.append('client_id', 'frontend-client');
        data.append('client_secret', 'frontend-secret');

        const response = await fetch('http://localhost:8081/oauth2/token', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: data.toString(),
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText);
        }

        const tokenResponse = await response.json();
        const accessToken = tokenResponse.access_token;

        // Decode JWT to extract user information
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        const username = payload.sub || '';
        const email = payload.email || '';

        // Optionally, fetch additional user details using accessToken
        // Example: const userInfoResponse = await fetch('http://localhost:8081/api/user/me', { headers: { Authorization: `Bearer ${accessToken}` } });
        // Handle userInfoResponse...

        setUser({ username, email, accessToken });
        localStorage.removeItem('oauth_state'); // Clean up
        navigate('/');
      } catch (err) {
        setError('Failed to exchange code: ' + err.message);
      }
    };

    exchangeCode();
  }, [searchParams, setUser, navigate]);

  return (
    <div className="container mx-auto px-4 py-6">
      {error ? (
        <div className="text-red-500">{error}</div>
      ) : (
        <p>Processing login...</p>
      )}
    </div>
  );
};

export default Callback;
