// src/components/Register.js

import React, { useState, useContext, useEffect } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Select from 'react-select';
import { getCountryOptions, getRegionOptions } from '../utils/countries';

const Register = () => {
  const { register } = useContext(AuthContext);
  const navigate = useNavigate(); // To redirect after registration
  const [details, setDetails] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    streetAddress: '',
    country: null, // Using react-select's option object
    province: null, // Using react-select's option object
    postalCode: '',
    isSeller: false,
  });
  const [countryOptions, setCountryOptions] = useState(getCountryOptions()); // Ensure setCountryOptions is defined
  const [regionOptions, setRegionOptions] = useState([]);

  const [error, setError] = useState('');

  useEffect(() => {
    // If you need to fetch country options from an API or perform any side effects, do it here.
    // Since getCountryOptions is a utility function, and already used in useState, this might be redundant.
    // However, if getCountryOptions is asynchronous, adjust accordingly.

    // Example if getCountryOptions is synchronous:
    const countries = getCountryOptions();
    console.log('Country Options:', countries); // Debugging line
    setCountryOptions(countries);
  }, []); // Empty dependency array ensures this runs once on mount

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setDetails({
      ...details,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleCountryChange = (selectedOption) => {
    setDetails({
      ...details,
      country: selectedOption,
      province: null, // Reset province when country changes
    });
    if (selectedOption) {
      const regions = getRegionOptions(selectedOption.value);
      setRegionOptions(regions);
    } else {
      setRegionOptions([]);
    }
  };

  const handleProvinceChange = (selectedOption) => {
    setDetails({
      ...details,
      province: selectedOption,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Basic validation
    if (
      !details.username ||
      !details.email ||
      !details.password ||
      !details.firstName ||
      !details.lastName ||
      !details.streetAddress ||
      !details.country ||
      (regionOptions.length > 0 && !details.province) || // Only require province if regions exist
      !details.postalCode
    ) {
      setError('Please fill in all required fields.');
      return;
    }

    // Prepare data to send
    const userDetails = {
      username: details.username,
      email: details.email,
      password: details.password,
      firstName: details.firstName,
      lastName: details.lastName,
      streetAddress: details.streetAddress, // Only this field
      province: details.province ? details.province.value : '',
      country: details.country.value,
      postalCode: details.postalCode,
      isSeller: details.isSeller,
    };

    console.log('User Details:', userDetails); // Debugging line

    const result = await register(userDetails);
    if (result.success) {
      // Redirect to items page
      navigate('/items');
    } else {
      setError(result.message);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-green-50 p-4">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded shadow-md w-full max-w-4xl"
      >
        <h2 className="text-2xl font-bold mb-6">Register</h2>

        {error && (
          <div
            className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
            role="alert"
          >
            <strong className="font-bold">Error:</strong>
            <span className="block sm:inline"> {error}</span>
          </div>
        )}

        {/* Two-Column Layout */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Left Column */}
          <div>
            {/* Username */}
            <div className="mb-4">
              <label className="block text-gray-700">Username:</label>
              <input
                type="text"
                name="username"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.username}
                onChange={handleChange}
                required
                placeholder="Choose a username"
              />
            </div>

            {/* Email */}
            <div className="mb-4">
              <label className="block text-gray-700">Email:</label>
              <input
                type="email"
                name="email"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.email}
                onChange={handleChange}
                required
                placeholder="Enter your email"
              />
            </div>

            {/* Password */}
            <div className="mb-4">
              <label className="block text-gray-700">Password:</label>
              <input
                type="password"
                name="password"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.password}
                onChange={handleChange}
                required
                placeholder="Create a password"
              />
            </div>

            {/* First Name */}
            <div className="mb-4">
              <label className="block text-gray-700">First Name:</label>
              <input
                type="text"
                name="firstName"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.firstName}
                onChange={handleChange}
                required
                placeholder="Enter your first name"
              />
            </div>
          </div>

          {/* Right Column */}
          <div>
            {/* Last Name */}
            <div className="mb-4">
              <label className="block text-gray-700">Last Name:</label>
              <input
                type="text"
                name="lastName"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.lastName}
                onChange={handleChange}
                required
                placeholder="Enter your last name"
              />
            </div>

            {/* Street Address */}
            <div className="mb-4">
              <label className="block text-gray-700">Street Address:</label>
              <input
                type="text"
                name="streetAddress"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.streetAddress}
                onChange={handleChange}
                required
                placeholder="Enter your street address"
              />
            </div>

            {/* Country */}
            <div className="mb-4">
              <label className="block text-gray-700">Country:</label>
              <Select
                options={countryOptions}
                value={details.country}
                onChange={handleCountryChange}
                placeholder="Select your country"
                isClearable
              />
            </div>

            {/* Province/State */}
            <div className="mb-4">
              <label className="block text-gray-700">Province/State:</label>
              <Select
                options={regionOptions}
                value={details.province}
                onChange={handleProvinceChange}
                placeholder={
                  regionOptions.length > 0
                    ? 'Select your province/state'
                    : 'No provinces/states available'
                }
                isClearable
                isDisabled={regionOptions.length === 0}
              />
            </div>

            {/* Postal Code */}
            <div className="mb-6">
              <label className="block text-gray-700">Postal Code:</label>
              <input
                type="text"
                name="postalCode"
                className="w-full px-3 py-2 border rounded mt-1"
                value={details.postalCode}
                onChange={handleChange}
                required
                placeholder="Enter your postal code"
              />
            </div>

            {/* Is Seller */}
            <div className="mb-6 flex items-center">
              <input
                type="checkbox"
                name="isSeller"
                id="isSeller"
                checked={details.isSeller}
                onChange={handleChange}
                className="mr-2"
              />
              <label htmlFor="isSeller" className="text-gray-700">
                Register as a Seller
              </label>
            </div>
          </div>
        </div>

        <button
          type="submit"
          className="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600"
        >
          Register
        </button>
      </form>
    </div>
  );
};

export default Register;
