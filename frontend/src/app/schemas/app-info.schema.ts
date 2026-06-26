import { z } from 'zod';

export const appInfoResponseSchema = z.object({
  build: z.object({ version: z.string(), time: z.string() }),
  git: z.object({ commit: z.object({ id: z.string() }) }),
});

export type AppInfoResponse = z.infer<typeof appInfoResponseSchema>;
