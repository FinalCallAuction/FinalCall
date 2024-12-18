import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import './index.css'; // Include your CSS (e.g., Tailwind CSS)

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <AuthProvider>
    <NotificationProvider>
      <App />
    </NotificationProvider>
  </AuthProvider>
);