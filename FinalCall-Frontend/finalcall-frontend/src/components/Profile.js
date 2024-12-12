// src/components/Profile.js
import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { useNavigate } from 'react-router-dom';

const Profile = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [listedItems, setListedItems] = useState([]);
  const [biddedItems, setBiddedItems] = useState([]);
  const [error, setError] = useState('');

  const fetchProfile = async () => {
    try {
      // We already have user details in user object
      // If you want to re-fetch from AuthenticationService, you can:
      const response = await authFetch(`http://localhost:8081/api/user/${user.id}`, {
        method: 'GET',
      }, logout);

      if (response && response.ok) {
        const data = await response.json();
        setProfile(data);
      } else {
        const errorMsg = await response.text();
        setError(`Error fetching profile: ${errorMsg}`);
      }
    } catch (err) {
      console.error('Profile Fetch Error:', err);
      setError('Failed to fetch profile.');
    }
  };

  const fetchListedItems = async () => {
    try {
      const response = await authFetch('http://localhost:8082/api/items/user', {
        method: 'GET',
      }, logout);

      if (response.ok) {
        const data = await response.json();
        setListedItems(data);
      } else {
        const errorMsg = await response.text();
        console.error('Error fetching user items:', errorMsg);
      }
    } catch (err) {
      console.error('Error fetching user items:', err);
    }
  };

  const fetchBiddedItems = async () => {
    try {
      const response = await authFetch(`http://localhost:8084/api/auctions/user/${user.id}/bids`, {
        method: 'GET',
      }, logout);

      if (response.ok) {
        const itemIds = await response.json(); // Array of itemIds
        // Fetch details for each item
        const itemsDetails = [];
        for (const itemId of itemIds) {
          const itemResp = await authFetch(`http://localhost:8082/api/items/${itemId}`, {method: 'GET'}, logout);
          if (itemResp.ok) {
            const itemData = await itemResp.json();
            itemsDetails.push(itemData);
          }
        }
        setBiddedItems(itemsDetails);
      } else {
        const errorMsg = await response.text();
        console.error('Error fetching bidded items:', errorMsg);
      }
    } catch (err) {
      console.error('Error fetching bidded items:', err);
    }
  };

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    fetchProfile();
    fetchListedItems();
    fetchBiddedItems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

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
      <div className="bg-white p-6 rounded shadow-md mb-6">
        <p><strong>Username:</strong> {profile.username}</p>
        <p><strong>Email:</strong> {profile.email}</p>
        <p><strong>First Name:</strong> {profile.firstName}</p>
        <p><strong>Last Name:</strong> {profile.lastName}</p>
        <p><strong>Street Address:</strong> {profile.streetAddress}</p>
        <p><strong>Province/State:</strong> {profile.province}</p>
        <p><strong>Country:</strong> {profile.country}</p>
        <p><strong>Postal Code:</strong> {profile.postalCode}</p>
        <p><strong>Seller:</strong> {profile.isSeller ? 'Yes' : 'No'}</p>
      </div>

      <h2 className="text-2xl font-semibold mb-4">My Listed Items</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        {listedItems.map((item) => (
          <div key={item.id} className="border p-4 rounded shadow">
            <h3 className="text-xl font-bold mb-2">{item.name}</h3>
            {item.imageUrls && item.imageUrls.length > 0 && (
              <img
                src={`http://localhost:8082${item.imageUrls[0]}`}
                alt={item.name}
                className="w-full h-48 object-cover mb-2"
              />
            )}
            <p className="mb-2">{item.description}</p>
            {item.auction && (
              <p className="mb-2">Current Bid: ${item.auction.currentBidPrice?.toFixed(2)}</p>
            )}
            <a
              href={`/items/${item.id}`}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              View Details
            </a>
          </div>
        ))}
      </div>

      <h2 className="text-2xl font-semibold mb-4">Items I've Bidded On</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {biddedItems.map((item) => (
          <div key={item.id} className="border p-4 rounded shadow">
            <h3 className="text-xl font-bold mb-2">{item.name}</h3>
            {item.imageUrls && item.imageUrls.length > 0 && (
              <img
                src={`http://localhost:8082${item.imageUrls[0]}`}
                alt={item.name}
                className="w-full h-48 object-cover mb-2"
              />
            )}
            <p className="mb-2">{item.description}</p>
            {item.auction && (
              <p className="mb-2">Current Bid: ${item.auction.currentBidPrice?.toFixed(2)}</p>
            )}
            <a
              href={`/items/${item.id}`}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              View Details
            </a>
          </div>
        ))}
      </div>

    </div>
  );
};

export default Profile;
