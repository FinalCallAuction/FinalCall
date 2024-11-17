// src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './components/Home';
import Login from './components/Login';
import Register from './components/Register';
import ItemsPage from './components/ItemsPage';
import CreateItem from './components/CreateItem';
import ItemDetail from './components/ItemDetail';
import ChangePassword from './components/ChangePassword'; // Import the new component
import ChangeAddress from './components/ChangeAddress'; // Import the new component
import Profile from './components/Profile'; // Ensure you have a Profile component

import { AuthContext } from './context/AuthContext';

const App = () => {
  const { user } = React.useContext(AuthContext);

  return (
    <Router>
      <Navbar />
      <Routes>
        <Route
          path="/"
          element={user ? <Navigate to="/items" /> : <Home />}
        />
        <Route
          path="/login"
          element={!user ? <Login /> : <Navigate to="/items" />}
        />
        <Route
          path="/register"
          element={!user ? <Register /> : <Navigate to="/items" />}
        />
        <Route
          path="/items"
          element={user ? <ItemsPage /> : <Navigate to="/login" />}
        />
        <Route
          path="/create-item"
          element={user ? <CreateItem /> : <Navigate to="/login" />}
        />
        <Route
          path="/items/:id"
          element={user ? <ItemDetail /> : <Navigate to="/login" />}
        />
        <Route
          path="/change-password"
          element={user ? <ChangePassword /> : <Navigate to="/login" />}
        />
        <Route
          path="/change-address"
          element={user ? <ChangeAddress /> : <Navigate to="/login" />}
        />
        <Route
          path="/profile"
          element={user ? <Profile /> : <Navigate to="/login" />}
        />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
};

export default App;
