// src/components/LandingPage.js

import React from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage = () => {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate('/login');
  };

  const handleSignup = () => {
    navigate('/register');
  };

  const handleBrowse = () => {
    navigate('/items');
  };

  return (
    <div className="flex flex-col items-center justify-center h-screen bg-blue-50">
      <h1 className="text-4xl font-bold mb-8">Welcome to FinalCall</h1>
      <div className="space-x-4">
        <button
          onClick={handleLogin}
          className="px-6 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Login
        </button>
        <button
          onClick={handleSignup}
          className="px-6 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          Signup
        </button>
        <button
          onClick={handleBrowse}
          className="px-6 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
        >
          Browse Items
        </button>
      </div>
    </div>
  );
};

export default LandingPage;
