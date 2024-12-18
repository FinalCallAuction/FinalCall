export const formatCreditCard = (number) => {
  return `**** **** **** ${number.slice(-4)}`;
};

export const validateCreditCard = {
  number: (number) => /^[0-9]{16}$/.test(number),
  expiry: (expiry) => /^(0[1-9]|1[0-2])\/([0-9]{2})$/.test(expiry),
  cvv: (cvv) => /^[0-9]{3,4}$/.test(cvv)
};
