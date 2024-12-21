// src/components/payment/PaymentConfirmation.js
import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';

const PaymentConfirmation = () => {
  const { transactionId } = useParams();
  const navigate = useNavigate();
  const { logout } = useContext(AuthContext);
  const [payment, setPayment] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPaymentDetails = async () => {
      try {
        const response = await authFetch(
          `http://localhost:8083/api/payments/transaction/${transactionId}`,
          { method: 'GET' },
          logout
        );

        if (response.ok) {
          const data = await response.json();
          setPayment(data);
        } else {
          setError('Failed to fetch payment details');
        }
      } catch (err) {
        setError('An error occurred while fetching payment details');
      }
    };

    fetchPaymentDetails();
  }, [transactionId, logout]);

  if (error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      </div>
    );
  }

  if (!payment) {
    return (
      <div className="container mx-auto px-4 py-6">
        <p>Loading payment details...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <div className="bg-white shadow-lg rounded-lg p-6 max-w-2xl mx-auto">
        <div className="text-center mb-6">
          {payment.status === 'SUCCESS' ? (
            <div className="text-green-500 text-5xl mb-4">✓</div>
          ) : (
            <div className="text-red-500 text-5xl mb-4">×</div>
          )}
          <h1 className="text-2xl font-bold mb-2">
            Payment {payment.status === 'SUCCESS' ? 'Successful' : 'Failed'}
          </h1>
        </div>

        <div className="border-t border-b py-4 my-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="text-gray-600">Transaction ID:</div>
            <div>{payment.transactionId}</div>
            
            <div className="text-gray-600">Amount:</div>
            <div>${payment.amount.toFixed(2)} {payment.currency}</div>
            
            <div className="text-gray-600">Payment Method:</div>
            <div>Card ending in {payment.lastFourDigits}</div>
            
            <div className="text-gray-600">Date:</div>
            <div>{new Date(payment.timestamp).toLocaleString()}</div>
          </div>
        </div>

        <div className="flex justify-center space-x-4 mt-6">
          <button
            onClick={() => navigate('/items')}
            className="bg-blue-500 text-white px-6 py-2 rounded hover:bg-blue-600"
          >
            Back to Items
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentConfirmation;
