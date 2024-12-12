// src/components/ItemsPage.js
import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

const ItemsPage = () => {
  const [items, setItems] = useState([]);
  const { user, logout } = useContext(AuthContext);

  useEffect(() => {
    const fetchItems = async () => {
      try {
        // No need for auth for public items, but if needed:
        const response = await fetch('http://localhost:8082/api/items');
        if (response.ok) {
          const data = await response.json();
          setItems(data);
        } else {
          const errorMsg = await response.text();
          console.error('Error fetching items:', errorMsg);
        }
      } catch (err) {
        console.error('Error:', err);
      }
    };

    fetchItems();
  }, []);

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">All Items</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {items.map((item) => (
          <div key={item.id} className="border p-4 rounded shadow">
            <h2 className="text-xl font-bold mb-2">{item.name}</h2>
            {item.imageUrls && item.imageUrls.length > 0 ? (
              <img
                src={`http://localhost:8082${item.imageUrls[0]}`}
                alt={item.name}
                className="w-full h-48 object-cover mb-2"
              />
            ) : (
              <img
                src="https://via.placeholder.com/150"
                alt="Placeholder"
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

export default ItemsPage;
