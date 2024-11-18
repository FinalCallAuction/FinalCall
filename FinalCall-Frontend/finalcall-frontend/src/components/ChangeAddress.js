// src/components/ChangeAddress.js

import React, { useState, useContext, useEffect } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Select from 'react-select';
import { getCountryOptions, getRegionOptions } from '../utils/countries';
import { authFetch } from '../utils/authFetch';

const ChangeAddress = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [address, setAddress] = useState({
    streetAddress: '',
    country: null,
    province: null,
    postalCode: '',
  });
  const [countryOptions] = useState(getCountryOptions());
  const [regionOptions, setRegionOptions] = useState([]);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    // Fetch current address details
    const fetchAddress = async () => {
      try {
        const response = await authFetch(`http://localhost:8081/api/user/${user.id}`, {
          method: 'GET',
        });

        if (response.ok) {
          const data = await response.json();
          setAddress({
            streetAddress: data.streetAddress,
            country: countryOptions.find((c) => c.value === data.country) || null,
            province: null, // Will set after regionOptions are loaded
            postalCode: data.postalCode,
          });
          if (data.country) {
            const regions = getRegionOptions(data.country);
            setRegionOptions(regions);
            setAddress((prev) => ({
              ...prev,
              province: regions.find((r) => r.value === data.province) || null,
            }));
          }
        } else {
          const errorMsg = await response.text();
          setError(`Error: ${errorMsg}`);
        }
      } catch (err) {
        setError('An error occurred while fetching address details.');
        console.error('Fetch Address Error:', err);
      }
    };

    fetchAddress();
  }, [user.id, countryOptions]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setAddress({
      ...address,
      [name]: value,
    });
  };

  const handleCountryChange = (selectedOption) => {
    setAddress({
      ...address,
      country: selectedOption,
      province: null, // Reset province when country changes
    });
    if (selectedOption) {
      setRegionOptions(getRegionOptions(selectedOption.value));
    } else {
      setRegionOptions([]);
    }
  };

  const handleProvinceChange = (selectedOption) => {
    setAddress({
      ...address,
      province: selectedOption,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    // Basic validation
    if (
      !address.streetAddress ||
      !address.country ||
      !address.province ||
      !address.postalCode
    ) {
      setError('Please fill in all required fields.');
      return;
    }

    // Prepare data to send
    const updatedAddress = {
      streetAddress: address.streetAddress,
      province: address.province.value,
      country: address.country.value,
      postalCode: address.postalCode,
      isSeller: user.isSeller, // Preserve existing isSeller status
    };

    try {
      const response = await authFetch(`http://localhost:8081/api/user/${user.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedAddress),
      });

      if (response.ok) {
        setMessage('Address updated successfully.');
        // Optionally, redirect or perform other actions
        navigate('/profile'); // Assuming you have a profile page
      } else {
        const errorMsg = await response.text();
        setError(`Error: ${errorMsg}`);
      }
    } catch (err) {
      setError('An error occurred while updating the address.');
      console.error('Change Address Error:', err);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-yellow-50">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded shadow-md w-full max-w-lg"
      >
        <h2 className="text-2xl font-bold mb-6">Change Address</h2>

        {message && (
          <div
            className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4"
            role="alert"
          >
            <strong className="font-bold">Success:</strong>
            <span className="block sm:inline"> {message}</span>
          </div>
        )}

        {error && (
          <div
            className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
            role="alert"
          >
            <strong className="font-bold">Error:</strong>
            <span className="block sm:inline"> {error}</span>
          </div>
        )}

        {/* Street Address */}
        <div className="mb-4">
          <label className="block text-gray-700">Street Address:</label>
          <input
            type="text"
            name="streetAddress"
            className="w-full px-3 py-2 border rounded mt-1"
            value={address.streetAddress}
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
            value={address.country}
            onChange={handleCountryChange}
            placeholder="Select your country"
            isClearable
          />
        </div>

        {/* Province/State */}
        <div className="mb-6">
          <label className="block text-gray-700">Province/State:</label>
          <Select
            options={regionOptions}
            value={address.province}
            onChange={handleProvinceChange}
            placeholder="Select your province/state"
            isClearable
            isDisabled={!address.country}
          />
        </div>

        {/* Postal Code */}
        <div className="mb-6">
          <label className="block text-gray-700">Postal Code:</label>
          <input
            type="text"
            name="postalCode"
            className="w-full px-3 py-2 border rounded mt-1"
            value={address.postalCode}
            onChange={handleChange}
            required
            placeholder="Enter your postal code"
          />
        </div>

        <button
          type="submit"
          className="w-full bg-yellow-500 text-white py-2 rounded hover:bg-yellow-600"
        >
          Update Address
        </button>
      </form>
    </div>
  );
};

export default ChangeAddress;
