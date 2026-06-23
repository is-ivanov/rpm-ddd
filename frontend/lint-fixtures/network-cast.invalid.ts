// FIXTURE (Task 205) — proves the `no-restricted-syntax` guard FIRES.
//
// Every function below casts an UNVALIDATED network response body with `as`,
// which is exactly what the guard forbids. This file is eslint-ignored (see
// eslint.config.ts) so it never breaks `eslint .`; the rule is proven to report
// here by linting it explicitly with `eslint --no-ignore`.
interface User {
  id: string;
}

// `(await response.json()) as T` — the recurring real-world shape (parenthesised await).
export async function castParenthesisedAwait(response: Response): Promise<User> {
  return (await response.json()) as User;
}

// `await response.json() as T` — `as` binds to the `.json()` call before `await` (no parens).
export async function castUnparenthesisedAwait(response: Response): Promise<User> {
  return await (response.json() as Promise<User>);
}

// `response.json() as T` — the bare promise cast (no await).
export function castSyncJson(response: Response): Promise<User> {
  return response.json() as Promise<User>;
}
