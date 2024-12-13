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
        const userId = payload.sub || ''; // Now 'sub' should be user ID
        const email = payload.email || '';
        const isSeller = payload.isSeller || false;

        // Fetch full user details by user ID
        const userResp = await fetch(`http://localhost:8081/api/user/${encodeURIComponent(userId)}`, {
          headers: {
            'Authorization': `Bearer ${accessToken}`
          }
        });
        if (!userResp.ok) {
          const userErr = await userResp.text();
          throw new Error(userErr);
        }
        const userData = await userResp.json();

        // Set user with full details including id, isSeller
        setUser({ 
          username: userData.username, 
          email: userData.email, 
          id: userData.id,
          isSeller: userData.isSeller,
          accessToken 
        });
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
