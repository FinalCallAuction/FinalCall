import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ItemsPage = () => {
    const [items, setItems] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        // Fetch items from the API
        fetch('/api/items')
            .then((res) => res.json())
            .then((data) => setItems(data))
            .catch((err) => console.error('Error fetching items:', err));
    }, []);

    const handleAddToCart = (item) => {
        // Save the item to the cart (localStorage or API)
        const cart = JSON.parse(localStorage.getItem('cart')) || [];
        localStorage.setItem('cart', JSON.stringify([...cart, item]));
        alert(`${item.name} added to cart!`);
    };

    const goToCart = () => {
        navigate('/cart');
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <h1 className="text-3xl font-bold mb-4">Items Listing</h1>
            {items.length === 0 ? (
                <p className="text-gray-700">No items available yet. Stay tuned!</p>
            ) : (
                <ul>
                    {items.map((item) => (
                        <li key={item.id}>
                            {item.name} - ${item.price / 100}
                            <button
                                onClick={() => handleAddToCart(item)}
                                className="ml-2 bg-blue-500 text-white px-2 py-1 rounded"
                            >
                                Add to Cart
                            </button>
                        </li>
                    ))}
                </ul>
            )}
            <button
                onClick={goToCart}
                className="mt-4 bg-green-500 text-white px-4 py-2 rounded"
            >
                Go to Cart
            </button>
        </div>
    );
};

export default ItemsPage;
