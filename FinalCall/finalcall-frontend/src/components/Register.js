// src/components/Register.js

import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Register = () => {
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);
  const [details, setDetails] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setDetails({ 
      ...details, 
      [e.target.name]: e.target.value 
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch('http://localhost:8094/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(details)
      });

      if (response.ok) {
        // After successful registration, auto-login the user
        const loginResponse = await fetch('http://localhost:8094/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ username: details.username, password: details.password })
        });

        if (loginResponse.ok) {
          const data = await loginResponse.json();
          login(data.user); // Update AuthContext with user data
          navigate('/items');
        } else {
          const errorMessage = await loginResponse.text();
          setError('Registration successful, but auto-login failed.');
          console.error('Auto-Login Error:', errorMessage);
        }
      } else {
        const errorMessage = await response.text();
        setError(errorMessage);
      }
    } catch (err) {
      setError('An error occurred. Please try again.');
      console.error('Registration Error:', err);
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-green-50">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded shadow-md w-full max-w-sm"
      >
        <h2 className="text-2xl font-bold mb-4">Signup</h2>
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <div className="mb-4">
          <label className="block text-gray-700">Username</label>
          <input
            type="text"
            name="username"
            value={details.username}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border rounded mt-1"
            placeholder="Choose a username"
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">Email</label>
          <input
            type="email"
            name="email"
            value={details.email}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border rounded mt-1"
            placeholder="Enter your email"
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700">Password</label>
          <input
            type="password"
            name="password"
            value={details.password}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border rounded mt-1"
            placeholder="Create a password"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600"
        >
          Signup
        </button>
      </form>
    </div>
  );
};

export default Register;
