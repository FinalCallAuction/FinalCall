// src/index.js

import React from 'react';
import ReactDOM from 'react-dom/client'; // ReactDOM.createRoot for React 18
import './index.css'; // Ensure Tailwind CSS is imported
import App from './App';
import AuthProvider from './context/AuthContext';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';

// Stripe configuration
const stripePromise = loadStripe('pk_test_51QMGVCIXrz0stHswLdwsBMSwlII9dovm2NIF5KjxJmjsrZMVgHpc2QMnDq7B9kZUEUra72gcuyif19ChOV5fvLoR003eW8kQfk');

// Correct usage of ReactDOM.createRoot
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <AuthProvider>
      <Elements stripe={stripePromise}>
        <App />
      </Elements>
    </AuthProvider>
  </React.StrictMode>
);
