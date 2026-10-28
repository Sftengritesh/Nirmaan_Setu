/**
 * Form validation utilities for production application
 */

// Indian Mobile Number (+91 followed by 10 digits, or 10 digits starting with 6-9)
export const validateIndianPhone = (phone: string): { isValid: boolean; formatted: string; error?: string } => {
  const cleaned = phone.replace(/[\s-]/g, '');
  const regex = /^(?:\+91|91)?[6-9]\d{9}$/;
  if (!cleaned) {
    return { isValid: false, formatted: '', error: 'Phone number is required' };
  }
  if (!regex.test(cleaned)) {
    return { isValid: false, formatted: cleaned, error: 'Enter a valid 10-digit Indian phone number starting with 6-9' };
  }
  // Standardize to +91XXXXXXXXXX
  const digitsOnly = cleaned.slice(-10);
  return { isValid: true, formatted: `+91${digitsOnly}` };
};

// Standard Email validation
export const validateEmail = (email: string): { isValid: boolean; error?: string } => {
  if (!email || !email.trim()) {
    return { isValid: false, error: 'Email address is required' };
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email.trim())) {
    return { isValid: false, error: 'Enter a valid email address' };
  }
  return { isValid: true };
};

// Indian GSTIN validation (15 characters: 2 state digits + 10 PAN + 1 entity + 1 'Z' + 1 checksum)
export const validateGSTIN = (gst: string): { isValid: boolean; error?: string } => {
  if (!gst) return { isValid: true }; // Optional if not provided
  const gstRegex = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;
  if (!gstRegex.test(gst.toUpperCase().trim())) {
    return { isValid: false, error: 'Invalid GSTIN format (e.g. 27ABCDE1234F1Z5)' };
  }
  return { isValid: true };
};

// Indian PAN validation (10 characters: 5 letters + 4 digits + 1 letter)
export const validatePAN = (pan: string): { isValid: boolean; error?: string } => {
  if (!pan) return { isValid: true }; // Optional
  const panRegex = /^[A-Z]{5}[0-9]{4}[A-Z]{1}$/;
  if (!panRegex.test(pan.toUpperCase().trim())) {
    return { isValid: false, error: 'Invalid PAN format (e.g. ABCDE1234F)' };
  }
  return { isValid: true };
};

// Positive numeric amount validation
export const validateAmount = (amount: number | string, min = 0): { isValid: boolean; error?: string } => {
  const num = typeof amount === 'number' ? amount : parseFloat(amount);
  if (isNaN(num)) {
    return { isValid: false, error: 'Please enter a valid numeric value' };
  }
  if (num < min) {
    return { isValid: false, error: `Value must be at least ₹${min}` };
  }
  return { isValid: true };
};

// Required string validation
export const validateRequired = (val: string, fieldName: string, minLength = 2): { isValid: boolean; error?: string } => {
  if (!val || val.trim().length < minLength) {
    return { isValid: false, error: `${fieldName} is required (minimum ${minLength} characters)` };
  }
  return { isValid: true };
};
