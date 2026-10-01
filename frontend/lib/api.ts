const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "/api";

type ApiEnvelope<T> = { data: T };
type ErrorEnvelope = { error?: { code?: string; message?: string } };

export class ApiError extends Error {
  constructor(message: string, public readonly status: number, public readonly code?: string) {
    super(message);
  }
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window === "undefined" ? null : localStorage.getItem("assetly_token");
  const headers = new Headers(options.headers);
  if (options.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ErrorEnvelope;
    throw new ApiError(body.error?.message ?? "요청을 처리하지 못했습니다.", response.status, body.error?.code);
  }
  return ((await response.json()) as ApiEnvelope<T>).data;
}

export const saveAccessToken = (token: string) => localStorage.setItem("assetly_token", token);
export const clearAccessToken = () => localStorage.removeItem("assetly_token");
