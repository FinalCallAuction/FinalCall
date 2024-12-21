// src/components/payment/PaymentHistory.js
import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { authFetch } from '../utils/authFetch';

const PaymentHistory = () => {
  const { user, logout } = useContext(AuthContext);
  const [payments, setPayments] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPayments = async () => {
      try {
        const response = await authFetch(
          `http://localhost:8083/api/payments/user/${user.id}`,
          { method: 'GET' },
          logout
        );

        if (response.ok) {
          const data = await response.json();
          setPayments(data);
        } else {
          setError('Failed to fetch payment history');
        }
      } catch (err) {
        setError('An error occurred while fetching payment history');
      }
    };

    fetchPayments();
  }, [user.id, logout]);

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Payment History</h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4">
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      )}

      {payments.length === 0 ? (
        <p className="text-gray-600">No payment history found.</p>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Date
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Transaction ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {payments.map((payment) => (
                <tr key={payment.transactionId}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {new Date(payment.timestamp).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {payment.transactionId}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    ${payment.amount.toFixed(2)} {payment.currency}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        payment.status === 'SUCCESS'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {payment.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <Link
                      to={`/payment-confirmation/${payment.transactionId}`}
                      className="text-blue-600 hover:text-blue-900"
                    >
                      View Details
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default PaymentHistory;
