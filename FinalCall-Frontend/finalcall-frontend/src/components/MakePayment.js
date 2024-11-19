// src/components/MakePayment.js

import React, { useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';

const MakePayment = () => {
  const { id } = useParams(); // itemId from the URL
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [amount, setAmount] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  if (!user) {
    navigate('/login');
  }

  const handlePayment = async () => {
    setError('');
    setSuccess('');

    if (!amount) {
      setError('Please enter an amount.');
      return;
    }

    try {
      const response = await authFetch(`http://localhost:8083/api/payments/submit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: user.id,
          itemId: id,
          amount: parseFloat(amount),
          currency: 'USD', // Assuming default currency
          paymentMethodId: 'mock-payment-method', // Mock payment method ID
        }),
      });

      if (response.ok) {
        const payment = await response.json();
        setSuccess('Payment successful!');
        navigate('/items');
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Payment failed. Please try again.');
      console.error('Payment Error:', err);
    }
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Make Payment</h1>
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
          <strong className="font-bold">Error:</strong> <span className="block sm:inline">{error}</span>
        </div>
      )}
      {success && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
          <strong className="font-bold">Success:</strong> <span className="block sm:inline">{success}</span>
        </div>
      )}
      <div className="mb-4">
        <label className="block text-gray-700">Amount to Pay ($)</label>
        <input
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          className="w-full px-3 py-2 border rounded mt-1"
          min="0.01"
          step="0.01"
        />
      </div>
      <button
        onClick={handlePayment}
        className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600"
      >
        Make Payment
      </button>
    </div>
  );
};

export default MakePayment;
