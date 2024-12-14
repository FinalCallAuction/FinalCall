import React, { useEffect, useState, useContext, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow, parseISO } from 'date-fns';
import { AuthContext } from '../context/AuthContext';
import { Carousel } from 'react-responsive-carousel';
import { Link } from 'react-router-dom';
import 'react-responsive-carousel/lib/styles/carousel.min.css';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';

const BiddingSection = ({ auction, user, timeLeft, onBidSubmit }) => {
  const [bidAmount, setBidAmount] = useState('');
  const [bidError, setBidError] = useState('');
  const [bidSuccess, setBidSuccess] = useState('');

  const handleBid = async (amount) => {
    setBidError('');
    setBidSuccess('');

    if (!user) {
      setBidError('Please log in to place a bid');
      return;
    }

    if (timeLeft === 'Auction Ended') {
      setBidError('This auction has ended');
      return;
    }

    const bidAmount = amount || parseFloat(amount);
    if (isNaN(bidAmount)) {
      setBidError('Please enter a valid amount');
      return;
    }

    try {
      await onBidSubmit(bidAmount);
      setBidSuccess('Bid placed successfully!');
      setBidAmount('');
    } catch (error) {
      setBidError(error.message);
    }
  };

  if (auction.auctionType === 'DUTCH') {
    return (
      <div className="mt-4 p-4 border rounded bg-gray-50">
        <h3 className="text-xl font-semibold mb-2">Dutch Auction</h3>
        <p className="mb-2">Current Price: ${auction.currentBidPrice.toFixed(2)}</p>
        {auction.priceDecrement && (
          <p className="mb-2">Price decreases by ${auction.priceDecrement.toFixed(2)} every minute</p>
        )}
        {auction.minimumPrice && (
          <p className="mb-2">Minimum Price: ${auction.minimumPrice.toFixed(2)}</p>
        )}
        <button
          onClick={() => handleBid(auction.currentBidPrice)}
          className="w-full px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 disabled:bg-gray-400"
          disabled={timeLeft === 'Auction Ended'}
        >
          Buy Now at ${auction.currentBidPrice.toFixed(2)}
        </button>
      </div>
    );
  }

  // Forward Auction Interface
  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="text-xl font-semibold mb-2">Place a Bid</h3>
      <input
        type="number"
        placeholder={`Enter bid higher than $${auction.currentBidPrice.toFixed(2)}`}
        value={bidAmount}
        onChange={(e) => setBidAmount(e.target.value)}
        className="w-full px-3 py-2 border rounded mb-2"
        min={auction.currentBidPrice + 0.01}
        step="0.01"
      />
      <button
        onClick={() => handleBid(bidAmount)}
        className="w-full px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-400"
        disabled={timeLeft === 'Auction Ended'}
      >
        Place Bid
      </button>
      {bidError && (
        <div className="mt-2 p-2 bg-red-100 border border-red-400 text-red-700 rounded">
          {bidError}
        </div>
      )}
      {bidSuccess && (
        <div className="mt-2 p-2 bg-green-100 border border-green-400 text-green-700 rounded">
          {bidSuccess}
        </div>
      )}
    </div>
  );
};

const ItemDetail = () => {
  const { id } = useParams(); // Item ID from the URL
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext); // Current authenticated user

  // Consolidated state with default values to prevent undefined access
  const [state, setState] = useState({
    item: null, // Item details
    biddingHistory: [], // List of bids
    timeLeft: '', // Time left for the auction
    error: '', // Error messages
    isEditing: false, // Whether the user is editing the listing
    newImageFiles: [], // New images selected for upload
    bidAmount: '', // User-entered bid amount
    bidError: '', // Error message for bidding
    bidSuccess: '', // Success message for bidding
  });

  const { item, biddingHistory, timeLeft, error, isEditing, newImageFiles, bidAmount, bidError, bidSuccess } = state;

  // Calculate time left based on auction end time
  const calculateTimeLeft = useCallback((endTime) => {
    if (!endTime) return 'No End Time';

    const now = new Date();
    const auctionEndDate = parseISO(endTime);
    const difference = auctionEndDate - now;

    if (difference <= 0) {
      return 'Auction Ended';
    } else {
      const days = Math.floor(difference / (1000 * 60 * 60 * 24));
      const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
      const minutes = Math.floor((difference / (1000 * 60)) % 60);
      const seconds = Math.floor((difference / 1000) % 60);

      return `${days}d ${hours}h ${minutes}m ${seconds}s`;
    }
  }, []);

  // Fetch item details and update state in a single call
  const fetchItemDetails = useCallback(async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}`, {
        method: 'GET',
      }, logout);

      if (response.ok) {
        const data = await response.json();
        // Ensure imageUrls is an array to prevent undefined access
        const imageUrls = Array.isArray(data.imageUrls) ? data.imageUrls : [];
        setState((prev) => ({
          ...prev,
          item: { ...data, imageUrls },
          timeLeft: calculateTimeLeft(data.auction ? data.auction.auctionEndTime : null),
          bidError: '',
          bidSuccess: '',
        }));

        // Log the auction ID for debugging
        console.log('Fetched Auction ID from CatalogueService:', data.auction ? data.auction.id : 'No Auction');

        // Fetch bidding history if auction exists
        if (data.auction && data.auction.id) {
          await fetchBiddingHistory(data.auction.id);
        }

      } else {
        const errorMsg = await response.text();
        setState((prev) => ({ ...prev, error: errorMsg }));
      }
    } catch (err) {
      setState((prev) => ({ ...prev, error: 'Failed to fetch item details.' }));
      console.error('Fetch Item Details Error:', err);
    }
  }, [id, calculateTimeLeft, logout]);

  // Fetch bidding history
  const fetchBiddingHistory = useCallback(async (auctionId) => {
    try {
      // Change the URL from 8082 (CatalogueService) to 8084 (AuctionService)
      const response = await authFetch(`http://localhost:8084/api/auctions/${auctionId}/bids`, {
        method: 'GET',
      }, logout);

      if (response.ok) {
        const data = await response.json();
        setState((prev) => ({
          ...prev,
          biddingHistory: data,
        }));
      } else {
        const errorData = await response.text();
        setState((prev) => ({ ...prev, error: errorData }));
      }
    } catch (err) {
      setState((prev) => ({ ...prev, error: 'Failed to fetch bidding history.' }));
      console.error('Fetch Bidding History Error:', err);
    }
  }, [logout]);
  
  // Fetch item details and bidding history on component mount
  useEffect(() => {
    fetchItemDetails();

    // Set up a 30-second interval for fetching fresh item data
    const fetchInterval = setInterval(() => {
      fetchItemDetails();
    }, 30000); // Fetch every 30 seconds

    return () => clearInterval(fetchInterval);
  }, [fetchItemDetails]);

  // Update time left every second
  const updateTimeLeft = useCallback(() => {
    if (item?.auction?.auctionEndTime) {
      const newTimeLeft = calculateTimeLeft(item.auction.auctionEndTime);
      setState((prev) => ({ ...prev, timeLeft: newTimeLeft }));
    }
  }, [item?.auction?.auctionEndTime, calculateTimeLeft]);

  useEffect(() => {
    // Set up a 1-second interval for updating the timer visually
    const timerInterval = setInterval(() => {
      updateTimeLeft();
    }, 1000); // Update timer every second

    return () => clearInterval(timerInterval);
  }, [item?.auction?.auctionEndTime, updateTimeLeft]);

  // Updated handlePlaceBid function with proper auction ID handling
  const handlePlaceBid = useCallback(async (amount) => {
    const auction = item?.auction;
    if (!auction) {
      throw new Error('Auction information not found.');
    }

    if (!auction.id) {
      throw new Error('Invalid auction information.');
    }

    if (timeLeft === 'Auction Ended') {
      throw new Error('Auction has ended.');
    }

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount)) {
      throw new Error('Please enter a valid number.');
    }

    // Validate bid amount based on auction type
    if (auction.auctionType === 'FORWARD') {
      if (parsedAmount <= auction.currentBidPrice) {
        throw new Error(`Bid must be higher than current bid ($${auction.currentBidPrice.toFixed(2)}).`);
      }
    } else if (auction.auctionType === 'DUTCH') {
      if (parsedAmount !== auction.currentBidPrice) {
        throw new Error(`Dutch auction requires exact current price ($${auction.currentBidPrice.toFixed(2)}).`);
      }
    }

    const bidRequest = {
      bidAmount: parsedAmount,
      bidderId: user.id
    };

    try {
      const response = await authFetch(`http://localhost:8084/api/auctions/${auction.id}/bid`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bidRequest),
      }, logout);

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData);
      }

      await fetchItemDetails();
    } catch (err) {
      console.error('Place Bid Error:', err);
      throw new Error(err.message || 'An error occurred while placing your bid.');
    }
  }, [item, user, timeLeft, fetchItemDetails, logout]);

  // Handle drag end for image reordering
  const onDragEnd = useCallback(
    (result) => {
      if (!result.destination || !item?.imageUrls) return;

      const reorderedImages = Array.from(item.imageUrls);
      const [movedImage] = reorderedImages.splice(result.source.index, 1);
      reorderedImages.splice(result.destination.index, 0, movedImage);

      setState((prev) => ({
        ...prev,
        item: { ...prev.item, imageUrls: reorderedImages },
      }));
    },
    [item]
  );

  // Handle image deletion
  const handleDeleteImage = useCallback(
    (index) => {
      if (!item?.imageUrls) return;

      const updatedImages = item.imageUrls.filter((_, i) => i !== index);
      setState((prev) => ({
        ...prev,
        item: { ...prev.item, imageUrls: updatedImages },
      }));
    },
    [item]
  );

  // Handle new image file selection
  const handleNewImageChange = useCallback((event) => {
    setState((prev) => ({ ...prev, newImageFiles: event.target.files }));
  }, []);

  // Handle uploading new images
  const handleUploadNewImages = useCallback(async () => {
    if (newImageFiles.length === 0) {
      alert('Please select images to upload.');
      return;
    }

    const formData = new FormData();
    for (let i = 0; i < newImageFiles.length; i++) {
      formData.append('images', newImageFiles[i]);
    }

    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}/upload-image`, {
        method: 'POST',
        body: formData,
      }, logout);

      if (response.ok) {
        const updatedImageUrls = await response.json(); // Assuming the backend returns the updated image URLs
        alert('Images uploaded successfully!');
        setState((prev) => ({
          ...prev,
          item: { ...prev.item, imageUrls: updatedImageUrls },
          newImageFiles: [],
        }));
      } else {
        const errorMsg = await response.text();
        setState((prev) => ({ ...prev, error: `Failed to upload images: ${errorMsg}` }));
      }
    } catch (err) {
      setState((prev) => ({ ...prev, error: 'An error occurred while uploading images.' }));
      console.error('Upload Images Error:', err);
    }
  }, [id, newImageFiles, logout]);

  // Handle saving changes after editing images
  const handleSaveChanges = useCallback(async () => {
    if (!item?.imageUrls) return;

    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}/update-images`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(item.imageUrls), // Send updated images array
      }, logout);

      if (response.ok) {
        const updatedImageUrls = await response.json(); // Assuming the backend returns the updated image URLs
        alert('Images updated successfully!');
        setState((prev) => ({
          ...prev,
          isEditing: false,
          item: { ...prev.item, imageUrls: updatedImageUrls },
        }));
      } else {
        const errorMsg = await response.text();
        setState((prev) => ({ ...prev, error: `Failed to save changes: ${errorMsg}` }));
      }
    } catch (err) {
      setState((prev) => ({ ...prev, error: 'An error occurred while saving changes.' }));
      console.error('Save Changes Error:', err);
    }
  }, [id, item?.imageUrls, logout]);

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
        <button
          onClick={() => navigate('/items')}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Back to Items
        </button>
      </div>
    );
  }

  // Render loading state
  if (!item) {
    return (
      <div className="container mx-auto px-4 py-6">
        <p>Loading item details...</p>
      </div>
    );
  }

  // Extract auction data safely
  const auction = item.auction || {};

  // Determine if the current user is the owner
  const isOwner = user && user.id === auction.sellerId; // Assuming 'sellerId' is the user ID

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
            isEditing ? (
              <DragDropContext onDragEnd={onDragEnd}>
                <Droppable droppableId="images">
                  {(provided) => (
                    <div {...provided.droppableProps} ref={provided.innerRef}>
                      {item.imageUrls.map((url, index) => (
                        <Draggable key={url} draggableId={url} index={index}>
                          {(provided) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              className="relative mb-4"
                            >
                              <img
                                src={`http://localhost:8082${url}`}
                                alt={`${item.name} ${index + 1}`}
                                className="w-full h-64 object-cover rounded"
                              />
                              <button
                                type="button"
                                onClick={() => handleDeleteImage(index)}
                                className="absolute top-1 right-1 bg-red-600 text-white rounded-full p-1 hover:bg-red-700 focus:outline-none"
                                title="Delete Image"
                              >
                                &times;
                              </button>
                            </div>
                          )}
                        </Draggable>
                      ))}
                      {provided.placeholder}
                    </div>
                  )}
                </Droppable>
              </DragDropContext>
            ) : (
              <Carousel
                showThumbs={false}
                showStatus={false}
                infiniteLoop
                useKeyboardArrows
                dynamicHeight
              >
                {item.imageUrls.map((url, index) => (
                  <div key={index}>
                    <img
                      src={`http://localhost:8082${url}`}
                      alt={`${item.name} ${index + 1}`}
                      className="object-contain h-64 w-full"
                    />
                  </div>
                ))}
              </Carousel>
            )
          ) : (
            <img
              src="https://placehold.co/600x400"
              alt="Placeholder"
              className="w-full h-96 object-cover rounded"
            />
          )}
          {isEditing && (
            <div className="mt-4">
              <input type="file" multiple onChange={handleNewImageChange} className="mb-2" />
              <button
                onClick={handleUploadNewImages}
                className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
              >
                Upload New Images
              </button>
            </div>
          )}
        </div>

        {/* Details Section */}
        <div className="md:w-1/2 md:pl-8">
          <h2 className="text-3xl font-bold mb-4">{item.name}</h2>
          <p className="mb-2">
            <strong>Description:</strong> {item.description}
          </p>
          <p className="mb-2">
            <strong>Auction Type:</strong> {auction.auctionType || 'N/A'}
          </p>
          <p className="mb-2">
            <strong>Starting Bid:</strong> $
            {auction.startingBidPrice !== undefined ? auction.startingBidPrice.toFixed(2) : 'N/A'}
          </p>
          <p className="mb-2">
            <strong>Current Bid:</strong> $
            {auction.currentBidPrice !== undefined ? auction.currentBidPrice.toFixed(2) : 'N/A'}
          </p>
		  <p className="mb-2">
		    <strong>Listed By:</strong>{" "}
		    <Link 
		      to={`/profile/${item.listedBy}`} 
		      className="text-blue-500 hover:underline"
		    >
		      {item.listedByName}
		    </Link>
		  </p>
          <p className="mb-2">
            <strong>Time Left:</strong> {timeLeft}
          </p>

          {/* Bid Success and Error Messages */}
          {bidSuccess && (
            <div className="mb-2 p-2 bg-green-100 border border-green-400 text-green-700 rounded">
              {bidSuccess}
            </div>
          )}
          {bidError && (
            <div className="mb-2 p-2 bg-red-100 border border-red-400 text-red-700 rounded">
              {bidError}
            </div>
          )}

          {/* Bidding Section */}
		  {user && !isOwner && (
		    <BiddingSection
		      auction={auction}
		      user={user}
		      timeLeft={timeLeft}
		      onBidSubmit={handlePlaceBid}
		    />
		  )}

          {/* Edit Listing Button - Visible Only to Lister */}
          {isOwner && (
            <div className="mt-4">
              <button
                onClick={() => setState((prev) => ({ ...prev, isEditing: !prev.isEditing }))}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                {isEditing ? 'Finish Editing' : 'Edit Listing'}
              </button>
              {isEditing && (
                <button
                  onClick={handleSaveChanges}
                  className="ml-4 px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                >
                  Save Changes
                </button>
              )}
            </div>
          )}
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
                  <td className="py-2 px-4 border">{bid.bidderUsername}</td>
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
