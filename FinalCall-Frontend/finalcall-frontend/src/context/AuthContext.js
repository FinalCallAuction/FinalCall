// src/context/AuthContext.js

import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  // Function to retrieve the token from localStorage
  const getToken = () => {
    return localStorage.getItem('token');
  };

  // Function to handle login
  const login = async (username, password) => {
    try {
      const response = await axios.post(
        'http://localhost:8081/api/auth/login',
        { username, password },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      if (response.status === 200) {
        const data = response.data;
        localStorage.setItem('token', data.token); // Store the JWT token
        setUser(data.user); // Set the user in state
        return { success: true };
      } else {
        return { success: false, message: 'Login failed.' };
      }
    } catch (error) {
      console.error('Login Error:', error);
      const errorMsg =
        error.response && error.response.data
          ? typeof error.response.data === 'string'
            ? error.response.data
            : error.response.data.message
          : 'An error occurred during login.';
      return { success: false, message: errorMsg };
    }
  };

  // Function to handle registration
  const register = async (userDetails) => {
    try {
      const response = await axios.post(
        'http://localhost:8081/api/auth/register',
        userDetails,
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      if (response.status === 200) {
        const data = response.data;
        localStorage.setItem('token', data.token); // Store the JWT token
        setUser(data.user); // Set the user in state
        return { success: true };
      } else {
        return { success: false, message: 'Registration failed.' };
      }
    } catch (error) {
      console.error('Registration Error:', error);
      const errorMsg =
        error.response && error.response.data
          ? typeof error.response.data === 'string'
            ? error.response.data
            : error.response.data.message
          : 'An error occurred during registration.';
      return { success: false, message: errorMsg };
    }
  };

  // Function to handle logout
  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  useEffect(() => {
    // On component mount, decode the token and set the user if token exists
    const token = getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          username: payload.sub,
          id: payload.id, // Ensure your JWT includes 'id'
          email: payload.email, // Include email if available
          firstName: payload.firstName,
          lastName: payload.lastName,
          streetAddress: payload.streetAddress,
          province: payload.province,
          country: payload.country,
          postalCode: payload.postalCode,
          isSeller: payload.isSeller,
        });
      } catch (error) {
        console.error('Error decoding token:', error);
        logout();
      }
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
