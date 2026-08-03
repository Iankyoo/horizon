"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { ApiError, login, setToken } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const { token } = await login(username, password);
      setToken(token);
      router.push("/vagas");
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : "Não foi possível entrar.");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="flex min-h-[calc(100vh-73px)] items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <div className="mb-6 h-1 w-16 rounded-full bg-gradient-to-r from-dawn to-gold" />
        <h1 className="font-display text-3xl font-bold text-ink">Bem-vindo de volta</h1>
        <p className="mt-2 text-sm text-ink-soft">
          Entre para acompanhar o funil das suas candidaturas.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
          <div>
            <label htmlFor="username" className="block font-mono text-xs uppercase tracking-wider text-ink-soft">
              Usuário
            </label>
            <input
              id="username"
              type="text"
              autoComplete="username"
              required
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              className="mt-1 w-full rounded-md border border-line bg-surface px-3 py-2 text-ink outline-none focus:border-dawn focus:ring-1 focus:ring-dawn"
            />
          </div>

          <div>
            <label htmlFor="password" className="block font-mono text-xs uppercase tracking-wider text-ink-soft">
              Senha
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="mt-1 w-full rounded-md border border-line bg-surface px-3 py-2 text-ink outline-none focus:border-dawn focus:ring-1 focus:ring-dawn"
            />
          </div>

          {erro && (
            <p role="alert" className="text-sm text-amber">
              {erro}
            </p>
          )}

          <button
            type="submit"
            disabled={carregando}
            className="mt-2 rounded-md bg-ink px-4 py-2 font-medium text-paper transition hover:bg-dawn disabled:opacity-50"
          >
            {carregando ? "Entrando…" : "Entrar"}
          </button>
        </form>
      </div>
    </div>
  );
}
