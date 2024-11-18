import React, { useContext, useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  const handleLogout = () => {
    logout();
    navigate('/'); // Redirect to landing page after logout
  };

  const toggleDropdown = () => {
    setDropdownOpen((prev) => !prev);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target)
      ) {
        setDropdownOpen(false);
      }
    };

    if (dropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [dropdownOpen]);

  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-4 py-3 flex justify-between items-center">
        <Link to="/" className="text-xl font-bold text-blue-600">
          FinalCall
        </Link>
        <div className="relative" ref={dropdownRef}>
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
              <Link
                to="/profile"
                className="mr-4 text-gray-700 hover:text-blue-600"
              >
                Profile
              </Link>
              <button
                onClick={toggleDropdown}
                className="relative text-gray-700 hover:text-blue-600 focus:outline-none"
              >
                {user.username} ({user.email})
                {/* Dropdown Icon */}
                <svg
                  className="w-4 h-4 inline ml-1"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              {dropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border rounded-md shadow-lg z-50">
                  <Link
                    to="/change-password"
                    className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                    onClick={() => setDropdownOpen(false)}
                  >
                    Change Password
                  </Link>
                  <Link
                    to="/change-address"
                    className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                    onClick={() => setDropdownOpen(false)}
                  >
                    Change Address
                  </Link>
                  <button
                    onClick={() => {
                      handleLogout();
                      setDropdownOpen(false);
                    }}
                    className="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100"
                  >
                    Log Out
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
