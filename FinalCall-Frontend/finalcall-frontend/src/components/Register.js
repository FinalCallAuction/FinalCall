// src/components/Register.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    streetAddress: '',
    province: '',
    country: '',
    postalCode: '',
    isSeller: false,
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, type, checked, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await fetch('http://localhost:8081/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        alert('Registration successful! Please login.');
        navigate('/login');
      } else {
        const errMsg = await response.text();
        setError(errMsg);
      }
    } catch (err) {
      setError('Error registering user.');
    }
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Register</h1>
      {error && <div className="text-red-500 mb-4">{error}</div>}
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Username */}
        <input
          name="username"
          placeholder="Username"
          value={formData.username}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Password */}
        <input
          name="password"
          placeholder="Password"
          type="password"
          value={formData.password}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Email */}
        <input
          name="email"
          placeholder="Email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* First Name */}
        <input
          name="firstName"
          placeholder="First Name"
          value={formData.firstName}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Last Name */}
        <input
          name="lastName"
          placeholder="Last Name"
          value={formData.lastName}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Street Address */}
        <input
          name="streetAddress"
          placeholder="Street Address"
          value={formData.streetAddress}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Province */}
        <input
          name="province"
          placeholder="Province"
          value={formData.province}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Country */}
        <input
          name="country"
          placeholder="Country"
          value={formData.country}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Postal Code */}
        <input
          name="postalCode"
          placeholder="Postal Code"
          value={formData.postalCode}
          onChange={handleChange}
          className="border p-2 w-full"
          required
        />
        {/* Is Seller */}
        <div className="flex items-center">
          <input
            name="isSeller"
            type="checkbox"
            checked={formData.isSeller}
            onChange={handleChange}
            className="mr-2"
          />
          <label>Register as Seller</label>
        </div>
        {/* Submit Button */}
        <button
          type="submit"
          className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
        >
          Register
        </button>
      </form>
    </div>
  );
};

export default Register;
