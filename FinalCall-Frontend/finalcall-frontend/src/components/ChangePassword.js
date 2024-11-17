// src/components/ChangePassword.js

import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { authFetch } from '../utils/authFetch';

const ChangePassword = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setPasswords({
      ...passwords,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    const { currentPassword, newPassword, confirmNewPassword } = passwords;

    if (newPassword !== confirmNewPassword) {
      setMessage('New passwords do not match.');
      return;
    }

    try {
      const response = await authFetch(`http://localhost:8081/api/user/${user.id}/password`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ password: newPassword }),
      });

      if (response.ok) {
        setMessage('Password updated successfully.');
        // Optionally, redirect or perform other actions
        navigate('/profile'); // Assuming you have a profile page
      } else {
        const errorMsg = await response.text();
        setMessage(`Error: ${errorMsg}`);
      }
    } catch (err) {
      setMessage('An error occurred while updating the password.');
      console.error('Change Password Error:', err);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-yellow-50">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded shadow-md w-full max-w-md"
      >
        <h2 className="text-2xl font-bold mb-6">Change Password</h2>

        {message && (
          <div
            className={`mb-4 p-3 rounded ${
              message.startsWith('Error') ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'
            }`}
            role="alert"
          >
            {message}
          </div>
        )}

        {/* Current Password */}
        <div className="mb-4">
          <label className="block text-gray-700">Current Password:</label>
          <input
            type="password"
            name="currentPassword"
            className="w-full px-3 py-2 border rounded mt-1"
            value={passwords.currentPassword}
            onChange={handleChange}
            required
            placeholder="Enter your current password"
          />
        </div>

        {/* New Password */}
        <div className="mb-4">
          <label className="block text-gray-700">New Password:</label>
          <input
            type="password"
            name="newPassword"
            className="w-full px-3 py-2 border rounded mt-1"
            value={passwords.newPassword}
            onChange={handleChange}
            required
            placeholder="Enter your new password"
          />
        </div>

        {/* Confirm New Password */}
        <div className="mb-6">
          <label className="block text-gray-700">Confirm New Password:</label>
          <input
            type="password"
            name="confirmNewPassword"
            className="w-full px-3 py-2 border rounded mt-1"
            value={passwords.confirmNewPassword}
            onChange={handleChange}
            required
            placeholder="Confirm your new password"
          />
        </div>

        <button
          type="submit"
          className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600"
        >
          Update Password
        </button>
      </form>
    </div>
  );
};

export default ChangePassword;
