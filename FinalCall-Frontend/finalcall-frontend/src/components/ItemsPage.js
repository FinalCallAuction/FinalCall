// src/components/ItemsPage.js
import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow } from 'date-fns';

const ItemsPage = () => {
  const { logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [sellerNames, setSellerNames] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true); // Add loading state

  const fetchSellerNames = async (userIds) => {
    const uniqueUserIds = [...new Set(userIds)];
    const newSellerNames = { ...sellerNames };

    try {
      await Promise.all(
        uniqueUserIds.map(async (id) => {
          if (!newSellerNames[id]) {
            const response = await authFetch(`http://localhost:8081/api/user/${id}`, {
              method: 'GET',
            }, logout);

            if (response && response.ok) {
              const userData = await response.json();
              newSellerNames[id] = userData.username || userData.email;
            } else {
              newSellerNames[id] = 'Unknown';
              console.error(`Failed to fetch user with ID: ${id}`);
            }
          }
        })
      );

      setSellerNames(newSellerNames);
    } catch (err) {
      console.error('Error fetching seller names:', err);
    }
  };

  const fetchItems = async () => {
    setLoading(true);
    try {
      // Use the absolute URL to the catalogue/auction server's items endpoint
      const itemsResponse = await fetch('http://localhost:8082/api/items', { // Replace with actual URL
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!itemsResponse.ok) {
        const errorMsg = await itemsResponse.text();
        setError(`Error fetching items: ${errorMsg}`);
        setLoading(false);
        return;
      }

      const itemsData = await itemsResponse.json();
      setItems(itemsData);
      console.log('Fetched Items:', itemsData);

      const sellerIds = itemsData.map(item => item.listedBy);
      await fetchSellerNames(sellerIds);
    } catch (err) {
      setError('Failed to fetch items.');
      console.error('Fetch Items Error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const mergedItems = items.map(item => {
    const auction = item.auction;
    return {
      ...item,
      currentBidPrice: auction && auction.currentBidPrice != null ? auction.currentBidPrice : item.startingBidPrice,
      auctionEndTime: auction ? auction.auctionEndTime : 'N/A',
      sellerName: sellerNames[item.listedBy] || 'Loading...',
    };
  });

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-6">
        <h1 className="text-3xl font-bold mb-4">Browse Items</h1>
        <p>Loading items...</p>
      </div>
    );
  }

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
              {item.imageUrls && item.imageUrls.length > 0 ? (
                <img
                  src={`${item.imageUrls[0]}`} // Adjust based on actual image URL structure
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
                <strong>Listed By:</strong> {item.sellerName}
              </p>
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
