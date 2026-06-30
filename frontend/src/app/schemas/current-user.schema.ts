import { z } from 'zod';

export const currentUserResponseSchema = z.object({
  login: z.string(),
  email: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  timeZone: z.string(),
});

export type CurrentUserResponse = z.infer<typeof currentUserResponseSchema>;
