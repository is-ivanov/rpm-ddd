// FIXTURE (Task 205) — proves the `no-restricted-syntax` guard STAYS GREEN.
//
// The compliant boundary pattern (schema validation) and an unrelated legitimate
// DOM `as` cast must NOT trip the guard. Linted explicitly via `eslint --no-ignore`
// to prove zero reports here. This file is eslint-ignored from the repo-wide run
// only because it lives beside the intentionally-failing invalid fixture.
import { z } from 'zod';

const userSchema = z.object({ id: z.string() });

// Compliant: validate the network body at the boundary instead of casting it.
export async function fetchUserValidated(response: Response): Promise<{ id: string }> {
  return userSchema.parse(await response.json());
}

// Legitimate, unrelated `as` cast (DOM narrowing) — the guard must stay narrow and
// not flag this. Proves the rule targets `.json()` results only, not every `as`.
export function readInputValue(el: Element): string {
  return (el as HTMLInputElement).value;
}
