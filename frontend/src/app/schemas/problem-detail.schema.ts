import { z } from 'zod';

export const problemFieldErrorSchema = z.object({
  property: z.string(),
  message: z.string(),
});

export const problemDetailSchema = z.object({
  type: z.string().optional(),
  detail: z.string().optional(),
  fieldErrors: z.array(problemFieldErrorSchema).readonly().optional(),
});

export type ProblemFieldError = z.infer<typeof problemFieldErrorSchema>;
export type ProblemDetail = z.infer<typeof problemDetailSchema>;
