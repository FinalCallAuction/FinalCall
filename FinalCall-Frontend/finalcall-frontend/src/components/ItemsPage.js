// src/components/ItemsPage.js

import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow } from 'date-fns'; // Ensure this import is present

const ItemsPage = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [sellerNames, setSellerNames] = useState({}); // To store seller names keyed by user ID
  const [error, setError] = useState('');

  // Function to fetch seller names based on user IDs
  const fetchSellerNames = async (userIds) => {
    const uniqueUserIds = [...new Set(userIds)]; // Remove duplicates
    const newSellerNames = { ...sellerNames };

    try {
      // Corrected endpoint: /api/user/{id}
      await Promise.all(
        uniqueUserIds.map(async (id) => {
          if (!newSellerNames[id]) { // Fetch only if not already fetched
            const response = await authFetch(`http://localhost:8081/api/user/${id}`, {
              method: 'GET',
            }, logout); // Pass logout to handle unauthorized responses

            if (response.ok) {
              const userData = await response.json();
              newSellerNames[id] = userData.username; // Corrected to 'username'
            } else {
              newSellerNames[id] = 'Unknown'; // Fallback if user not found
              console.error(`Failed to fetch user with ID: ${id}`);
            }
          }
        })
      );

      setSellerNames(newSellerNames);
    } catch (err) {
      console.error('Error fetching seller names:', err);
      // Optionally, set error state or handle accordingly
    }
  };

  const fetchItems = async () => {
    try {
      // Fetch items from Catalogue Service
      const itemsResponse = await authFetch('http://localhost:8082/api/items', {
        method: 'GET',
      }, logout); // Pass logout to handle unauthorized responses

      if (!itemsResponse.ok) {
        const errorMsg = await itemsResponse.text();
        setError(`Error fetching items: ${errorMsg}`);
        return;
      }

      const itemsData = await itemsResponse.json();
      setItems(itemsData);
      console.log('Fetched Items:', itemsData); // Debugging

      // Extract seller IDs to fetch their names
      const sellerIds = itemsData.map(item => item.listedBy);
      await fetchSellerNames(sellerIds);

    } catch (err) {
      setError('Failed to fetch items.');
      console.error('Fetch Items Error:', err);
    }
  };

  useEffect(() => {
    fetchItems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Process items to extract auction data and map seller names
  const mergedItems = items.map(item => {
    const auction = item.auction;
    return {
      ...item,
      currentBidPrice: auction && auction.currentBidPrice != null ? auction.currentBidPrice : item.startingBidPrice, // Use startingBidPrice if currentBidPrice is null
      auctionEndTime: auction ? auction.auctionEndTime : 'N/A',
      sellerName: sellerNames[item.listedBy] || 'Loading...',
    };
  });

  console.log('Merged Items:', mergedItems); // Debugging

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Browse Items</h1>
      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          {error}
        </div>
      )}
      {mergedItems.length === 0 ? (
        <p>No items available.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {mergedItems.map((item) => (
            <div key={item.id} className="bg-white p-4 rounded shadow">
              {/* Use the imageUrls array to get the first image */}
              {item.imageUrls && item.imageUrls.length > 0 ? (
                <img
                  src={`http://localhost:8082${item.imageUrls[0]}`}
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
              <h2
                className="text-xl font-semibold mb-2 cursor-pointer"
                onClick={() => navigate(`/items/${item.id}`)}
              >
                {item.name}
              </h2>
              {/* Display the seller's name */}
              <p>
                <strong>Listed By:</strong> {item.sellerName}
              </p>
              {/* Auction-specific details */}
              <p>
                <strong>Current Bid:</strong> $
                {typeof item.currentBidPrice === 'number' ? item.currentBidPrice.toFixed(2) : 'N/A'}
              </p>
              <p>
                <strong>Time Left:</strong>{' '}
                {item.auctionEndTime !== 'N/A' ? (
                  (() => {
                    const now = new Date();
                    const auctionEnd = new Date(item.auctionEndTime);
                    if (!isNaN(auctionEnd)) {
                      if (auctionEnd > now) {
                        return `${formatDistanceToNow(auctionEnd, { addSuffix: true })}`;
                      } else {
                        return 'Ended';
                      }
                    } else {
                      return 'Invalid Date';
                    }
                  })()
                ) : (
                  'N/A'
                )}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ItemsPage;
