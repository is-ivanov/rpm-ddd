export interface AuthenticatedUser {
  readonly login: string;
  readonly email: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly timeZone: string;
}

export type CurrentUserResult =
  | { readonly authenticated: false }
  | { readonly authenticated: true; readonly user: AuthenticatedUser };
