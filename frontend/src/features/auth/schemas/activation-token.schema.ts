import { z } from 'zod';

export const activationTokenResponseSchema = z.object({
  login: z.string(),
  email: z.string(),
});

export type ActivationTokenResponse = z.infer<typeof activationTokenResponseSchema>;
