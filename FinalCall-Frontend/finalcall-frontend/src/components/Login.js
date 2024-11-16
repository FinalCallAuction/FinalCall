// src/components/Login.js

import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const Login = () => {
  const { login } = useContext(AuthContext);
  const navigate = useNavigate(); // To redirect after login
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await login(username, password);
    if (result.success) {
      // Redirect to items page
      navigate('/items');
    } else {
      window.alert(result.message);
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-blue-50">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded shadow-md w-full max-w-sm"
      >
        <h2 className="text-2xl font-bold mb-4">Login</h2>
        <div className="mb-4">
          <label className="block text-gray-700">Username:</label>
          <input
            type="text"
            className="w-full px-3 py-2 border rounded mt-1"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            placeholder="Enter your username"
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700">Password:</label>
          <input
            type="password"
            className="w-full px-3 py-2 border rounded mt-1"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="Enter your password"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600"
        >
          Login
        </button>
      </form>
    </div>
  );
};

export default Login;
