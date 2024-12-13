// src/components/CountdownTimer.js
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';

/**
 * CountdownTimer Component
 * Displays the remaining time in HH:MM:SS format.
 *
 * Props:
 * - endTime (string): ISO 8601 formatted date-time string indicating auction end time.
 */
const CountdownTimer = ({ endTime }) => {
  const calculateTimeLeft = () => {
    const now = new Date();
    const auctionEnd = new Date(endTime);
    const difference = auctionEnd - now;

    if (difference <= 0) {
      return null; // Auction ended
    }

    const timeLeft = {
      hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
      minutes: Math.floor((difference / 1000 / 60) % 60),
      seconds: Math.floor((difference / 1000) % 60),
    };

    return timeLeft;
  };

  const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

  useEffect(() => {
    // Update the countdown every second
    const timer = setInterval(() => {
      setTimeLeft(calculateTimeLeft());
    }, 1000);

    // Cleanup the interval on component unmount
    return () => clearInterval(timer);
  }, [endTime]);

  // Format the time left as HH:MM:SS
  const formatTime = (time) => {
    const { hours, minutes, seconds } = time;
    const pad = (num) => String(num).padStart(2, '0');
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  };

  return (
    <div className="mt-2">
      {timeLeft ? (
        <span className="text-green-600 font-semibold">
          Time Left: {formatTime(timeLeft)}
        </span>
      ) : (
        <span className="text-red-600 font-semibold">Auction Ended</span>
      )}
    </div>
  );
};

CountdownTimer.propTypes = {
  endTime: PropTypes.string.isRequired,
};

export default CountdownTimer;
