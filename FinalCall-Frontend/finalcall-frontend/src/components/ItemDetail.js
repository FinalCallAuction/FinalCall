// src/components/ItemDetail.js

import React, { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow, parseISO } from 'date-fns';
import { AuthContext } from '../context/AuthContext';

const ItemDetail = () => {
  const { id } = useParams(); // Get the item ID from the route
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [item, setItem] = useState(null);
  const [biddingHistory, setBiddingHistory] = useState([]);
  const [timeLeft, setTimeLeft] = useState('');
  const [error, setError] = useState('');
  const [currentBidder, setCurrentBidder] = useState(null);

  useEffect(() => {
    fetchItemDetails();
    fetchBiddingHistory();
    const timer = setInterval(() => {
      updateTimeLeft();
    }, 1000); // Update every second
    return () => clearInterval(timer);
  }, [id, item]);

  const fetchItemDetails = async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}`, {
        method: 'GET',
      });

      if (response.ok) {
        const data = await response.json();
        setItem(data);
        setCurrentBidder(data.currentBidder);
        updateTimeLeft(data.auctionEndTime);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch item details.');
      console.error('Fetch Item Details Error:', err);
    }
  };

  const fetchBiddingHistory = async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}/bids`, {
        method: 'GET',
      });

      if (response.ok) {
        const data = await response.json();
        setBiddingHistory(data);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch bidding history.');
      console.error('Fetch Bidding History Error:', err);
    }
  };

  const updateTimeLeft = (endTimeParam) => {
    const endTime = endTimeParam || item?.auctionEndTime;
    if (!endTime) {
      setTimeLeft('No End Time');
      return;
    }

    const now = new Date();
    const auctionEndDate = parseISO(endTime);
    const difference = auctionEndDate - now;

    if (difference <= 0) {
      setTimeLeft('Auction Ended');
    } else {
      const days = Math.floor(difference / (1000 * 60 * 60 * 24));
      const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
      const minutes = Math.floor((difference / 1000 / 60) % 60);
      const seconds = Math.floor((difference / 1000) % 60);

      setTimeLeft(
        `${days}d ${hours}h ${minutes}m ${seconds}s`
      );
    }
  };

  if (error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
        <button
          onClick={() => navigate('/items')}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Back to Items
        </button>
      </div>
    );
  }

  if (!item) {
    return (
      <div className="container mx-auto px-4 py-6">
        <p>Loading item details...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <button
        onClick={() => navigate('/items')}
        className="mb-4 px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400"
      >
        &larr; Back to Items
      </button>
      <div className="flex flex-col md:flex-row">
        {/* Image Section */}
        <div className="md:w-1/2">
          {item.imageUrls && item.imageUrls.length > 0 ? (
            <div className="flex flex-wrap">
              {item.imageUrls.map((url, index) => (
                <img
                  key={index}
                  src={`http://localhost:8082${url}`}
                  alt={`${item.name} ${index + 1}`}
                  className="w-full md:w-1/2 h-64 object-cover rounded mb-4"
                  onError={(e) => {
                    console.error(`Failed to load image: http://localhost:8082${url}`);
                    e.target.onerror = null;
                    e.target.src = 'https://via.placeholder.com/300';
                  }}
                />
              ))}
            </div>
          ) : (
            <img
              src="https://via.placeholder.com/600x400"
              alt="Placeholder"
              className="w-full h-96 object-cover rounded"
            />
          )}
        </div>

        {/* Details Section */}
        <div className="md:w-1/2 md:pl-8">
          <h2 className="text-3xl font-bold mb-4">{item.name}</h2>
          <p className="mb-2">
            <strong>Description:</strong> {item.description}
          </p>
          <p className="mb-2">
            <strong>Auction Type:</strong> {item.auctionType}
          </p>
          <p className="mb-2">
            <strong>Starting Bid:</strong> ${item.startingBid.toFixed(2)}
          </p>
          <p className="mb-2">
            <strong>Current Bid:</strong> ${item.currentBid.toFixed(2)}
          </p>
          <p className="mb-2">
            <strong>Current Bidder:</strong> {currentBidder || 'No bids yet'}
          </p>
          <p className="mb-2">
            <strong>Listed By:</strong> {item.listedBy}
          </p>
          <p className="mb-2">
            <strong>Time Left:</strong> {timeLeft}
          </p>

          {/* Optional: Button to place a bid */}
          {/* {user.username !== item.listedBy && timeLeft !== 'Auction Ended' && (
            <button
              onClick={handlePlaceBid}
              className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Place a Bid
            </button>
          )} */}
        </div>
      </div>

      {/* Bidding History Section */}
      <div className="mt-8">
        <h3 className="text-2xl font-semibold mb-4">Bidding History</h3>
        {biddingHistory.length > 0 ? (
          <table className="min-w-full bg-white border">
            <thead>
              <tr>
                <th className="py-2 px-4 border">Bidder</th>
                <th className="py-2 px-4 border">Bid Amount ($)</th>
                <th className="py-2 px-4 border">Time</th>
              </tr>
            </thead>
            <tbody>
              {biddingHistory.map((bid, index) => (
                <tr key={index} className="text-center">
                  <td className="py-2 px-4 border">{bid.bidder}</td>
                  <td className="py-2 px-4 border">{bid.amount.toFixed(2)}</td>
                  <td className="py-2 px-4 border">
                    {formatDistanceToNow(parseISO(bid.timestamp), { addSuffix: true })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No bids have been placed yet.</p>
        )}
      </div>
    </div>
  );
};

export default ItemDetail;
