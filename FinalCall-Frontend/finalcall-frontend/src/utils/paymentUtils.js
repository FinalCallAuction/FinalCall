// src/utils/paymentUtils.js

// Format credit card number to show only last 4 digits
export const formatCreditCard = (number) => {
  return `**** **** **** ${number.slice(-4)}`;
};

// Format currency amount with proper decimals and symbol
export const formatCurrency = (amount, currency = 'USD') => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount);
};

// Validate credit card details
export const validateCreditCard = {
  number: (number) => /^[0-9]{16}$/.test(number),
  expiry: (expiry) => {
    if (!/^(0[1-9]|1[0-2])\/([0-9]{2})$/.test(expiry)) return false;
    
    const [month, year] = expiry.split('/');
    const expDate = new Date(2000 + parseInt(year), parseInt(month) - 1);
    const today = new Date();
    return expDate > today;
  },
  cvv: (cvv) => /^[0-9]{3,4}$/.test(cvv),
  cardHolderName: (name) => /^[a-zA-Z\s]{2,50}$/.test(name)
};

// Format date for displaying payment timestamps
export const formatPaymentDate = (timestamp) => {
  return new Date(timestamp).toLocaleString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// Get payment status display properties
export const getPaymentStatusProperties = (status) => {
  const statusMap = {
    SUCCESS: {
      color: 'green',
      bgColor: 'bg-green-100',
      textColor: 'text-green-800',
      icon: '✓',
      label: 'Successful'
    },
    FAILED: {
      color: 'red',
      bgColor: 'bg-red-100',
      textColor: 'text-red-800',
      icon: '×',
      label: 'Failed'
    },
    PROCESSING: {
      color: 'yellow',
      bgColor: 'bg-yellow-100',
      textColor: 'text-yellow-800',
      icon: '⋯',
      label: 'Processing'
    }
  };

  return statusMap[status] || statusMap.FAILED;
};

// Generate mock card for testing
export const generateTestCard = () => {
  const currentYear = new Date().getFullYear();
  const nextYear = currentYear + 1;
  return {
    number: '4532015112830366', // Test valid card number
    expiry: `12/${nextYear.toString().slice(-2)}`, // MM/YY format for next year
    cvv: '123',
    cardHolderName: 'Test User'
  };
};

// Check if payment amount is within acceptable range
export const validatePaymentAmount = (amount) => {
  const minAmount = 0.01;
  const maxAmount = 1000000.00;
  const numAmount = parseFloat(amount);
  
  return {
    isValid: !isNaN(numAmount) && numAmount >= minAmount && numAmount <= maxAmount,
    message: !isNaN(numAmount) 
      ? numAmount < minAmount 
        ? 'Amount must be at least $0.01'
        : numAmount > maxAmount 
          ? 'Amount exceeds maximum limit'
          : ''
      : 'Please enter a valid amount'
  };
};

// Error message handler for payment API responses
export const getPaymentErrorMessage = (error) => {
  const errorMessages = {
    'INVALID_CARD': 'The card information provided is invalid.',
    'EXPIRED_CARD': 'This card has expired.',
    'INSUFFICIENT_FUNDS': 'Insufficient funds on the card.',
    'PROCESSING_ERROR': 'Error processing payment. Please try again.',
    'DEFAULT': 'An error occurred during payment processing.'
  };

  return errorMessages[error] || errorMessages.DEFAULT;
};
