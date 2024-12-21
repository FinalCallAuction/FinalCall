import React, { useEffect, useContext, useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import { formatDistanceToNow, parseISO } from 'date-fns';
import { Carousel } from 'react-responsive-carousel';
import 'react-responsive-carousel/lib/styles/carousel.min.css';

const ItemDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);
  const [item, setItem] = useState(null);
  const [biddingHistory, setBiddingHistory] = useState([]);
  const [timeLeft, setTimeLeft] = useState('');
  const [error, setError] = useState('');
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

    const bidAmount = parseFloat(amount);
    if (isNaN(bidAmount)) {
      setBidError('Please enter a valid amount');
      return;
    }

    try {
      const bidRequest = {
        bidAmount: bidAmount,
        bidderId: user.id
      };

      const response = await authFetch(
        `http://localhost:8084/api/auctions/${item.auction.id}/bid`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(bidRequest),
        },
        logout
      );

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData);
      }

      setBidSuccess('Bid placed successfully!');
      setBidAmount('');
      await fetchItemDetails(); // Refresh item details after successful bid
    } catch (error) {
      setBidError(error.message);
    }
  };

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

  const fetchBiddingHistory = async (auctionId) => {
    try {
      const response = await authFetch(
        `http://localhost:8084/api/auctions/${auctionId}/bids`,
        { method: 'GET' },
        logout
      );

      if (response.ok) {
        const data = await response.json();
        setBiddingHistory(data);
      }
    } catch (err) {
      console.error('Error fetching bidding history:', err);
    }
  };

  const fetchItemDetails = useCallback(async () => {
    try {
      const response = await authFetch(
        `http://localhost:8082/api/items/${id}`,
        { method: 'GET' },
        logout
      );

      if (response.ok) {
        const data = await response.json();

        // Check and modify auction status
        if (data.auction) {
          const now = new Date();
          const endTime = new Date(data.auction.auctionEndTime);
          const hasEnded = now > endTime;

          // Debug log
          console.log('Auction Debug:', {
            now: now,
            endTime: endTime,
            hasEnded: hasEnded,
            currentBidderId: data.auction.currentBidderId,
            currentUserId: user?.id,
            auctionStatus: data.auction.status,
            timeLeft: calculateTimeLeft(data.auction.auctionEndTime),
          });

          if (hasEnded) {
            data.auction.status = 'ENDED';
          }
        }

        setItem(data);
        setTimeLeft(calculateTimeLeft(data.auction?.auctionEndTime));

        if (data.auction?.id) {
          await fetchBiddingHistory(data.auction.id);
        }
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch item details.');
      console.error('Fetch Error:', err);
    }
  }, [id, calculateTimeLeft, logout, user]);

  useEffect(() => {
    fetchItemDetails();
    const timer = setInterval(() => {
      if (item?.auction?.auctionEndTime) {
        const newTimeLeft = calculateTimeLeft(item.auction.auctionEndTime);
        setTimeLeft(newTimeLeft);

        if (newTimeLeft === 'Auction Ended' && item.auction.status !== 'ENDED') {
          setItem((prev) => ({
            ...prev,
            auction: { ...prev.auction, status: 'ENDED' },
          }));
        }
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [item?.auction?.auctionEndTime, calculateTimeLeft, fetchItemDetails]);

  const handlePlaceBid = async (amount) => {
    setBidError('');
    setBidSuccess('');

    if (!user) {
      setBidError('Please log in to place a bid');
      return;
    }

    if (!item?.auction) {
      setBidError('Auction information not found.');
      return;
    }

    try {
      const bidRequest = {
        bidAmount: parseFloat(amount),
        bidderId: user.id,
      };

      const response = await authFetch(
        `http://localhost:8084/api/auctions/${item.auction.id}/bid`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(bidRequest),
        },
        logout
      );

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData);
      }

      setBidSuccess('Bid placed successfully!');
      setBidAmount('');
      await fetchItemDetails();
    } catch (error) {
      setBidError(error.message);
    }
  };

  const isOwner = user && item?.auction && user.id === item.auction.sellerId;
  const isWinner = user && item?.auction && item.auction.currentBidderId === user.id;
  const isAuctionEnded =
    timeLeft === 'Auction Ended' || item?.auction?.status === 'ENDED';

  // Debug log for conditions
  console.log('Rendering conditions:', {
    isOwner,
    isWinner,
    isAuctionEnded,
    timeLeft,
    auctionStatus: item?.auction?.status,
    userId: user?.id,
    currentBidderId: item?.auction?.currentBidderId,
  });

  if (error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
          {error}
        </div>
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
          {item.imageUrls && item.imageUrls.length > 0 ? (
            <Carousel showThumbs={false} showStatus={false}>
              {item.imageUrls.map((url, index) => (
                <div key={index}>
                  <img
                    src={`http://localhost:8082${url}`}
                    alt={`${item.name} ${index + 1}`}
                    className="w-full h-64 object-contain"
                  />
                </div>
              ))}
            </Carousel>
          ) : (
            <img
              src="https://placehold.co/600x400"
              alt="Placeholder"
              className="w-full h-64 object-cover"
            />
          )}
        </div>

        {/* Details Section */}
        <div className="md:w-1/2 md:pl-8">
          <h2 className="text-3xl font-bold mb-4">{item.name}</h2>
          <p className="mb-2">
            <strong>Description:</strong> {item.description}
          </p>
          {item.auction && (
            <>
              <p className="mb-2">
                <strong>Starting Bid:</strong> $
                {item.auction.startingBidPrice.toFixed(2)}
              </p>
              <p className="mb-2">
                <strong>Current Bid:</strong> $
                {item.auction.currentBidPrice.toFixed(2)}
              </p>
            </>
          )}
          <p className="mb-2">
            <strong>Listed By:</strong>{' '}
            <Link
              to={`/profile/${item.listedBy}`}
              className="text-blue-500 hover:underline"
            >
              {item.listedByName}
            </Link>
          </p>
          <p className="mb-4">
            <strong>Time Left:</strong> {timeLeft}
          </p>

			{/* Payment Section */}
			<div className="mt-4 p-4 border rounded bg-gray-50">
			  <h3 className="text-xl font-semibold mb-2">Payment</h3>
			  <div className="flex flex-col items-center space-y-2">
				<p className="text-lg">
				  Current Bid: ${item.auction?.currentBidPrice?.toFixed(2)}
				</p>
				
				{user && (
				  <Link
					to={`/make-payment/${item.auction?.id}`}
					className={`w-full text-center px-4 py-2 rounded ${
					  isAuctionEnded && user.id === item.auction?.currentBidderId
						? 'bg-green-500 hover:bg-green-600 text-white'
						: 'bg-gray-300 text-gray-500 cursor-not-allowed'
					}`}
					onClick={(e) => {
					  if (!isAuctionEnded || user.id !== item.auction?.currentBidderId) {
						e.preventDefault();
					  }
					}}
				  >
					Pay Now
				  </Link>
				)}

				{/* Status Messages */}
				{!isAuctionEnded && (
				  <p className="text-yellow-600">
					Payment will be available after the auction ends
				  </p>
				)}
				{isAuctionEnded && user && user.id !== item.auction?.currentBidderId && (
				  <p className="text-red-600">
					Only the winning bidder can make payment
				  </p>
				)}
				{isAuctionEnded && user && user.id === item.auction?.currentBidderId && (
				  <p className="text-green-600">
					Congratulations! You won this auction. Click above to make payment.
				  </p>
				)}
				{!user && (
				  <p className="text-blue-600">
					Please log in to make payment
				  </p>
				)}
			  </div>
			</div>

			{/* Bidding Section - only show if auction is not ended and user is not the owner */}
			{!isOwner && !isAuctionEnded && (
			  <div className="mt-4 p-4 border rounded bg-gray-50">
				<h3 className="text-xl font-semibold mb-2">Place a Bid</h3>
				<input
				  type="number"
				  placeholder={`Enter bid higher than $${item.auction?.currentBidPrice?.toFixed(2)}`}
				  value={bidAmount}
				  onChange={(e) => setBidAmount(e.target.value)}
				  className="w-full px-3 py-2 border rounded mb-2"
				  min={item.auction?.currentBidPrice + 0.01}
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
			)}
        </div>
      </div>

      {/* Debug Info */}
      {user && item?.auction && (
        <div className="mb-4 p-2 bg-gray-100 rounded">
          <p>Debug Info:</p>
          <p>Is Winner: {isWinner.toString()}</p>
          <p>Is Auction Ended: {isAuctionEnded.toString()}</p>
          <p>Time Left: {timeLeft}</p>
          <p>User ID: {user.id}</p>
          <p>Current Bidder ID: {item.auction.currentBidderId}</p>
          <p>Auction Status: {item.auction.status}</p>
        </div>
      )}

      {/* Bidding History Section */}
      <div className="mt-8">
        <h3 className="text-2xl font-semibold mb-4">Bidding History</h3>
        {biddingHistory.length > 0 ? (
          <table className="min-w-full bg-white border">
            <thead>
              <tr>
                <th className="py-2 px-4 border">Bidder</th>
                <th className="py-2 px-4 border">Amount</th>
                <th className="py-2 px-4 border">Time</th>
              </tr>
            </thead>
            <tbody>
              {biddingHistory.map((bid, index) => (
                <tr key={index} className="text-center">
                  <td className="py-2 px-4 border">{bid.bidderUsername}</td>
                  <td className="py-2 px-4 border">
                    ${bid.amount.toFixed(2)}
                  </td>
                  <td className="py-2 px-4 border">
                    {formatDistanceToNow(parseISO(bid.timestamp), {
                      addSuffix: true,
                    })}
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
