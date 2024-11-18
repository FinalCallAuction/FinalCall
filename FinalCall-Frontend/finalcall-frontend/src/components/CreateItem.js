// src/components/CreateItem.js

import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { formatDistance } from 'date-fns';

const CreateItem = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [itemDetails, setItemDetails] = useState({
    name: '',
    startingBid: '',
    auctionType: 'FORWARD', // Default value
    auctionEndTime: '',
  });
  const [error, setError] = useState('');
  const [timeDifference, setTimeDifference] = useState('');
  const [image, setImage] = useState(null);

  // If not logged in, redirect to login
  useEffect(() => {
    if (!user) {
      navigate('/login');
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
    setItemDetails((prevDetails) => ({
      ...prevDetails,
      [name]: value,
    }));
  };

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setImage(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const { name, startingBid, auctionType, auctionEndTime } = itemDetails;

    // Basic validation
    if (!name || !startingBid || !auctionEndTime) {
      setError('Please fill in all required fields.');
      return;
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
      // Use FormData to handle file upload
      const formData = new FormData();
      formData.append('name', name);
      formData.append('startingBid', parseFloat(startingBid));
      formData.append('auctionType', auctionType);
      formData.append('auctionEndTime', auctionEndTime);
      if (image) {
        formData.append('image', image);
      }

      const response = await authFetch('http://localhost:8082/api/items/create', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        alert('Item listed successfully!');
        navigate('/items');
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
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
        <div className="mb-6">
          <label className="block text-gray-700" htmlFor="image">
            Upload Image
          </label>
          <input
            type="file"
            name="image"
            id="image"
            accept="image/*"
            onChange={handleImageChange}
            className="w-full px-3 py-2 border rounded mt-1"
          />
          {image && (
            <p className="text-sm text-gray-600 mt-2">{image.name} selected</p>
          )}
        </div>
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
