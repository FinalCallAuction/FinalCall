import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';
import CountdownTimer from './CountdownTimer';

const MyBids = () => {
  const { user, logout } = useContext(AuthContext);
  const [bids, setBids] = useState({
    active: [],
    won: [],
    lost: []
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUserBids = async () => {
      if (!user) return;

      try {
        // Fetch bids from Auction Service
        const bidsResponse = await authFetch(`http://localhost:8084/api/auctions/user/${user.id}/bids`, {
          method: 'GET',
        }, logout);

        if (!bidsResponse.ok) {
          throw new Error('Failed to fetch bids');
        }

        const userBids = await bidsResponse.json();

        // Collate bids by item
        const collatedBids = {};
        userBids.forEach(bid => {
          const itemId = bid.auction.item.id;
          if (!collatedBids[itemId] || bid.amount > collatedBids[itemId].amount) {
            collatedBids[itemId] = bid;
          }
        });

        // Categorize collated bids
        const now = new Date();
        const categorizedBids = {
          active: [],
          won: [],
          lost: []
        };

        Object.values(collatedBids).forEach(bid => {
          const auction = bid.auction;
          const auctionEndTime = new Date(auction.auctionEndTime);

          if (auctionEndTime > now) {
            // Active auction
            categorizedBids.active.push(bid);
          } else {
            // Ended auction
            if (auction.currentBidderId === user.id) {
              categorizedBids.won.push(bid);
            } else {
              categorizedBids.lost.push(bid);
            }
          }
        });

        // Sort active bids: winning bids first (green), then losing bids (red)
        categorizedBids.active.sort((a, b) => {
          const aWinning = a.auction.currentBidderId === user.id;
          const bWinning = b.auction.currentBidderId === user.id;
          return aWinning ? -1 : bWinning ? 1 : 0;
        });

        setBids(categorizedBids);
        setLoading(false);
      } catch (err) {
        setError(err.message);
        setLoading(false);
      }
    };

    fetchUserBids();
  }, [user, logout]);

  if (!user) {
    return <div>Please log in to view your bids.</div>;
  }

  if (loading) {
    return <div>Loading bids...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  const renderBidSection = (sectionTitle, bidList, statusColor = '') => (
    <div className="mb-8">
      <h2 className="text-2xl font-semibold mb-4">{sectionTitle}</h2>
      {bidList.length === 0 ? (
        <p>No {sectionTitle.toLowerCase()} bids.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {bidList.map((bid) => {
            const auction = bid.auction;
            const isWinning = auction.currentBidderId === user.id;
            const isCurrentBid = bid.amount === auction.currentBidPrice;
            
            return (
              <div 
                key={auction.item.id} 
                className={`border p-4 rounded shadow ${
                  statusColor === 'green' && !isCurrentBid ? 'border-red-500 bg-red-50' :
                  statusColor === 'green' && isCurrentBid ? 'border-green-500' : ''
                }`}
              >
                <h3 className={`text-xl font-bold mb-2 ${
                  statusColor === 'green' && !isCurrentBid ? 'text-red-600' :
                  statusColor === 'green' && isCurrentBid ? 'text-green-600' : ''
                }`}>
                  {auction.item.name}
                </h3>
                {auction.item.imageUrls && auction.item.imageUrls.length > 0 && (
                  <img
                    src={`http://localhost:8082${auction.item.imageUrls[0]}`}
                    alt={auction.item.name}
                    className="w-full h-48 object-cover mb-2 rounded"
                  />
                )}
                <p>Your Bid: ${bid.amount.toFixed(2)}</p>
                <p className={`${
                  statusColor === 'green' && !isCurrentBid ? 'text-red-600' : ''
                }`}>
                  Current Bid: ${auction.currentBidPrice.toFixed(2)}
                </p>
                {!isCurrentBid && statusColor === 'green' && (
                  <p className="text-red-600 font-semibold mt-2">
                    You are no longer the highest bidder
                  </p>
                )}
                {auction.auctionEndTime && (
                  <CountdownTimer endTime={auction.auctionEndTime} />
                )}
                <Link
                  to={`/items/${auction.item.id}`}
                  className="inline-block mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                  View Details
                </Link>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-6">My Bids</h1>
      
      {renderBidSection('Active Bids', bids.active, 'green')}
      {renderBidSection('Won Auctions', bids.won)}
      {renderBidSection('Lost Auctions', bids.lost)}
    </div>
  );
};

export default MyBids;