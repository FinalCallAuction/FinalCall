import React, { useEffect, useState, useContext, useRef } from 'react';
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
  const [item, setItem] = useState(null);
  const [biddingHistory, setBiddingHistory] = useState([]);
  const [timeLeft, setTimeLeft] = useState('');
  const [error, setError] = useState('');
  const [currentBidder, setCurrentBidder] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [images, setImages] = useState([]);
  const fileInputRef = useRef(null);

  useEffect(() => {
    fetchItemDetails();
    setBiddingHistory([{
      bidderUsername: 'stubUser1',
      amount: 100.0,
      timestamp: new Date().toISOString(),
    }]);
    const timer = setInterval(() => {
      updateTimeLeft();
    }, 1000);
    return () => clearInterval(timer);
  }, [id]); // Removed 'item' from dependencies

  const fetchItemDetails = async () => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${id}`, {
        method: 'GET',
      });

      if (response.ok) {
        const data = await response.json();
        setItem(data);
        setImages(data.imageUrls);
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

  const onDragEnd = (result) => {
    if (!result.destination) return;

    const updatedImages = Array.from(images);
    const [movedImage] = updatedImages.splice(result.source.index, 1);
    updatedImages.splice(result.destination.index, 0, movedImage);
    setImages(updatedImages);
  };

  const handleDeleteImage = (index) => {
    setImages((prevImages) => prevImages.filter((_, i) => i !== index));
  };

  const handleUploadButtonClick = () => {
    fileInputRef.current.click();
  };

  const handleImageChange = (e) => {
    if (e.target.files) {
      const selectedFiles = Array.from(e.target.files);
      const MAX_IMAGES = 5;
      if (images.length + selectedFiles.length > MAX_IMAGES) {
        alert(`You can only upload up to ${MAX_IMAGES} images.`);
        return;
      }

      const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
      const maxSize = 5 * 1024 * 1024;
      const validFiles = selectedFiles.filter(
        (file) => allowedTypes.includes(file.type) && file.size <= maxSize
      );

      if (validFiles.length !== selectedFiles.length) {
        alert('Some files were rejected. Only JPEG, PNG, GIF images under 5MB are allowed.');
      }

      const imagesWithPreview = validFiles.map((file) => ({
        file,
        preview: URL.createObjectURL(file),
      }));

      setImages((prevImages) => [...prevImages, ...imagesWithPreview]);
    }
  };

  const handleSaveChanges = async () => {
    try {
      const formData = new FormData();
      formData.append('id', id);
      images.forEach((image) => {
        if (image.file) {
          formData.append('images', image.file);
        } else {
          formData.append('imageUrls', image);
        }
      });

      const response = await authFetch(`http://localhost:8082/api/items/${id}/update-images`, {
        method: 'PUT',
        body: formData,
      });

      if (response.ok) {
        const updatedImageUrls = await response.json();
        alert('Images updated successfully!');
        setIsEditing(false);
        
        // Update the item and images state after saving
        setItem((prev) => ({ ...prev, imageUrls: updatedImageUrls }));
        setImages(updatedImageUrls);
      } else {
        const errorMsg = await response.text();
        setError(`Failed to save changes: ${errorMsg}`);
      }
    } catch (err) {
      setError('An error occurred while saving changes.');
      console.error('Save Changes Error:', err);
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
          {images.length > 0 ? (
            isEditing ? (
              <>
                <DragDropContext onDragEnd={onDragEnd}>
                  <Droppable droppableId="images">
                    {(provided) => (
                      <div {...provided.droppableProps} ref={provided.innerRef}>
                        {images.map((url, index) => (
                          <Draggable key={url.preview || url} draggableId={url.preview || url} index={index}>
                            {(provided) => (
                              <div
                                ref={provided.innerRef}
                                {...provided.draggableProps}
                                {...provided.dragHandleProps}
                                className="relative mb-4"
                              >
                                <img
                                  src={url.preview || `http://localhost:8082${url}`}
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
                <input
                  type="file"
                  name="images"
                  id="images"
                  accept="image/*"
                  multiple
                  onChange={handleImageChange}
                  ref={fileInputRef}
                  className="hidden"
                />
                <button
                  type="button"
                  onClick={handleUploadButtonClick}
                  className="mt-2 w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 focus:outline-none focus:bg-blue-600"
                >
                  Upload More Images
                </button>
              </>
            ) : (
              <Carousel showThumbs={false} showStatus={false} infiniteLoop useKeyboardArrows dynamicHeight>
                {images.map((url, index) => (
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
          <button
            onClick={() => navigate(`/items/${id}/payment`)}
            className="mt-4 px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          >
            Make Payment
          </button>
          {user && user.username === item.listedBy && (
            <button
              onClick={() => setIsEditing((prev) => !prev)}
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
