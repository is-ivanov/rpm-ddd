export interface AuthenticatedUser {
  readonly login: string;
  readonly email: string;
}

export type CurrentUserResult =
  | { readonly authenticated: false }
  | { readonly authenticated: true; readonly user: AuthenticatedUser };
