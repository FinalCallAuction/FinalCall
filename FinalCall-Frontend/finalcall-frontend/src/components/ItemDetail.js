import React, { useEffect, useState, useContext, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow, parseISO } from 'date-fns';
import { AuthContext } from '../context/AuthContext';
import { Carousel } from 'react-responsive-carousel';
import 'react-responsive-carousel/lib/styles/carousel.min.css'; // Import carousel styles
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';

const ItemDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  // Consolidated state with default values to prevent undefined access
  const [state, setState] = useState({
    item: null,
    biddingHistory: [],
    timeLeft: '',
    error: '',
    isEditing: false,
    newImageFiles: [],
  });

  const { item, biddingHistory, timeLeft, error, isEditing, newImageFiles } = state;

  // Fetch item details and initialize bidding history
  useEffect(() => {
    const fetchData = async () => {
      await fetchItemDetails();
      // Initialize bidding history with a stub or fetch actual history
      setState((prev) => ({
        ...prev,
        biddingHistory: [
          {
            bidderUsername: 'stubUser1',
            amount: 100.0,
            timestamp: new Date().toISOString(),
          },
        ],
      }));
    };

    fetchData();

    // Set up a 30-second interval for fetching fresh item data
    const fetchInterval = setInterval(() => {
      fetchItemDetails();
    }, 30000); // Fetch every 30 seconds

    return () => clearInterval(fetchInterval);
  }, [id]); // Depend only on 'id'

  useEffect(() => {
    // Set up a 1-second interval for updating the timer visually
    const timerInterval = setInterval(() => {
      updateTimeLeft();
    }, 1000); // Update timer every second

    return () => clearInterval(timerInterval);
  }, [item?.auctionEndTime]); // Depend on auction end time so that interval restarts if auction end time changes

  // Fetch item details and update state in a single call
  const fetchItemDetails = useCallback(async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}`, {
        method: 'GET',
      });

      if (response.ok) {
        const data = await response.json();
        // Ensure imageUrls is an array to prevent undefined access
        const imageUrls = Array.isArray(data.imageUrls) ? data.imageUrls : [];
        setState((prev) => ({
          ...prev,
          item: { ...data, imageUrls }, // Merge data with imageUrls
          timeLeft: calculateTimeLeft(data.auctionEndTime),
        }));
      } else {
        const errorMsg = await response.text();
        setState((prev) => ({ ...prev, error: errorMsg }));
      }
    } catch (err) {
      setState((prev) => ({ ...prev, error: 'Failed to fetch item details.' }));
      console.error('Fetch Item Details Error:', err);
    }
  }, [id]);

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
      const minutes = Math.floor((difference / 1000 / 60) % 60);
      const seconds = Math.floor((difference / 1000) % 60);

      return `${days}d ${hours}h ${minutes}m ${seconds}s`;
    }
  }, []);

  // Update time left
  const updateTimeLeft = useCallback(() => {
    if (item?.auctionEndTime) {
      const newTimeLeft = calculateTimeLeft(item.auctionEndTime);
      setState((prev) => ({ ...prev, timeLeft: newTimeLeft }));
    }
  }, [item, calculateTimeLeft]);

  // Handle drag end for image reordering
  const onDragEnd = useCallback((result) => {
    if (!result.destination || !item?.imageUrls) return;

    const reorderedImages = Array.from(item.imageUrls);
    const [movedImage] = reorderedImages.splice(result.source.index, 1);
    reorderedImages.splice(result.destination.index, 0, movedImage);

    setState((prev) => ({
      ...prev,
      item: { ...prev.item, imageUrls: reorderedImages },
    }));
  }, [item]);

  // Handle image deletion
  const handleDeleteImage = useCallback((index) => {
    if (!item?.imageUrls) return;

    const updatedImages = item.imageUrls.filter((_, i) => i !== index);
    setState((prev) => ({
      ...prev,
      item: { ...prev.item, imageUrls: updatedImages },
    }));
  }, [item]);

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
      });

      if (response.ok) {
        const updatedImageUrls = await response.json();
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
  }, [id, newImageFiles]);

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
      });

      if (response.ok) {
        const updatedImageUrls = await response.json(); // Receive the updated image URLs from the server
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
  }, [id, item?.imageUrls]);

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
              <Carousel showThumbs={false} showStatus={false} infiniteLoop useKeyboardArrows dynamicHeight>
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
              src="https://via.placeholder.com/600x400"
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
            <strong>Auction Type:</strong> {item.auctionType}
          </p>
          <p className="mb-2">
            <strong>Starting Bid:</strong> ${item.startingBid.toFixed(2)}
          </p>
          <p className="mb-2">
            <strong>Current Bid:</strong> ${item.currentBid.toFixed(2)}
          </p>
          <p className="mb-2">
            <strong>Current Bidder:</strong> {item.currentBidder || 'No bids yet'}
          </p>
          <p className="mb-2">
            <strong>Listed By:</strong> {item.listedBy}
          </p>
          <p className="mb-2">
            <strong>Time Left:</strong> {timeLeft}
          </p>
          <button
            onClick={() => navigate(`/items/${id}/payment`)}
            className="mt-4 px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          >
            Make Payment
          </button>
          {user && user.username === item.listedBy && (
            <button
              onClick={() => setState((prev) => ({ ...prev, isEditing: !prev.isEditing }))}
              className="ml-4 mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              {isEditing ? 'Finish Editing' : 'Edit'}
            </button>
          )}
          {isEditing && (
            <button
              onClick={handleSaveChanges}
              className="ml-4 mt-4 px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
            >
              Save Changes
            </button>
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
