// src/components/MakePayment.js
import React, { useState, useContext, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';

const MakePayment = () => {
  const { auctionId } = useParams();
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);
  const [auctionDetails, setAuctionDetails] = useState(null);
  const [paymentData, setPaymentData] = useState({
    cardNumber: '',
    expiryDate: '',
    cvv: '',
    cardHolderName: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const fetchAuctionDetails = async () => {
      try {
        const response = await authFetch(`http://localhost:8084/api/auctions/${auctionId}`, {
          method: 'GET'
        }, logout);

        if (response.ok) {
          const data = await response.json();
          setAuctionDetails(data);
        } else {
          const errorData = await response.text();
          setError(errorData);
        }
      } catch (err) {
        setError('Failed to fetch auction details');
        console.error('Fetch Error:', err);
      }
    };

    fetchAuctionDetails();
  }, [auctionId, logout]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setPaymentData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      const response = await authFetch(`http://localhost:8094/api/payments/process-auction-payment/${auctionId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          amount: auctionDetails.currentBidPrice,
          currency: 'USD',
          ...paymentData
        }),
      }, logout);

      if (response.ok) {
        const result = await response.json();
        setSuccess('Payment successful!');
        setTimeout(() => {
          navigate(`/items`);
        }, 2000);
      } else {
        const errorData = await response.text();
        setError(errorData);
      }
    } catch (err) {
      setError('Payment failed. Please try again.');
      console.error('Payment Error:', err);
    }
  };

  if (!auctionDetails) {
    return (
      <div className="container mx-auto px-4 py-6">
        <p>Loading auction details...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Complete Payment</h1>

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

      <div className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
        <div className="mb-6">
          <h2 className="text-xl font-semibold mb-2">Payment Details</h2>
          <p className="text-gray-700">Amount to Pay: ${auctionDetails.currentBidPrice.toFixed(2)} USD</p>
        </div>

        <form onSubmit={handlePayment}>
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="cardHolderName">
              Cardholder Name
            </label>
            <input
              type="text"
              id="cardHolderName"
              name="cardHolderName"
              value={paymentData.cardHolderName}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:border-blue-500"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="cardNumber">
              Card Number
            </label>
            <input
              type="text"
              id="cardNumber"
              name="cardNumber"
              value={paymentData.cardNumber}
              onChange={handleInputChange}
              pattern="[0-9]{16}"
              maxLength="16"
              className="w-full px-3 py-2 border rounded focus:outline-none focus:border-blue-500"
              required
            />
          </div>

          <div className="flex mb-4 space-x-4">
            <div className="w-1/2">
              <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="expiryDate">
                Expiry Date (MM/YY)
              </label>
              <input
                type="text"
                id="expiryDate"
                name="expiryDate"
                value={paymentData.expiryDate}
                onChange={handleInputChange}
                pattern="(0[1-9]|1[0-2])\/([0-9]{2})"
                placeholder="MM/YY"
                className="w-full px-3 py-2 border rounded focus:outline-none focus:border-blue-500"
                required
              />
            </div>

            <div className="w-1/2">
              <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="cvv">
                CVV
              </label>
              <input
                type="text"
                id="cvv"
                name="cvv"
                value={paymentData.cvv}
                onChange={handleInputChange}
                pattern="[0-9]{3,4}"
                maxLength="4"
                className="w-full px-3 py-2 border rounded focus:outline-none focus:border-blue-500"
                required
              />
            </div>
          </div>

          <div className="flex items-center justify-between">
            <button
              type="submit"
              className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 focus:outline-none focus:shadow-outline"
            >
              Complete Payment
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default MakePayment;
