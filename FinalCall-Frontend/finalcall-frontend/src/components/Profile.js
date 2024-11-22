// src/components/Profile.js

import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { authFetch } from '../utils/authFetch';

const Profile = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [userDetails, setUserDetails] = useState(null);
  const [activeListings, setActiveListings] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
    } else {
      fetchUserDetails();
      fetchActiveListings();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const fetchUserDetails = async () => {
    try {
      const response = await authFetch(`http://localhost:8081/api/user/${user.id}`, {
        method: 'GET',
      }, logout);

      if (response.ok) {
        const data = await response.json();
        setUserDetails(data);
      } else {
        const errorMsg = await response.text();
        setError(`Error: ${errorMsg}`);
      }
    } catch (err) {
      setError('Failed to fetch user details.');
      console.error('Fetch User Details Error:', err);
    }
  };

  const fetchActiveListings = async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/user/active-listings`, {
        method: 'GET',
      }, logout);

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

  if (!userDetails) {
    return (
      <div className="container mx-auto px-4 py-6">
        <p>Loading profile details...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Profile</h1>
      <div className="mb-6">
        <h2 className="text-xl font-semibold">Username: {userDetails.username}</h2>
        <h2 className="text-xl font-semibold">Email: {userDetails.email}</h2>
        <h2 className="text-xl font-semibold">First Name: {userDetails.firstName}</h2>
        <h2 className="text-xl font-semibold">Last Name: {userDetails.lastName}</h2>
        <h2 className="text-xl font-semibold">Address:</h2>
        <p className="ml-4">
          {userDetails.streetAddress}, {userDetails.province}, {userDetails.country}, {userDetails.postalCode}
        </p>
        <h2 className="text-xl font-semibold">Seller Status: {userDetails.isSeller ? 'Seller' : 'Buyer'}</h2>
      </div>
      <div className="flex space-x-4 mb-6">
        <Link
          to="/change-password"
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Change Password
        </Link>
        <Link
          to="/change-address"
          className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
        >
          Change Address
        </Link>
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
