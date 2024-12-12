// src/App.js
import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthContext } from './context/AuthContext';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import Register from './components/Register';
import Callback from './components/Callback';
import ItemsPage from './components/ItemsPage'; // Provided
// Import other components as needed

const App = () => {
  const { user } = useContext(AuthContext);

  return (
    <Router>
      <Navbar />
      <Routes>
        {/* Public Route: ItemsPage accessible without login */}
        <Route path="/" element={<ItemsPage />} />
        <Route path="/items" element={<ItemsPage />} />

        {/* Authentication Routes */}
        <Route path="/login" element={!user ? <Login /> : <Navigate to="/" />} />
        <Route path="/register" element={!user ? <Register /> : <Navigate to="/" />} />
        <Route path="/callback" element={<Callback />} />

        {/* Optional Landing Page */}
        <Route path="/landing" element={<LandingPage />} />

        {/* Protected Routes (if any) */}
        {/* Example:
        <Route path="/profile" element={user ? <Profile /> : <Navigate to="/login" />} />
        */}

        {/* Catch-All Route */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
};

export default App;
