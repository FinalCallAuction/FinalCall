import React, { useEffect, useState, useCallback } from "react";
import { Link } from 'react-router-dom';
import CountdownTimer from './CountdownTimer';

const ItemsPage = () => {
    const [items, setItems] = useState([]);
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
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setItems(data);
                setError("");
            } else {
                const errorText = await response.text();
                setError(`Failed to fetch items: ${errorText}`);
            }
        } catch (err) {
            setError("Failed to fetch items");
            console.error("Error:", err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchItems();

        // Connect to WebSocket for real-time updates
        const ws = new WebSocket('ws://localhost:8082/ws/items');
        
        ws.onopen = () => {
            console.log("Connected to items WebSocket");
        };

        ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            
            switch(message.type) {
                case "INITIAL_ITEMS":
                    setItems(message.data);
                    break;
                case "ITEM_UPDATE":
                    setItems(prevItems => 
                        prevItems.map(item => 
                            item.id === message.data.id ? message.data : item
                        )
                    );
                    break;
                default:
                    console.log("Unknown message type:", message.type);
            }
        };

        return () => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.close();
            }
        };
    }, [fetchItems]);

    // Filter and pagination logic
    const filteredItems = items.filter((item) =>
        item.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentItems = filteredItems.slice(indexOfFirstItem, indexOfLastItem);
    const totalPages = Math.ceil(filteredItems.length / itemsPerPage);

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

            {loading ? (
                // Loading skeleton
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {[...Array(itemsPerPage)].map((_, index) => (
                        <div key={index} className="border p-4 rounded shadow animate-pulse">
                            <div className="h-48 bg-gray-300 rounded mb-2"></div>
                            <div className="h-6 bg-gray-300 rounded w-3/4 mb-2"></div>
                            <div className="h-4 bg-gray-300 rounded mb-2"></div>
                            <div className="h-4 bg-gray-300 rounded w-1/2"></div>
                        </div>
                    ))}
                </div>
            ) : error ? (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
                    {error}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {currentItems.map(item => (
                        <div key={item.id} className="border p-4 rounded shadow hover:shadow-lg transition-shadow duration-300">
                            <h2 className="text-xl font-semibold mb-2">{item.name}</h2>
                            
                            {/* Listed By */}
                            <p className="text-sm text-gray-600 mb-2">
                                Listed by: 
                                <Link to={`/profile/${item.listedBy}`} className="text-blue-500 hover:underline ml-1">
                                    {item.listedByName}
                                </Link>
                            </p>

                            {/* Image and Auction Type */}
                            <div className="relative">
                                <img
                                    src={item.imageUrls?.[0] ? `http://localhost:8082${item.imageUrls[0]}` : "https://placehold.co/600x400"}
                                    alt={item.name}
                                    className="w-full h-48 object-cover rounded mb-2"
                                />
                                {item.auction && (
                                    <span className={`absolute top-2 right-2 px-2 py-1 text-xs font-semibold rounded ${
                                        item.auction.auctionType === 'DUTCH' ? 'bg-yellow-200 text-yellow-800' : 'bg-blue-200 text-blue-800'
                                    }`}>
                                        {item.auction.auctionType === 'DUTCH' ? 'Dutch Auction' : 'Forward Auction'}
                                    </span>
                                )}
                            </div>

                            {/* Auction Details */}
                            {item.auction && (
                                <div className="space-y-2 mt-2">
                                    <CountdownTimer endTime={item.auction.auctionEndTime} />
                                    <p className="text-sm text-gray-600">
                                        Starting Bid: ${item.auction.startingBidPrice?.toFixed(2)}
                                    </p>
                                    <p className="font-semibold">
                                        Current Bid: ${item.auction.currentBidPrice?.toFixed(2)}
                                    </p>
                                    {item.auction.status === 'ENDED' && (
                                        <p className="text-red-600 font-semibold">Auction Ended</p>
                                    )}
                                </div>
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

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="flex justify-center mt-6 space-x-2">
                    {[...Array(totalPages)].map((_, index) => (
                        <button
                            key={index}
                            onClick={() => setCurrentPage(index + 1)}
                            className={`px-3 py-1 rounded ${
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