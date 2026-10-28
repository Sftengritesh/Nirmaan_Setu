/**
 * Client-side rate limiter to prevent abuse and spamming of sensitive operations
 */

interface RateLimitRecord {
  count: number;
  resetAt: number;
}

const rateLimitStore = new Map<string, RateLimitRecord>();

export const checkRateLimit = (actionKey: string, maxAttempts = 5, windowMs = 60000): { allowed: boolean; waitSeconds?: number } => {
  const now = Date.now();
  const record = rateLimitStore.get(actionKey);

  if (!record || now > record.resetAt) {
    rateLimitStore.set(actionKey, {
      count: 1,
      resetAt: now + windowMs,
    });
    return { allowed: true };
  }

  if (record.count >= maxAttempts) {
    const waitSeconds = Math.ceil((record.resetAt - now) / 1000);
    return { allowed: false, waitSeconds };
  }

  record.count += 1;
  return { allowed: true };
};
