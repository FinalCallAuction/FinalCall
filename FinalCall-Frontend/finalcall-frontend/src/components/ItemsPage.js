// src/components/ItemsPage.js

import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow } from 'date-fns';

const ItemsPage = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [auctions, setAuctions] = useState([]);
  const [error, setError] = useState('');

  const fetchItemsAndAuctions = async () => {
    try {
      // Fetch items from Catalogue Service
      const itemsResponse = await authFetch('http://localhost:8082/api/items', {
        method: 'GET',
      });

      if (!itemsResponse.ok) {
        const errorMsg = await itemsResponse.text();
        setError(`Error fetching items: ${errorMsg}`);
        return;
      }

      const itemsData = await itemsResponse.json();
      setItems(itemsData);
      console.log('Fetched Items:', itemsData); // Debugging

      // Extract item IDs
      const itemIds = itemsData.map(item => item.id);

      if (itemIds.length === 0) {
        // No items to fetch auctions for
        return;
      }

      // Fetch auctions from Auction Service
      const auctionsResponse = await authFetch(`http://localhost:8083/api/auctions/by-item-ids?itemIds=${itemIds.join(',')}`, {
        method: 'GET',
      });

      if (!auctionsResponse.ok) {
        const errorMsg = await auctionsResponse.text();
        setError(`Error fetching auctions: ${errorMsg}`);
        return;
      }

      const auctionsData = await auctionsResponse.json();
      setAuctions(auctionsData);
      console.log('Fetched Auctions:', auctionsData); // Debugging

    } catch (err) {
      setError('Failed to fetch items or auctions.');
      console.error('Fetch Items/Auctions Error:', err);
    }
  };

  useEffect(() => {
    fetchItemsAndAuctions();
  }, []);

  // Merge items and auctions
  const mergedItems = items.map(item => {
    // Ensure that 'catalogueItemId' and 'id' are both numbers
    const auction = auctions.find(a => Number(a.catalogueItemId) === Number(item.id));
    console.log(`Merging Item ID: ${item.id} with Auction: ${auction ? JSON.stringify(auction) : 'N/A'}`); // Debugging
    return {
      ...item,
      auctionType: auction ? auction.auctionType : 'N/A',
      startingBidPrice: auction ? auction.startingBidPrice : 'N/A',
      currentBidPrice: auction ? auction.currentBidPrice : 'N/A',
      auctionEndTime: auction ? auction.auctionEndTime : 'N/A',
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
              {/* Auction-specific details */}
              <p>
                <strong>Auction Type:</strong> {item.auctionType}
              </p>
              <p>
                <strong>Starting Bid:</strong> ${item.startingBidPrice !== 'N/A' ? item.startingBidPrice.toFixed(2) : 'N/A'}
              </p>
              <p>
                <strong>Current Bid:</strong> ${item.currentBidPrice !== 'N/A' ? item.currentBidPrice.toFixed(2) : 'N/A'}
              </p>
              <p>
                <strong>Time Left:</strong>{' '}
                {item.auctionEndTime !== 'N/A' ? (
                  (() => {
                    const now = new Date();
                    const auctionEnd = new Date(item.auctionEndTime);
                    if (!isNaN(auctionEnd)) { // Check if the date is valid
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
