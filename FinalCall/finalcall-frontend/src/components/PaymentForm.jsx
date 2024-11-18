import React, { useState } from 'react';
import { useLocation, useHistory, useNavigate } from 'react-router-dom';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import axios from 'axios';

const PaymentForm = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const stripe = useStripe();
    const elements = useElements();

    const { user, item } = location.state; // Ensure you pass user and item from previous page

    const [error, setError] = useState(null);
    const [processing, setProcessing] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setProcessing(true);

        if (!stripe || !elements) {
            setError('Stripe has not loaded yet.');
            setProcessing(false);
            return;
        }

        const cardElement = elements.getElement(CardElement);

        // Create PaymentMethod
        const { error, paymentMethod } = await stripe.createPaymentMethod({
            type: 'card',
            card: cardElement,
            billing_details: {
                name: user.username,
                email: user.email,
                address: {
                    line1: user.address,
                },
            },
        });

        if (error) {
            setError(error.message);
            setProcessing(false);
            return;
        }

        // Send payment data to backend
        try {
            const paymentRequest = {
                amount: item.price,
                currency: 'usd',
                paymentMethodId: paymentMethod.id,
                userId: user.id,
                itemId: item.id,
            };

            const response = await axios.post('http://localhost:8094/api/payments', paymentRequest);

            if (response.status === 200) {
                navigate('/confirmation', { success: true, details: response.data });
            } else {
                setError('Payment failed.');
                setProcessing(false);
            }
        } catch (err) {
            setError(err.response?.data || 'Payment processing failed.');
            setProcessing(false);
        }
    };

    return (
        <div>
            <h2>Payment Form</h2>
            {error && <div>{error}</div>}
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Name</label>
                    <input type="text" value={user.username} readOnly />
                </div>
                <div>
                    <label>Email</label>
                    <input type="email" value={user.email} readOnly />
                </div>
                <div>
                    <label>Address</label>
                    <input type="text" value={user.address} readOnly />
                </div>
                <div>
                    <label>Card Details</label>
                    <CardElement />
                </div>
                <button type="submit" disabled={!stripe || processing}>
                    {processing ? 'Processing...' : 'Pay Now'}
                </button>
            </form>
        </div>
    );
};

export default PaymentForm;
