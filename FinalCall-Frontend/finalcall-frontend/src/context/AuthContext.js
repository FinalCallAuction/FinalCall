// src/context/AuthContext.js

import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';
import {jwtDecode} from 'jwt-decode'; // Fixed import

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(getToken());

  // Function to retrieve the token from localStorage
  function getToken() {
    return localStorage.getItem('token');
  }

  // Function to decode JWT token and set user state
  const decodeAndSetUser = (token) => {
    try {
      const decoded = jwtDecode(token);
      if (decoded) {
        setUser({
          username: decoded.sub,
          id: decoded.id,
          email: decoded.email,
          firstName: decoded.firstName,
          lastName: decoded.lastName,
          streetAddress: decoded.streetAddress,
          province: decoded.province,
          country: decoded.country,
          postalCode: decoded.postalCode,
          isSeller: decoded.isSeller,
        });
      }
    } catch (error) {
      console.error('Invalid token:', error);
      logout();
    }
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
        setToken(data.token);
        decodeAndSetUser(data.token); // Decode and set the user
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
        setToken(data.token);
        decodeAndSetUser(data.token); // Decode and set the user
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
    setToken(null);
    setUser(null);
  };

  // Check token validity and set user on component mount or token change
  useEffect(() => {
    if (token) {
      const decoded = jwtDecode(token);
      if (decoded) {
        const currentTime = Date.now() / 1000;
        if (decoded.exp && decoded.exp < currentTime) {
          // Token expired
          console.warn('Token expired.');
          logout();
        } else {
          decodeAndSetUser(token);
        }
      } else {
        logout();
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
