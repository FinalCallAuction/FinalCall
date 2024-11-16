// src/components/ItemsPage.js

import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { formatDistance, parseISO } from 'date-fns';
import { authFetch } from '../utils/authFetch';
import { useNavigate } from 'react-router-dom';

const ItemsPage = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [auctionType, setAuctionType] = useState('ALL');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchItems();
    const timer = setInterval(() => {
      fetchItems();
    }, 60000); // Refresh every 60 seconds
    return () => clearInterval(timer);
  }, [auctionType]);

  const fetchItems = async () => {
    try {
      let url = 'http://localhost:8082/api/items/';
      if (auctionType !== 'ALL') {
        url = `http://localhost:8082/api/items/type/${auctionType}`;
      }

      const response = await authFetch(url, {
        method: 'GET',
      });

      if (response.ok) {
        const data = await response.json();
        setItems(data);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch items.');
      console.error('Fetch Items Error:', err);
    }
  };

  // Handler for accepting a bid by the lister
  const handleAcceptBid = async (itemId, bidAmount) => {
    if (!user) {
      alert('You must be logged in to accept a bid.');
      return;
    }

    try {
      const response = await authFetch(`http://localhost:8082/api/items/${itemId}/acceptBid`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          bidAmount: bidAmount,
        }),
      });

      if (response.ok) {
        alert('Bid accepted successfully');
        fetchItems(); // Refresh items
      } else {
        const errorMsg = await response.text();
        alert(`Bid acceptance failed: ${errorMsg}`);
      }
    } catch (err) {
      alert('An error occurred while accepting the bid.');
      console.error('Accept Bid Error:', err);
    }
  };

  // Handler for placing a bid by a bidder
  const handlePlaceBid = async (itemId, bidAmount) => {
    if (!user) {
      alert('You must be logged in to place a bid.');
      return;
    }

    try {
      const response = await authFetch(`http://localhost:8082/api/items/${itemId}/bid`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          bidAmount: bidAmount,
        }),
      });

      if (response.ok) {
        alert('Bid placed successfully');
        fetchItems(); // Refresh items
      } else {
        const errorMsg = await response.text();
        alert(`Bid failed: ${errorMsg}`);
      }
    } catch (err) {
      alert('An error occurred while placing the bid.');
      console.error('Place Bid Error:', err);
    }
  };

  const renderAuctionTypeFilter = () => {
    return (
      <div className="mb-4">
        <label className="mr-2 font-semibold">Filter by Auction Type:</label>
        <select
          value={auctionType}
          onChange={(e) => setAuctionType(e.target.value)}
          className="border rounded px-2 py-1"
        >
          <option value="ALL">All</option>
          <option value="DUTCH">Dutch</option>
          <option value="FORWARD">Forward</option>
        </select>
      </div>
    );
  };

  const calculateTimeLeft = (endTime) => {
    try {
      const now = new Date();
      const end = parseISO(endTime); // parseISO expects an ISO 8601 formatted date string

      if (isNaN(end.getTime())) {
        return 'Invalid date';
      }
      if (end < now) {
        return 'Auction Ended';
      }
      return formatDistance(end, now, { addSuffix: true });
    } catch (error) {
      console.error('Error calculating time left:', error);
      return 'Invalid date';
    }
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Auction Items</h1>
      {renderAuctionTypeFilter()}
      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      )}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {items.map((item) => (
          <div key={item.id} className="bg-white p-4 rounded shadow">
            {item.imageUrl ? (
              <img
                src={`http://localhost:8082${item.imageUrl}`}
                alt={item.name}
                className="w-full h-48 object-cover rounded mb-4"
                onError={(e) => {
                  console.error(`Failed to load image: http://localhost:8082${item.imageUrl}`);
                  e.target.onerror = null;
                  e.target.src = 'https://via.placeholder.com/150';
                }}
              />
            ) : (
              <img
                src="https://via.placeholder.com/150"
                alt="Placeholder"
                className="w-full h-48 object-cover rounded mb-4"
              />
            )}
            <h2 className="text-xl font-semibold mb-2">{item.name}</h2>
            <p>
              <strong>Auction Type:</strong> {item.auctionType}
            </p>
            <p>
              <strong>Listed By:</strong> {item.listedBy}
            </p>
            <p>
              <strong>Current Bid:</strong> ${item.currentBid.toFixed(2)}
            </p>
            <p>
              <strong>Time Left:</strong> {calculateTimeLeft(item.auctionEndTime)}
            </p>
            {user && new Date(item.auctionEndTime) > new Date() && (
              <div className="mt-4">
                {user.username === item.listedBy ? (
                  // Lister can accept bid only if currentBid > startingBid
                  item.currentBid > item.startingBid ? (
                    <button
                      onClick={() => {
                        const confirmBid = window.confirm(
                          `Accept current bid of $${item.currentBid.toFixed(2)}?`
                        );
                        if (confirmBid) {
                          handleAcceptBid(item.id, item.currentBid);
                        }
                      }}
                      className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                    >
                      Accept Current Bid (${item.currentBid.toFixed(2)})
                    </button>
                  ) : (
                    <p className="text-gray-500">No bids placed yet.</p>
                  )
                ) : (
                  // Bidders can place bids
                  <>
                    {item.auctionType === 'FORWARD' ? (
                      <button
                        onClick={() => {
                          const bidAmountStr = prompt('Enter your bid amount:');
                          const bidAmount = parseFloat(bidAmountStr);
                          if (isNaN(bidAmount) || bidAmount <= item.currentBid) {
                            alert('Invalid bid amount. Bid must be greater than current bid.');
                          } else {
                            handlePlaceBid(item.id, bidAmount);
                          }
                        }}
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                      >
                        Place Bid
                      </button>
                    ) : item.auctionType === 'DUTCH' ? (
                      <button
                        onClick={() => {
                          const bidAmountStr = prompt('Enter your bid amount:');
                          const bidAmount = parseFloat(bidAmountStr);
                          if (isNaN(bidAmount) || bidAmount <= item.currentBid) {
                            alert('Invalid bid amount. Bid must be greater than current bid.');
                          } else {
                            handlePlaceBid(item.id, bidAmount);
                          }
                        }}
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                      >
                        Place Bid
                      </button>
                    ) : null}
                  </>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default ItemsPage;
