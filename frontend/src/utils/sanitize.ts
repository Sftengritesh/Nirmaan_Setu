import DOMPurify from 'dompurify';

/**
 * Sanitize untrusted user input string against XSS injections
 */
export const sanitizeString = (input: string): string => {
  if (!input) return '';
  return DOMPurify.sanitize(input.trim());
};

/**
 * Sanitize an entire object's string properties recursively
 */
export const sanitizeObject = <T extends Record<string, any>>(obj: T): T => {
  const result: Record<string, any> = {};
  for (const [key, value] of Object.entries(obj)) {
    if (typeof value === 'string') {
      result[key] = sanitizeString(value);
    } else if (value && typeof value === 'object' && !Array.isArray(value)) {
      result[key] = sanitizeObject(value);
    } else {
      result[key] = value;
    }
  }
  return result as T;
};
