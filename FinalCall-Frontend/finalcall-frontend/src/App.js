// src/App.js
import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthContext } from './context/AuthContext';
import Navbar from './components/Navbar';
import ItemsPage from './components/ItemsPage';
import Login from './components/Login';
import Register from './components/Register';
import Callback from './components/Callback';
import Profile from './components/Profile';
import CreateItem from './components/CreateItem';
import ItemDetail from './components/ItemDetail';
import ChangePassword from './components/ChangePassword';
import ChangeAddress from './components/ChangeAddress';
import MyBids from './components/MyBids';
import PrivateRoute from './components/PrivateRoute';
import MakePayment from './components/payment/MakePayment';
import PaymentConfirmation from './components/payment/PaymentConfirmation';
import PaymentHistory from './components/payment/PaymentHistory';

const App = () => {
  const { user } = useContext(AuthContext);

  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<ItemsPage />} />
        <Route path="/items" element={<ItemsPage />} />
        <Route path="/items/:id" element={<ItemDetail />} />
        <Route path="/login" element={!user ? <Login /> : <Navigate to="/" />} />
        <Route path="/register" element={!user ? <Register /> : <Navigate to="/" />} />
        <Route path="/callback" element={<Callback />} />
        <Route path="/profile/:userId" element={<Profile />} />
        <Route path="/change-address" element={user ? <ChangeAddress /> : <Navigate to="/login" />} />
        <Route path="/change-password" element={user ? <ChangePassword /> : <Navigate to="/login" />} />
        <Route path="/my-bids" element={user ? <MyBids /> : <Navigate to="/login" />} />
        <Route path="/create-item" element={user ? <CreateItem /> : <Navigate to="/login" />} />
        <Route path="/make-payment/:auctionId" element={<PrivateRoute><MakePayment /></PrivateRoute>} />
        <Route path="/payment-confirmation/:transactionId" element={<PrivateRoute><PaymentConfirmation /></PrivateRoute>} />
        <Route path="/payment-history" element={<PrivateRoute><PaymentHistory /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
};

export default App;
