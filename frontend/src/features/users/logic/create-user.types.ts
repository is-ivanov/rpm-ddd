export interface CreateUserRequest {
  readonly firstName: string;
  readonly middleName: string | null;
  readonly lastName: string;
  readonly login: string;
  readonly email: string;
  readonly timeZone: string;
}
