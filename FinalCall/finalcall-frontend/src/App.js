// src/App.js

import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import Register from './components/Register';
import ItemsPage from './components/ItemsPage';
import CreateItem from './components/CreateItem';
import { AuthContext } from './context/AuthContext';

function App() {
  const { user } = useContext(AuthContext);

  return (
    <Router>
      <Navbar />
      <Routes>
        {/* Redirect root to /items */}
        <Route path="/" element={<Navigate to="/items" />} />
        <Route path="/login" element={!user ? <Login /> : <Navigate to="/items" />} />
        <Route path="/signup" element={!user ? <Register /> : <Navigate to="/items" />} />
        <Route path="/items" element={<ItemsPage />} />
        <Route path="/create-item" element={user ? <CreateItem /> : <Navigate to="/login" />} />
        {/* Add a catch-all route for undefined paths */}
        <Route path="*" element={<Navigate to="/items" />} />
      </Routes>
    </Router>
  );
}

export default App;
