import type {
  AtualizarStatusRequest,
  DashboardMetricsResponse,
  PageResponse,
  StatusVaga,
  VagaDetailResponse,
  VagaRequest,
  VagaResponse,
} from "./types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
const TOKEN_KEY = "horizon_token";

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  window.localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  window.localStorage.removeItem(TOKEN_KEY);
}

async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(`${API_URL}${path}`, { ...options, headers });

  if (!res.ok) {
    let message = `Erro ${res.status}`;
    try {
      const body = await res.json();
      message = body.message ?? JSON.stringify(body);
    } catch {
      // corpo vazio ou não-JSON (ex: 403 sem token válido)
    }
    throw new ApiError(res.status, message);
  }

  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export async function login(username: string, password: string) {
  return apiFetch<{ token: string }>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

export async function listVagas(params: {
  status?: StatusVaga;
  plataforma?: string;
  page?: number;
}) {
  const query = new URLSearchParams();
  if (params.status) query.set("status", params.status);
  if (params.plataforma) query.set("plataforma", params.plataforma);
  if (params.page !== undefined) query.set("page", String(params.page));

  const qs = query.toString();
  return apiFetch<PageResponse<VagaResponse>>(
    `/api/v1/vagas${qs ? `?${qs}` : ""}`,
  );
}

export async function createVaga(data: VagaRequest) {
  return apiFetch<VagaResponse>("/api/v1/vagas", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getVaga(id: number) {
  return apiFetch<VagaDetailResponse>(`/api/v1/vagas/${id}`);
}

export async function atualizarStatus(id: number, data: AtualizarStatusRequest) {
  return apiFetch<VagaResponse>(`/api/v1/vagas/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify(data),
  });
}

export async function deletarVaga(id: number) {
  return apiFetch<void>(`/api/v1/vagas/${id}`, { method: "DELETE" });
}

export async function getDashboardMetrics() {
  return apiFetch<DashboardMetricsResponse>("/api/v1/dashboard/metrics");
}
