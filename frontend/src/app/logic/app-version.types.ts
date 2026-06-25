export interface AppInfo {
  readonly build: { readonly version: string; readonly time: string };
  readonly git: { readonly commit: { readonly id: string } };
}

export interface AppVersion {
  readonly version: string;
  readonly commit: string;
  readonly buildTime: string;
}
