// src/components/Profile.js

import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { useNavigate, Link } from 'react-router-dom';

const Profile = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [activeListings, setActiveListings] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
    } else {
      fetchActiveListings();
    }
    // eslint-disable-next-line
  }, [user]);

  const fetchActiveListings = async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/user/${user.id}/active-listings`, {
        method: 'GET',
      });

      if (response.ok) {
        const data = await response.json();
        setActiveListings(data);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch active listings.');
      console.error('Fetch Active Listings Error:', err);
    }
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Profile</h1>
      <div className="mb-6">
        <h2 className="text-xl font-semibold">Username: {user.username}</h2>
        <h2 className="text-xl font-semibold">Email: {user.email}</h2>
      </div>
      <h2 className="text-2xl font-bold mb-4">Active Listings</h2>
      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      )}
      {activeListings.length === 0 ? (
        <p>You have no active listings.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {activeListings.map((item) => (
            <div key={item.id} className="bg-white p-4 rounded shadow">
              {item.imageUrl ? (
                <img
                  src={`http://localhost:8082${item.imageUrl}`}
                  alt={item.name}
                  className="w-full h-48 object-cover rounded mb-4 cursor-pointer"
                  onClick={() => navigate(`/items/${item.id}`)}
                />
              ) : (
                <img
                  src="https://via.placeholder.com/150"
                  alt="Placeholder"
                  className="w-full h-48 object-cover rounded mb-4 cursor-pointer"
                  onClick={() => navigate(`/items/${item.id}`)}
                />
              )}
              <h2 className="text-xl font-semibold mb-2 cursor-pointer" onClick={() => navigate(`/items/${item.id}`)}>
                {item.name}
              </h2>
              <p>
                <strong>Auction Type:</strong> {item.auctionType}
              </p>
              <p>
                <strong>Current Bid:</strong> ${item.currentBid.toFixed(2)}
              </p>
              <p>
                <strong>Time Left:</strong> {new Date(item.auctionEndTime) > new Date() ? 'Ongoing' : 'Ended'}
              </p>
              <Link to={`/items/${item.id}`} className="mt-4 inline-block text-blue-500 hover:text-blue-700">
                View Details
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Profile;
