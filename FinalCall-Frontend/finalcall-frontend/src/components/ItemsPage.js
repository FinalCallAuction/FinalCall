// src/components/ItemsPage.js

import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';

const ItemsPage = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate(); // If not used, remove
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');

  const fetchItems = async () => {
    try {
      const response = await authFetch('http://localhost:8082/api/items', {
        method: 'GET',
      });
      if (response.ok) {
        const data = await response.json();
        setItems(data);
      } else {
        const errorMsg = await response.text();
        setError(`Error: ${errorMsg}`);
      }
    } catch (err) {
      setError('Failed to fetch items.');
      console.error('Fetch Items Error:', err);
    }
  };

  useEffect(() => {
    fetchItems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Remove unused functions if not implemented
  // If handleAcceptBid and handlePlaceBid are implemented, keep them; otherwise, remove

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
      {items.length === 0 ? (
        <p>No items available.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {items.map((item) => (
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
              <h2
                className="text-xl font-semibold mb-2 cursor-pointer"
                onClick={() => navigate(`/items/${item.id}`)}
              >
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
              {/* Uncomment and implement bidding functions if necessary */}
              {/* <button
                onClick={() => handlePlaceBid(item.id)}
                className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                Place Bid
              </button>
              <button
                onClick={() => handleAcceptBid(item.id)}
                className="mt-4 ml-2 px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
              >
                Accept Bid
              </button> */}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ItemsPage;
