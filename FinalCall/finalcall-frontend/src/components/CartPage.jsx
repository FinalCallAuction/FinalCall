import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const CartPage = () => {
    const [cart, setCart] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const savedCart = JSON.parse(localStorage.getItem('cart')) || [];
        setCart(savedCart);
    }, []);

    const handleCheckout = () => {
        navigate('/payment-form');
    };

    return (
        <div>
            <h1>Your Cart</h1>
            {cart.length === 0 ? (
                <p>Your cart is empty.</p>
            ) : (
                <ul>
                    {cart.map((item, index) => (
                        <li key={index}>
                            {item.name} - ${item.price / 100}
                        </li>
                    ))}
                </ul>
            )}
            {cart.length > 0 && <button onClick={handleCheckout}>Proceed to Payment</button>}
        </div>
    );
};

export default CartPage;
