// src/context/AuthContext.js
import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = sessionStorage.getItem('finalcall_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  useEffect(() => {
    if (user) {
      sessionStorage.setItem('finalcall_user', JSON.stringify(user));
    } else {
      sessionStorage.removeItem('finalcall_user');
    }
  }, [user]);

  const logout = () => {
  // Clear everything 
  setUser(null);
  sessionStorage.clear();  // Clear all sessionStorage
  localStorage.clear();    // Clear all localStorage
  
  // Clear any specific items if needed
  sessionStorage.removeItem('finalcall_user');
  localStorage.removeItem('oauth_state');

  // Clear any cookies that might be stored
  document.cookie.split(";").forEach(function(c) { 
    document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/"); 
  });
};

  return (
    <AuthContext.Provider value={{ user, setUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
};