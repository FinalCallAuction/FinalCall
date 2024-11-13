// src/index.js

import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css'; // Ensure Tailwind CSS is imported
import App from './App';
import AuthProvider from './context/AuthContext';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>
);
