import React, { useEffect, useState, useCallback, useContext } from "react";
import { Link } from "react-router-dom";
import { useParams, useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import { NotificationContext } from "../context/NotificationContext";
import CountdownTimer from "./CountdownTimer";
import "react-responsive-carousel/lib/styles/carousel.min.css";
import { Carousel } from "react-responsive-carousel";
import { formatDistance, parseISO } from "date-fns";

const ItemDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const { addNotification } = useContext(NotificationContext);
  const [state, setState] = useState({
    item: null,
    biddingHistory: [],
    error: "",
    bidAmount: "",
    bidError: "",
    bidSuccess: "",
  });

  const fetchItemDetails = useCallback(async () => {
    try {
      const response = await fetch(`http://localhost:8082/api/items/${id}`, {
        method: "GET",
        credentials: "include",
      });

      if (response.ok) {
        const data = await response.json();
        setState((prev) => ({
          ...prev,
          item: data,
          error: "",
        }));

        // Fetch initial bidding history if auction exists
        if (data.auction?.id) {
          try {
            const bidsResponse = await fetch(
              `http://localhost:8084/api/auctions/${data.auction.id}/bids`,
              {
                method: "GET",
                credentials: "include",
              }
            );

            if (bidsResponse.ok) {
              const bidsData = await bidsResponse.json();
              setState((prev) => ({
                ...prev,
                biddingHistory: bidsData,
              }));
            }
          } catch (error) {
            console.error("Error fetching bids:", error);
          }
        }
      } else {
        const errorMsg = await response.text();
        setState((prev) => ({
          ...prev,
          error: `Error: ${errorMsg}`,
        }));
      }
    } catch (err) {
      console.error("Fetch Error:", err);
      setState((prev) => ({
        ...prev,
        error: "Failed to fetch item details. Please try again later.",
      }));
    }
  }, [id]);
  
  useEffect(() => {
      fetchItemDetails();
    }, [fetchItemDetails]);

    useEffect(() => {
      if (state.item?.auction?.id) {
        const ws = new WebSocket(
          `ws://localhost:8084/ws/auctions/${state.item.auction.id}`
        );

        ws.onopen = () => {
          console.log("WebSocket connected for auction:", state.item.auction.id);
        };

        ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            if (data.type === "AUCTION_UPDATE") {
              setState((prev) => ({
                ...prev,
                item: { ...prev.item, auction: data.auction },
                biddingHistory: data.biddingHistory,
              }));
            }
          } catch (error) {
            console.error("Error parsing WebSocket message:", error);
          }
        };

        ws.onclose = () => {
          console.log("WebSocket connection closed");
        };

        return () => {
          if (ws.readyState === WebSocket.OPEN) {
            ws.close();
          }
        };
      }
    }, [state.item?.auction?.id]);
	
  // Update the bid handling function:
  const handleBid = async (amount) => {
    if (!user) {
      setState(prev => ({
        ...prev,
        bidError: 'Please log in to place a bid',
        bidSuccess: ''
      }));
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8084/api/auctions/${state.item.auction.id}/bid`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${user.accessToken}`
          },
          body: JSON.stringify({
            bidAmount: parseFloat(amount),
            bidderId: user.id
          })
        }
      );

      if (response.ok) {
        setState(prev => ({
          ...prev,
          bidAmount: '',
          bidError: '',
          bidSuccess: 'Bid placed successfully!'
        }));
        // Refresh will happen via WebSocket
      } else {
        const errorData = await response.text();
        setState(prev => ({
          ...prev,
          bidError: errorData,
          bidSuccess: ''
        }));
      }
    } catch (err) {
      console.error('Bid Error:', err);
      setState(prev => ({
        ...prev,
        bidError: 'An error occurred while placing your bid.',
        bidSuccess: ''
      }));
    }
  };

  const handleDutchAuctionDecrement = async (decrementAmount) => {
    try {
      const response = await fetch(
        `http://localhost:8084/api/auctions/${state.item.auction.id}/decrement`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            userId: user.id,
            decrementAmount: decrementAmount,
          }),
        },
      );

      if (!response.ok) {
        const errorMsg = await response.text();
        setState((prev) => ({
          ...prev,
          error: errorMsg,
        }));
      }
      // Refresh will happen automatically via WebSocket
    } catch (err) {
      console.error("Price Decrease Error:", err);
      setState((prev) => ({
        ...prev,
        error: "Failed to decrease price",
      }));
    }
  };

  // Early returns for loading and error states
  if (state.error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4">
          <strong className="font-bold"> Error: </strong>{" "}
          <span className="block sm:inline"> {state.error} </span>{" "}
        </div>{" "}
        <button
          onClick={() => navigate("/items")}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Back to Items{" "}
        </button>{" "}
      </div>
    );
  }

  if (!state.item) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-4"> </div>{" "}
          <div className="h-64 bg-gray-200 rounded mb-4"> </div>{" "}
          <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"> </div>{" "}
          <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"> </div>{" "}
        </div>{" "}
      </div>
    );
  }

  const auction = state.item.auction || {};
  const isOwner = user && user.id === auction.sellerId;
  const isDutchAuction = auction.auctionType === "DUTCH";

  return (
    <div className="container mx-auto px-4 py-6">
      <button
        onClick={() => navigate("/items")}
        className="mb-4 px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400"
      >
        & larr; Back to Items{" "}
      </button>
      <div className="flex flex-col md:flex-row">
        {" "}
        {/* Image Gallery Section */}{" "}
        <div className="md:w-1/2">
          {" "}
          {state.item.imageUrls?.length > 0 ? (
            <Carousel
              showThumbs={false}
              showStatus={false}
              infiniteLoop
              useKeyboardArrows
              dynamicHeight
            >
              {" "}
              {state.item.imageUrls.map((url, index) => (
                <div key={index}>
                  <img
                    src={`http://localhost:8082${url}`}
                    alt={`${state.item.name} ${index + 1}`}
                    className="object-contain h-64 w-full"
                  />
                </div>
              ))}{" "}
            </Carousel>
          ) : (
            <img
              src="https://placehold.co/600x400"
              alt="Placeholder"
              className="w-full h-96 object-cover rounded"
            />
          )}{" "}
        </div>
        {/* Details Section */}{" "}
        <div className="md:w-1/2 md:pl-8 mt-4 md:mt-0">
          <h2 className="text-3xl font-bold mb-4"> {state.item.name} </h2>
          <div className="space-y-2 mb-4">
            <p>
              {" "}
              <strong> Description: </strong> {state.item.description}
            </p>
            <p>
              <strong> Listed By: </strong>{" "}
              <Link
                to={`/profile/${state.item.listedBy}`}
                className="text-blue-500 hover:underline"
              >
                {" "}
                {state.item.listedByName}{" "}
              </Link>{" "}
            </p>{" "}
            <p>
              {" "}
              <strong> Auction Type: </strong>{" "}
              {isDutchAuction ? "Dutch Auction" : "Forward Auction"}
            </p>
            <p>
              {" "}
              <strong> Starting Bid: </strong> $
              {auction.startingBidPrice?.toFixed(2)}
            </p>
            <p>
              {" "}
              <strong> Current Bid: </strong> $
              {auction.currentBidPrice?.toFixed(2)}
            </p>{" "}
            {auction.auctionEndTime && (
              <CountdownTimer endTime={auction.auctionEndTime} />
            )}{" "}
          </div>
          {/* Bidding Section */}{" "}
          {!isOwner && user && (
            <div className="mt-6 p-4 bg-gray-50 rounded shadow">
              <h3 className="text-xl font-semibold mb-2">
                {" "}
                {isDutchAuction ? "Buy Now" : "Place a Bid"}{" "}
              </h3>
              {isDutchAuction ? (
                <button
                  onClick={() => handleBid(auction.currentBidPrice)}
                  className="w-full bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600 transition-colors"
                  disabled={auction.status === "ENDED"}
                >
                  Buy Now at $ {auction.currentBidPrice?.toFixed(2)}{" "}
                </button>
              ) : (
                <div className="space-y-2">
                  <input
                    type="number"
                    value={state.bidAmount}
                    onChange={(e) =>
                      setState((prev) => ({
                        ...prev,
                        bidAmount: e.target.value,
                      }))
                    }
                    className="w-full px-3 py-2 border rounded"
                    placeholder={`Enter bid higher than $${auction.currentBidPrice?.toFixed(2)}`}
                    min={auction.currentBidPrice + 0.01}
                    step="0.01"
                  />
                  <button
                    onClick={() => handleBid(state.bidAmount)}
                    className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600 transition-colors"
                    disabled={auction.status === "ENDED"}
                  >
                    Place Bid{" "}
                  </button>{" "}
                </div>
              )}
              {state.bidError && (
                <div className="mt-2 p-2 bg-red-100 text-red-700 rounded">
                  {" "}
                  {state.bidError}{" "}
                </div>
              )}{" "}
              {state.bidSuccess && (
                <div className="mt-2 p-2 bg-green-100 text-green-700 rounded">
                  {" "}
                  {state.bidSuccess}{" "}
                </div>
              )}{" "}
            </div>
          )}
          {/* Dutch Auction Controls for Owner */}{" "}
          {isOwner && isDutchAuction && (
            <div className="mt-6 p-4 bg-gray-50 rounded shadow">
              <h3 className="text-xl font-semibold mb-2"> Price Controls </h3>{" "}
              <div className="space-y-2">
                <input
                  type="number"
                  value={state.decrementAmount}
                  onChange={(e) =>
                    setState((prev) => ({
                      ...prev,
                      decrementAmount: e.target.value,
                    }))
                  }
                  className="w-full px-3 py-2 border rounded"
                  placeholder="Enter amount to decrease"
                  min="0.01"
                  step="0.01"
                />
                <button
                  onClick={() =>
                    handleDutchAuctionDecrement(
                      parseFloat(state.decrementAmount),
                    )
                  }
                  className="w-full bg-yellow-500 text-white py-2 rounded hover:bg-yellow-600 transition-colors"
                  disabled={auction.status === "ENDED"}
                >
                  Decrease Price{" "}
                </button>{" "}
              </div>{" "}
            </div>
          )}{" "}
        </div>{" "}
      </div>
      {/* Bidding History Section */}{" "}
      <div className="mt-8">
        <h3 className="text-2xl font-semibold mb-4">
          {" "}
          {isDutchAuction ? "Price History" : "Bidding History"}{" "}
        </h3>
        {state.biddingHistory.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full bg-white border">
              <thead>
                <tr>
                  <th className="py-2 px-4 border"> User </th>{" "}
                  <th className="py-2 px-4 border">
                    {" "}
                    {isDutchAuction ? "Price Change" : "Bid Amount"}{" "}
                  </th>{" "}
                  <th className="py-2 px-4 border"> Time </th>{" "}
                </tr>{" "}
              </thead>{" "}
              <tbody>
                {" "}
                {state.biddingHistory.map((entry, index) => (
                  <tr key={index} className="text-center">
                    <td className="py-2 px-4 border">
                      {" "}
                      {entry.bidderUsername}{" "}
                    </td>{" "}
                    <td className="py-2 px-4 border">
                      $ {entry.amount.toFixed(2)}{" "}
                      {isDutchAuction && entry.previousAmount && (
                        <span className="text-sm text-gray-500">
                          {" "}
                          (−$ {(entry.previousAmount - entry.amount).toFixed(2)}
                          ){" "}
                        </span>
                      )}{" "}
                    </td>{" "}
                    <td className="py-2 px-4 border">
                      {" "}
                      {formatDistance(parseISO(entry.timestamp), new Date(), {
                        addSuffix: true,
                      })}{" "}
                    </td>{" "}
                  </tr>
                ))}{" "}
              </tbody>{" "}
            </table>{" "}
          </div>
        ) : (
          <p className="text-gray-600">
            No {isDutchAuction ? "price changes" : "bids"}
            yet.{" "}
          </p>
        )}{" "}
      </div>{" "}
    </div>
  );
};

export default ItemDetail;
