// src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import Register from './components/Register';
import ItemsPage from './components/ItemsPage';
import CreateItem from './components/CreateItem';
import ItemDetail from './components/ItemDetail'; // Import the new component
import { AuthContext } from './context/AuthContext';

const App = () => {
  const { user } = React.useContext(AuthContext);

  return (
    <Router>
      <Navbar />
      <Routes>
        <Route
          path="/"
          element={user ? <Navigate to="/items" /> : <LandingPage />}
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
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
};

export default App;
