import React, { createContext, useState, useEffect } from 'react';

// Create the AuthContext
export const AuthContext = createContext();

// Create the AuthProvider component
const AuthProvider = ({ children }) => {
  const [auth, setAuth] = useState(() => {
    const storedAuth = JSON.parse(localStorage.getItem('auth'));
    return storedAuth || { user: null, token: null };
  });

  // Function to handle login
  const login = (userData, tokenData) => {
    const authData = { user: userData, token: tokenData };
    setAuth(authData);
    localStorage.setItem('auth', JSON.stringify(authData));
  };

  // Function to handle logout
  const logout = () => {
    setAuth({ user: null, token: null });
    localStorage.removeItem('auth');
  };

  return (
    <AuthContext.Provider value={{ ...auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
