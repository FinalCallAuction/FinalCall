import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { useNavigate, useParams, Link } from 'react-router-dom';
import CountdownTimer from './CountdownTimer';

const Profile = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const { userId } = useParams();
  const [profile, setProfile] = useState(null);
  const [items, setItems] = useState({ active: [], expired: [] });
  const [bidCounts, setBidCounts] = useState({});
  const [error, setError] = useState('');
  
  const isOwnProfile = user && userId === user.id.toString();

  useEffect(() => {
    if (!user && !userId) {
      navigate('/login');
      return;
    }
    
    fetchProfile();
    fetchUserItems();
  }, [user, userId]);

  const fetchProfile = async () => {
    try {
      if (!userId) return;

      const response = await authFetch(`http://localhost:8081/api/users/${userId}`, {
        method: 'GET',
      }, logout);

      if (response.ok) {
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

  const fetchUserItems = async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items`, {
        method: 'GET',
      }, logout);

      if (response.ok) {
        const allItems = await response.json();
        const userItems = allItems.filter(item => item.listedBy.toString() === userId);
        
        const now = new Date();
        
        const active = userItems.filter(item => 
          item.auction &&
          new Date(item.auction.auctionEndTime) > now && 
          item.auction.status !== 'ENDED'
        );
        
        const expired = userItems.filter(item => 
          item.auction && (
            new Date(item.auction.auctionEndTime) <= now || 
            item.auction.status === 'ENDED'
          )
        );

        setItems({ active, expired });

        // Fetch bid counts for each auction
        const bidCountPromises = userItems.map(item => 
          fetch(`http://localhost:8084/api/auctions/${item.auction.id}/bids`)
        );

        const bidCountResponses = await Promise.all(bidCountPromises);
        const bidCountData = await Promise.all(
          bidCountResponses.map(response => response.json())
        );

        const countMap = {};
        bidCountData.forEach((bids, index) => {
          countMap[userItems[index].id] = bids.length;
        });

        setBidCounts(countMap);
      } else {
        console.error('Error fetching user items:', await response.text());
      }
    } catch (err) {
      console.error('Error fetching user items:', err);
    }
  };

  if (error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <h1 className="text-3xl font-bold mb-4">Profile</h1>
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
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

  const renderItemSection = (sectionTitle, itemList, isActive = true) => (
    <>
      <h2 className="text-2xl font-semibold mb-4">{sectionTitle}</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        {itemList.length > 0 ? (
          itemList.map((item) => (
            <div 
              key={item.id} 
              className={`border p-4 rounded shadow ${!isActive ? 'opacity-75' : ''}`}
            >
              <h3 className="text-xl font-semibold mb-1">{item.name}</h3>
              {item.imageUrls && item.imageUrls.length > 0 ? (
                <img
                  src={`http://localhost:8082${item.imageUrls[0]}`}
                  alt={item.name}
                  className="w-full h-48 object-cover mb-2 rounded"
                  loading="lazy"
                />
              ) : (
                <img
                  src="https://placehold.co/600x400"
                  alt="Placeholder"
                  className="w-full h-48 object-cover mb-2 rounded"
                  loading="lazy"
                />
              )}

              {item.auction && item.auction.auctionEndTime && (
                <CountdownTimer endTime={item.auction.auctionEndTime} />
              )}

              {item.auction && (
                <div className="mt-2 space-y-1">
                  <p>
                    <strong>Starting Bid:</strong> ${item.auction.startingBidPrice?.toFixed(2) || 'N/A'}
                  </p>
                  <p>
                    <strong>Current Bid:</strong> ${item.auction.currentBidPrice?.toFixed(2) || 'N/A'}
                  </p>
                  <p>
                    <strong>Bids:</strong> {bidCounts[item.id] || 0}
                  </p>
                </div>
              )}

              <Link
                to={`/items/${item.id}`}
                className="inline-block mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                View Details
              </Link>
            </div>
          ))
        ) : (
          <p className="col-span-3">No {sectionTitle.toLowerCase()} found.</p>
        )}
      </div>
    </>
  );

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">
        {isOwnProfile ? 'My Profile' : `${profile.username}'s Profile`}
      </h1>

      <div className="bg-white p-6 rounded shadow-md mb-6">
        <div className="flex justify-between items-start">
          <div>
            <p><strong>Username:</strong> {profile.username}</p>
            <p><strong>Name:</strong> {profile.firstName} {profile.lastName}</p>
            {isOwnProfile && (
              <>
                <p><strong>Email:</strong> {profile.email}</p>
                <p><strong>Street Address:</strong> {profile.streetAddress}</p>
                <p><strong>Province/State:</strong> {profile.province}</p>
                <p><strong>Country:</strong> {profile.country}</p>
                <p><strong>Postal Code:</strong> {profile.postalCode}</p>
              </>
            )}
            <p><strong>Seller:</strong> {profile.isSeller ? 'Yes' : 'No'}</p>
          </div>

          {isOwnProfile && (
            <div className="space-y-2">
              <Link
                to="/change-address"
                className="block px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 text-center"
              >
                Change Address
              </Link>
              <Link
                to="/change-password"
                className="block px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 text-center"
              >
                Change Password
              </Link>
            </div>
          )}
        </div>
      </div>

      {/* Active Listings Section */}
      {renderItemSection('Active Listings', items.active, true)}

      {/* Expired Listings Section */}
      {renderItemSection('Expired Listings', items.expired, false)}
    </div>
  );
};

export default Profile;