// src/components/ItemsPage.js
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import CountdownTimer from './CountdownTimer'; // Import the CountdownTimer component

const ItemsPage = () => {
  const [items, setItems] = useState([]); // List of items
  const [loading, setLoading] = useState(true); // Loading state
  const [error, setError] = useState(''); // Error message
  const [currentPage, setCurrentPage] = useState(1); // Current page for pagination
  const [itemsPerPage] = useState(9); // Items per page for pagination
  const [searchTerm, setSearchTerm] = useState(''); // Search term

  useEffect(() => {
    const fetchItems = async () => {
      try {
        const response = await fetch('http://localhost:8082/api/items', {
          method: 'GET',
        });

        if (response.ok) {
          const data = await response.json();
          setItems(data);
          setError('');
        } else {
          const errorMsg = await response.text();
          setError(`Failed to fetch items: ${errorMsg}`);
          setItems([]);
        }
      } catch (err) {
        console.error('Error fetching items:', err);
        setError('An unexpected error occurred while fetching items.');
        setItems([]);
      } finally {
        setLoading(false);
      }
    };

    fetchItems();
  }, []);

  // Calculate current items for pagination
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = items.slice(indexOfFirstItem, indexOfLastItem);

  // Calculate total pages
  const totalPages = Math.ceil(items.length / itemsPerPage);

  // Filter items based on search term
  const filteredItems = currentItems.filter((item) =>
    item.name.toLowerCase().includes(searchTerm.toLowerCase())
    // Removed description from search to align with UI changes
  );

  // Render loading state with skeleton loaders
  if (loading) {
    return (
      <div className="container mx-auto px-4 py-6">
        <h1 className="text-3xl font-bold mb-4">All Items</h1>
        <div className="mb-4">
          <input
            type="text"
            placeholder="Search items..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-4 py-2 border rounded"
          />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {Array.from({ length: itemsPerPage }, (_, index) => (
            <div key={index} className="border p-4 rounded shadow animate-pulse">
              <div className="h-48 bg-gray-300 rounded mb-2"></div>
              <div className="h-6 bg-gray-300 rounded w-3/4 mb-2"></div>
              <div className="h-4 bg-gray-300 rounded mb-2"></div>
              <div className="h-4 bg-gray-300 rounded w-1/2"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Render error state
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
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">All Items</h1>
      
      {/* Search Input */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Search items..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-2 border rounded"
        />
      </div>

      {/* Items Grid */}
      {filteredItems.length === 0 ? (
        <p className="text-center text-gray-700">No items found.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {filteredItems.map((item) => (
            <div key={item.id} className="border p-4 rounded shadow hover:shadow-lg transition-shadow duration-300">
              <h2 className="text-xl font-semibold mb-2">{item.name}</h2>
              {item.imageUrls && item.imageUrls.length > 0 ? (
                <img
                  src={`http://localhost:8082${item.imageUrls[0]}`} // Ensure the image URL is correct
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
              {/* Removed description */}
              {item.auction && item.auction.auctionEndTime && (
                <CountdownTimer endTime={item.auction.auctionEndTime} />
              )}
              {item.auction && (
                <p className="mt-2">
                  <strong>Current Bid:</strong> ${item.auction.currentBidPrice?.toFixed(2) || 'N/A'}
                </p>
              )}
              <Link
                to={`/items/${item.id}`}
                className="inline-block mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                View Details
              </Link>
            </div>
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-6">
          {Array.from({ length: totalPages }, (_, index) => (
            <button
              key={index + 1}
              onClick={() => setCurrentPage(index + 1)}
              className={`mx-1 px-3 py-1 rounded ${
                currentPage === index + 1
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              {index + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default ItemsPage;
