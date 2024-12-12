// src/components/Profile.js

import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { useNavigate } from 'react-router-dom';

const Profile = () => {
  const { user, token, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');

  const fetchProfile = async () => {
    try {
      // Assuming the user object has an 'id' field. If not, adjust accordingly.
      const response = await authFetch(`/api/user/all`, {
        method: 'GET',
      }, logout);

      if (response && response.ok) {
        const users = await response.json();
        // Find the current user in the list
        const currentUser = users.find(u => u.username === user.username);
        if (currentUser) {
          setProfile(currentUser);
        } else {
          setError('User not found.');
        }
      } else {
        const errorMsg = await response.text();
        setError(`Error fetching profile: ${errorMsg}`);
      }
    } catch (err) {
      console.error('Profile Fetch Error:', err);
      setError('Failed to fetch profile.');
    }
  };

  useEffect(() => {
    fetchProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <h1 className="text-3xl font-bold mb-4">Profile</h1>
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"
          role="alert"
        >
          {error}
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="container mx-auto px-4 py-6">
        <h1 className="text-3xl font-bold mb-4">Profile</h1>
        <p>Loading profile...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Profile</h1>
      <div className="bg-white p-6 rounded shadow-md">
        <p><strong>Username:</strong> {profile.username}</p>
        <p><strong>Email:</strong> {profile.email}</p>
        <p><strong>First Name:</strong> {profile.firstName}</p>
        <p><strong>Last Name:</strong> {profile.lastName}</p>
        <p><strong>Street Address:</strong> {profile.streetAddress}</p>
        <p><strong>Province/State:</strong> {profile.province}</p>
        <p><strong>Country:</strong> {profile.country}</p>
        <p><strong>Postal Code:</strong> {profile.postalCode}</p>
        <p><strong>Seller:</strong> {profile.isSeller ? 'Yes' : 'No'}</p>
        {/* Add options to edit profile, change password, etc. */}
      </div>
    </div>
  );
};

export default Profile;
