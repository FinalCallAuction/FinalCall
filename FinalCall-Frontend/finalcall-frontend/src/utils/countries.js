// src/utils/countries.js

import { allCountries } from 'country-region-data';

// Function to format countries for react-select
export const getCountryOptions = () => {
  console.log('All Countries:', allCountries); // Debugging line
  if (!allCountries || allCountries.length === 0) {
    console.error('No countries data available.');
    return [];
  }

  return allCountries.map((country) => ({
    label: country[0] || 'Unknown Country', // country[0] is the country name
    value: country[0] || 'Unknown Country', // country[0] is the country name
    regions: country[2] ? country[2].map((region) => region[0]) : [],
  }));
};

// Function to get regions based on selected country
export const getRegionOptions = (countryName) => {
  if (!countryName) return [];

  const country = allCountries.find(
    (country) => country[0] === countryName
  );

  if (!country) {
    console.error(`Country "${countryName}" not found.`);
    return [];
  }

  if (!country[2] || country[2].length === 0) {
    console.warn(`No regions available for country "${countryName}".`);
    return [];
  }

  return country[2].map((region) => ({
    label: region[0], // region[0] is the region name
    value: region[0],
  }));
};
