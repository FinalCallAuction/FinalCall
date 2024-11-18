// src/components/ConfirmationPage.jsx

import React from 'react';
import { useLocation, useHistory, useNavigate } from 'react-router-dom';

const ConfirmationPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { success, details } = location.state || {};

  const handleBackToHome = () => {
    navigate('/');
  };

  if (success === undefined || details === undefined) {
    // Redirect to home if accessed directly without payment data
    navigate('/');
    return null;
  }

  return (
    <div className="container mx-auto p-4">
      {success ? (
        <div>
          <h1 className="text-2xl font-bold mb-4">Payment Successful!</h1>
          <p className="mb-2">Thank you for your purchase.</p>
          <p className="mb-2">Transaction ID: {details.id}</p>
          <p className="mb-2">
            Amount: ${(details.amount / 100).toFixed(2)}{' '}
            {details.currency.toUpperCase()}
          </p>
          <p className="mb-2">Status: {details.status}</p>
        </div>
      ) : (
        <div>
          <h1 className="text-2xl font-bold mb-4 text-red-600">Payment Failed</h1>
          <p className="mb-2">
            We're sorry, but your payment could not be processed.
          </p>
          <p className="mb-2">Error: {details}</p>
        </div>
      )}
      <button
        onClick={handleBackToHome}
        className="mt-4 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
      >
        Back to Home
      </button>
    </div>
  );
};

export default ConfirmationPage;
