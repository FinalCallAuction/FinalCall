// src/components/Register.js

import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const Register = () => {
  const { register } = useContext(AuthContext);
  const navigate = useNavigate(); // To redirect after registration
  const [details, setDetails] = useState({ username: '', email: '', password: '' });

  const handleChange = (e) => {
    setDetails({
      ...details,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await register(details.username, details.email, details.password);
    if (result.success) {
      // Redirect to items page
      navigate('/items');
    } else {
      window.alert(result.message);
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-green-50">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded shadow-md w-full max-w-sm"
      >
        <h2 className="text-2xl font-bold mb-4">Register</h2>
        <div className="mb-4">
          <label className="block text-gray-700">Username:</label>
          <input
            type="text"
            name="username"
            className="w-full px-3 py-2 border rounded mt-1"
            value={details.username}
            onChange={handleChange}
            required
            placeholder="Choose a username"
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">Email:</label>
          <input
            type="email"
            name="email"
            className="w-full px-3 py-2 border rounded mt-1"
            value={details.email}
            onChange={handleChange}
            required
            placeholder="Enter your email"
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700">Password:</label>
          <input
            type="password"
            name="password"
            className="w-full px-3 py-2 border rounded mt-1"
            value={details.password}
            onChange={handleChange}
            required
            placeholder="Create a password"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600"
        >
          Register
        </button>
      </form>
    </div>
  );
};

export default Register;
