// src/components/Navbar.js

import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import NotificationBell from './NotificationBell';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const displayName = user?.username || user?.email || 'Guest';

  return (
    <nav className="bg-gray-800 text-white p-4 flex items-center justify-between">
      <Link to="/" className="font-bold text-xl">
        FinalCall
      </Link>
      <div className="flex items-center space-x-4">
        {user && (
          <>
            <Link to="/my-bids" className="hover:underline">
              My Bids
            </Link>
            <Link to="/create-item" className="hover:underline">
              List Item
            </Link>
            <NotificationBell />
          </>
        )}
        {!user ? (
          <>
            <Link to="/login" className="hover:underline">
              Login
            </Link>
            <Link to="/register" className="hover:underline">
              Register
            </Link>
          </>
        ) : (
          <>
            <Link
              to={`/profile/${user.id}`}
              className="hover:underline cursor-pointer"
            >
              Hello, {displayName}
            </Link>
            <button
              onClick={handleLogout}
              className="bg-red-500 px-3 py-1 rounded hover:bg-red-600"
            >
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
