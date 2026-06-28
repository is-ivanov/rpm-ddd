import { z } from 'zod';

const personNameSchema = z.object({
  firstName: z.string(),
  middleName: z.string().nullable(),
  lastName: z.string(),
});

const userAuditSchema = z.object({
  createdAt: z.string(),
  createdBy: personNameSchema,
  updatedAt: z.string(),
  updatedBy: personNameSchema,
});

export const adminUsersSchema = z.array(
  z.object({
    userId: z.string(),
    name: personNameSchema,
    login: z.string(),
    email: z.string(),
    status: z.string(),
    audit: userAuditSchema,
  }),
);
