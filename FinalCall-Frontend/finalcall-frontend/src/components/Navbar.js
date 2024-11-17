// src/components/Navbar.js

import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/'); // Redirect to landing page after logout
  };

  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-4 py-3 flex justify-between items-center">
        <Link to="/" className="text-xl font-bold text-blue-600">
          FinalCall
        </Link>
        <div>
          {!user ? (
            <>
              <Link
                to="/login"
                className="mr-4 text-gray-700 hover:text-blue-600"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="mr-4 text-gray-700 hover:text-green-600"
              >
                Register
              </Link>
              <Link
                to="/items"
                className="text-gray-700 hover:text-gray-900"
              >
                Browse Items
              </Link>
            </>
          ) : (
            <>
              <Link
                to="/create-item"
                className="mr-4 text-gray-700 hover:text-yellow-600"
              >
                List Item
              </Link>
              <span className="mr-4 text-gray-700">
                {user.username} ({user.email})
              </span>
              <button
                onClick={handleLogout}
                className="text-gray-700 hover:text-red-600"
              >
                Logout
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;