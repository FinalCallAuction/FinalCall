// src/components/ItemsPage.js

import React, { useEffect, useState, useCallback, useContext } from "react";
import { Link } from 'react-router-dom';
import CountdownTimer from './CountdownTimer';

const ItemsPage = () => {
  const [items, setItems] = useState([]);
  const [bidCounts, setBidCounts] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");
  const itemsPerPage = 9;

  const fetchItems = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fetch("http://localhost:8082/api/items", {
        method: "GET",
        headers: {
          'Accept': 'application/json'
        }
      });

      console.log('Fetch Items Response:', response); // Added detailed logging

      if (response.ok) {
        const data = await response.json();
        console.log('Fetched Items Data:', data); // Log fetched data
        
        setItems(data);
        setError("");

        // Fetch bid counts for each auction
        const bidCountPromises = data.map((item) => {
          if (item.auction && item.auction.id) {
            return fetch(
              `http://localhost:8084/api/auctions/${item.auction.id}/bids`,
              {
                method: "GET",
              }
            )
              .then((res) => (res.ok ? res.json() : []))
              .then((bids) => ({ itemId: item.id, count: bids.length }))
              .catch(() => ({ itemId: item.id, count: 0 }));
          }
          return Promise.resolve({ itemId: item.id, count: 0 });
        });

        const bidCountResults = await Promise.all(bidCountPromises);
        const countMap = {};
        bidCountResults.forEach((result) => {
          countMap[result.itemId] = result.count;
        });
        setBidCounts(countMap);
      } else {
        // More detailed error handling
        const errorText = await response.text();
        console.error('Fetch Items Error Response:', errorText);
        setError(`Failed to fetch items: ${errorText}`);
        setItems([]);
      }
    } catch (err) {
      console.error("Detailed Error Fetching Items:", err);
      setError(err.message || "An unexpected error occurred while fetching items.");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  useEffect(() => {
      const ws = new WebSocket("ws://localhost:8084/ws/items");

      ws.onopen = () => {
        console.log("Connected to items WebSocket");
      };

      ws.onmessage = (event) => {
		console.log("Received WebSocket message:", event.data);
        try {
          const data = JSON.parse(event.data);

          if (data.type === "NEW_AUCTION") {
            // data.data is the auctionDTO
            const auctionDTO = data.data;
            const newItem = auctionDTO.item; 
            // Attach the auction info to the item object
            newItem.auction = auctionDTO;

            setItems((prevItems) => [newItem, ...prevItems]);
            setBidCounts((prevCounts) => ({
              ...prevCounts,
              [newItem.id]: 0,
            }));
          } else if (data.type === "AUCTION_UPDATE") {
            // ... (existing code)
          }
        } catch (error) {
          console.error("Error handling WebSocket message:", error);
        }
      };


    ws.onclose = () => {
      console.log("Items WebSocket connection closed");
    };

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, [setItems, setBidCounts, fetchItems]);
  
  useEffect(() => {
    const refreshHandler = () => {
      fetchItems(); // Re-fetch items from catalogue-service
    };

    window.addEventListener('refreshItems', refreshHandler);
    return () => window.removeEventListener('refreshItems', refreshHandler);
  }, [fetchItems]);


  // Filter items based on search term
  const filteredItems = items.filter((item) =>
    item.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Calculate pagination
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredItems.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredItems.length / itemsPerPage);
  
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
            onChange={e => setSearchTerm(e.target.value)}
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
          onChange={e => setSearchTerm(e.target.value)}
          className="w-full px-4 py-2 border rounded"
        />
      </div>

      {/* Items Grid */}
      {filteredItems.length === 0 ? (
        <p className="text-center text-gray-700">No items found.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {filteredItems.map(item => (
            <div
              key={item.id}
              className="border p-4 rounded shadow hover:shadow-lg transition-shadow duration-300"
            >
              <h2 className="text-xl font-semibold mb-1">{item.name}</h2>

              {/* Listed By Positioning */}
              <div className="flex justify-between items-center mb-2">
                <p className="text-sm text-gray-600">
                  Listed by:{' '}
                  <Link
                    to={`/profile/${item.listedBy}`}
                    className="text-blue-500 hover:underline"
                  >
                    {item.listedByName || 'Unknown'}
                  </Link>
                </p>
              </div>

              {/* Image Container with Auction Type Badge */}
              <div className="relative">
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
                {/* Auction Type Badge */}
                {item.auction && (
                  <span
                    className={`absolute top-2 right-2 px-2 py-1 text-xs font-semibold rounded ${
                      item.auction.auctionType === 'DUTCH'
                        ? 'bg-yellow-200 text-yellow-800'
                        : 'bg-blue-200 text-blue-800'
                    }`}
                  >
                    {item.auction.auctionType === 'DUTCH'
                      ? 'Dutch Auction'
                      : 'Forward Auction'}
                  </span>
                )}
              </div>

              {/* Countdown Timer for Forward Auctions */}
              {item.auction && item.auction.auctionEndTime && (
                <CountdownTimer endTime={item.auction.auctionEndTime} />
              )}

              {/* Auction Details */}
              {item.auction && (
                <div className="mt-2 space-y-1">
                  <p className="text-sm text-gray-500">
                    <strong>Starting Bid:</strong>{' '}
                    <span className="text-gray-700">
                      $
                      {item.auction.startingBidPrice
                        ? item.auction.startingBidPrice.toFixed(2)
                        : 'N/A'}
                    </span>
                  </p>
                  <p className="text-base font-semibold">
                    <strong>Current Bid:</strong>{' '}
                    $
                    {item.auction.currentBidPrice
                      ? item.auction.currentBidPrice.toFixed(2)
                      : 'N/A'}
                  </p>
                  <p className="text-sm text-gray-500">
                    <strong>Bids:</strong> {bidCounts[item.id] || 0}
                  </p>
                </div>
              )}

              {/* View Details Button */}
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
