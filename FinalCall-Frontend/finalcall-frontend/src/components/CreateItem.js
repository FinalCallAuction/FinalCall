import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { formatDistance } from 'date-fns';

const CreateItem = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [itemDetails, setItemDetails] = useState({
    name: '',
    startingBid: '',
    auctionType: 'FORWARD', // Default value
    auctionEndTime: '',
    description: '', // Description field 
    // Dutch auction specific fields
    priceDecrement: '',
    minimumPrice: ''
  });
  const [error, setError] = useState('');
  const [timeDifference, setTimeDifference] = useState('');
  const [imageFiles, setImageFiles] = useState([]); // Support multiple images

  // If not logged in, redirect to login
  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    } else {
      // Set the default auction end time to 24 hours in the future
      const futureDate = new Date();
      futureDate.setHours(futureDate.getHours() + 24);
      // Format date to yyyy-MM-ddTHH:mm for datetime-local
      const year = futureDate.getFullYear();
      const month = String(futureDate.getMonth() + 1).padStart(2, '0');
      const day = String(futureDate.getDate()).padStart(2, '0');
      const hours = String(futureDate.getHours()).padStart(2, '0');
      const minutes = String(futureDate.getMinutes()).padStart(2, '0');
      const defaultAuctionEndTime = `${year}-${month}-${day}T${hours}:${minutes}`;

      setItemDetails((prevDetails) => ({
        ...prevDetails,
        auctionEndTime: defaultAuctionEndTime,
      }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, navigate]);

  // Update the time difference whenever auctionEndTime changes
  useEffect(() => {
    if (itemDetails.auctionEndTime) {
      try {
        const now = new Date();
        const auctionEndDate = new Date(itemDetails.auctionEndTime);
        if (isNaN(auctionEndDate.getTime())) {
          setTimeDifference('Invalid date');
        } else if (auctionEndDate > now) {
          const diff = formatDistance(auctionEndDate, now, { addSuffix: true });
          setTimeDifference(diff + ' from now');
        } else {
          setTimeDifference('Auction Ended');
        }
      } catch (error) {
        setTimeDifference('Invalid date');
      }
    }
  }, [itemDetails.auctionEndTime]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setItemDetails({
      ...itemDetails,
      [name]: value,
    });
  };

  const handleImageChange = (e) => {
    if (e.target.files) {
      setImageFiles(Array.from(e.target.files));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const { 
      name, 
      startingBid, 
      auctionType, 
      auctionEndTime, 
      description,
      priceDecrement,
      minimumPrice
    } = itemDetails;

    // Basic validation
    if (!name || !startingBid || !auctionEndTime) {
      setError('Please fill in all required fields.');
      return;
    }

    // Additional validation for Dutch auctions
    if (auctionType === 'DUTCH') {
      if (!priceDecrement || !minimumPrice) {
        setError('Price Decrement and Minimum Price are required for Dutch auctions.');
        return;
      }
      
      if (parseFloat(priceDecrement) <= 0 || parseFloat(minimumPrice) <= 0) {
        setError('Price Decrement and Minimum Price must be positive values.');
        return;
      }
      
      if (parseFloat(minimumPrice) >= parseFloat(startingBid)) {
        setError('Minimum Price must be less than Starting Bid Price for Dutch auctions.');
        return;
      }
    }

    const auctionEnd = new Date(auctionEndTime);
    if (isNaN(auctionEnd.getTime())) {
      setError('Invalid auction end time.');
      return;
    }
    if (auctionEnd <= new Date()) {
      setError('Auction end time must be in the future.');
      return;
    }

    try {
      // Create item without images
      const itemData = {
        name,
        startingBid: parseFloat(startingBid),
        auctionType,
        auctionEndTime,
        description, // Include description if applicable
        // Add Dutch auction specific fields for the request
        ...(auctionType === 'DUTCH' && {
          priceDecrement: parseFloat(priceDecrement),
          minimumPrice: parseFloat(minimumPrice)
        })
      };

      const response = await authFetch('http://localhost:8082/api/items/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(itemData),
      }, logout);

      if (!response.ok) {
        const errorMsg = await response.text();
        setError(errorMsg);
        return;
      }

      const savedItem = await response.json();
      const itemId = savedItem.id;

      // If images are selected, upload images
      if (imageFiles.length > 0) {
        const formData = new FormData();
        imageFiles.forEach((file) => {
          formData.append('images', file);
        });

        const uploadResponse = await authFetch(`http://localhost:8082/api/items/${itemId}/upload-image`, {
          method: 'POST',
          body: formData,
        }, logout);

        if (!uploadResponse.ok) {
          const errorMsg = await uploadResponse.text();
          setError(`Item created, but failed to upload images: ${errorMsg}`);
          return;
        }
      }

      alert('Item listed successfully!');
      navigate('/items');
    } catch (err) {
      setError('An error occurred while listing the item.');
      console.error('Create Item Error:', err);
    }
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">List a New Item</h1>
      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      )}
      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded shadow-md"
        encType="multipart/form-data"
      >
        {/* Existing fields */}
        <div className="mb-4">
          <label className="block text-gray-700" htmlFor="name">
            Item Name
          </label>
          <input
            type="text"
            name="name"
            id="name"
            value={itemDetails.name}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border rounded mt-1"
            placeholder="Enter a descriptive item name"
          />
        </div>
		<div className="mb-6">
		          <label className="block text-gray-700" htmlFor="description">
		            Description:
		          </label>
		          <textarea
		            name="description"
		            id="description"
		            value={itemDetails.description}
		            onChange={handleChange}
		            className="w-full px-3 py-2 border rounded mt-1"
		            placeholder="Enter a detailed description of the item"
		            rows="4"
		          ></textarea>
		        </div>

		        <div className="mb-4">
		          <label className="block text-gray-700" htmlFor="startingBid">
		            Starting Bid ($)
		          </label>
		          <input
		            type="number"
		            name="startingBid"
		            id="startingBid"
		            value={itemDetails.startingBid}
		            onChange={handleChange}
		            required
		            className="w-full px-3 py-2 border rounded mt-1"
		            placeholder="Enter the starting bid"
		            min="0"
		            step="0.01"
		          />
		        </div>

        <div className="mb-4">
          <label className="block text-gray-700" htmlFor="auctionType">
            Auction Type
          </label>
          <select
            name="auctionType"
            id="auctionType"
            value={itemDetails.auctionType}
            onChange={handleChange}
            className="w-full px-3 py-2 border rounded mt-1"
          >
            <option value="FORWARD">Forward Auction</option>
            <option value="DUTCH">Dutch Auction</option>
          </select>
        </div>

        {/* Dutch Auction Specific Fields - Conditionally Rendered */}
        {itemDetails.auctionType === 'DUTCH' && (
          <>
            <div className="mb-4">
              <label className="block text-gray-700" htmlFor="priceDecrement">
                Price Decrement ($)
              </label>
              <input
                type="number"
                name="priceDecrement"
                id="priceDecrement"
                value={itemDetails.priceDecrement}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border rounded mt-1"
                placeholder="Enter price decrement per interval"
                min="0.01"
                step="0.01"
              />
              <p className="text-sm text-gray-600 mt-1">
                Amount the price will decrease at each interval
              </p>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700" htmlFor="minimumPrice">
                Minimum Price ($)
              </label>
              <input
                type="number"
                name="minimumPrice"
                id="minimumPrice"
                value={itemDetails.minimumPrice}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border rounded mt-1"
                placeholder="Enter the lowest acceptable price"
                min="0"
                step="0.01"
              />
              <p className="text-sm text-gray-600 mt-1">
                The lowest price the item can be sold at
              </p>
            </div>
          </>
        )}

        {/* Rest of the existing form */}
        <div className="mb-6">
          <label className="block text-gray-700" htmlFor="auctionEndTime">
            Auction End Time{' '}
            {timeDifference && (
              <span className="text-gray-600 text-sm">({timeDifference})</span>
            )}
          </label>
          <input
            type="datetime-local"
            name="auctionEndTime"
            id="auctionEndTime"
            value={itemDetails.auctionEndTime}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border rounded mt-1"
            placeholder="Select auction end time"
            min={
              (() => {
                const now = new Date();
                now.setHours(now.getHours() + 1); // At least 1 hour from now
                const year = now.getFullYear();
                const month = String(now.getMonth() + 1).padStart(2, '0');
                const day = String(now.getDate()).padStart(2, '0');
                const hours = String(now.getHours()).padStart(2, '0');
                const minutes = String(now.getMinutes()).padStart(2, '0');
                return `${year}-${month}-${day}T${hours}:${minutes}`;
              })()
            } // Sets the minimum selectable time to 1 hour from now
          />
        </div>

        {/* Rest of the form */}
        <button
          type="submit"
          className="w-full bg-yellow-500 text-white py-2 rounded hover:bg-yellow-600"
        >
          List Item
        </button>
      </form>
    </div>
  );
};

export default CreateItem;