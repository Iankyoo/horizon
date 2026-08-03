"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState, type FormEvent } from "react";
import { ApiError, createVaga, getToken, listVagas } from "@/lib/api";
import { STATUS_COR, STATUS_LABEL } from "@/lib/status";
import { STATUS_ORDEM, type StatusVaga, type VagaResponse } from "@/lib/types";

export default function VagasPage() {
  const router = useRouter();
  const [vagas, setVagas] = useState<VagaResponse[]>([]);
  const [total, setTotal] = useState(0);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [filtroStatus, setFiltroStatus] = useState<StatusVaga | "">("");
  const [filtroPlataforma, setFiltroPlataforma] = useState("");

  const [formAberto, setFormAberto] = useState(false);
  const [empresa, setEmpresa] = useState("");
  const [cargo, setCargo] = useState("");
  const [plataforma, setPlataforma] = useState("");
  const [link, setLink] = useState("");
  const [salvando, setSalvando] = useState(false);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const pagina = await listVagas({
        status: filtroStatus || undefined,
        plataforma: filtroPlataforma || undefined,
      });
      setVagas(pagina.content);
      setTotal(pagina.totalElements);
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : "Não foi possível carregar as vagas.");
    } finally {
      setCarregando(false);
    }
  }, [filtroStatus, filtroPlataforma]);

  useEffect(() => {
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    // Busca client-side deliberada (token só existe no browser) — ver
    // lib/api.ts. Não há como derivar isso durante o render.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    carregar();
  }, [carregar, router]);

  async function handleCriar(event: FormEvent) {
    event.preventDefault();
    setSalvando(true);
    setErro(null);
    try {
      await createVaga({
        empresa,
        cargo,
        plataforma: plataforma || undefined,
        link: link || undefined,
      });
      setEmpresa("");
      setCargo("");
      setPlataforma("");
      setLink("");
      setFormAberto(false);
      await carregar();
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : "Não foi possível criar a vaga.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-6 py-10">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink">Vagas</h1>
          <p className="mt-1 text-sm text-ink-soft">
            {total} candidatura{total === 1 ? "" : "s"} no funil
          </p>
        </div>
        <button
          type="button"
          onClick={() => setFormAberto((v) => !v)}
          className="rounded-md bg-ink px-4 py-2 text-sm font-medium text-paper transition hover:bg-dawn"
        >
          {formAberto ? "Cancelar" : "+ Nova vaga"}
        </button>
      </div>

      {formAberto && (
        <form
          onSubmit={handleCriar}
          className="mt-6 grid grid-cols-1 gap-4 rounded-lg border border-line bg-surface p-6 sm:grid-cols-2"
        >
          <Campo label="Empresa" required value={empresa} onChange={setEmpresa} />
          <Campo label="Cargo" required value={cargo} onChange={setCargo} />
          <Campo label="Plataforma" value={plataforma} onChange={setPlataforma} placeholder="Gupy, LinkedIn…" />
          <Campo label="Link" value={link} onChange={setLink} placeholder="https://…" />
          <div className="sm:col-span-2">
            <button
              type="submit"
              disabled={salvando}
              className="rounded-md bg-dawn px-4 py-2 text-sm font-medium text-paper transition hover:bg-twilight disabled:opacity-50"
            >
              {salvando ? "Salvando…" : "Salvar candidatura"}
            </button>
          </div>
        </form>
      )}

      <div className="mt-8 flex flex-wrap items-end gap-4 font-mono text-xs uppercase tracking-wider text-ink-soft">
        <label className="flex flex-col gap-1">
          Status
          <select
            value={filtroStatus}
            onChange={(event) => setFiltroStatus(event.target.value as StatusVaga | "")}
            className="rounded-md border border-line bg-surface px-2 py-1.5 normal-case tracking-normal text-ink"
          >
            <option value="">Todos</option>
            {STATUS_ORDEM.concat("REJEITADO").map((status) => (
              <option key={status} value={status}>
                {STATUS_LABEL[status]}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1">
          Plataforma
          <input
            value={filtroPlataforma}
            onChange={(event) => setFiltroPlataforma(event.target.value)}
            placeholder="Gupy…"
            className="rounded-md border border-line bg-surface px-2 py-1.5 normal-case tracking-normal text-ink"
          />
        </label>
      </div>

      {erro && (
        <p role="alert" className="mt-4 text-sm text-amber">
          {erro}
        </p>
      )}

      <div className="mt-4 overflow-hidden rounded-lg border border-line bg-surface">
        {carregando ? (
          <p className="p-6 text-sm text-ink-soft">Carregando…</p>
        ) : vagas.length === 0 ? (
          <p className="p-6 text-sm text-ink-soft">
            Nenhuma vaga por aqui ainda. Registre a primeira candidatura acima.
          </p>
        ) : (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-line font-mono text-[11px] uppercase tracking-wider text-ink-faint">
                <th className="px-4 py-3 font-medium">Empresa</th>
                <th className="px-4 py-3 font-medium">Cargo</th>
                <th className="px-4 py-3 font-medium">Plataforma</th>
                <th className="px-4 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {vagas.map((vaga) => (
                <tr key={vaga.id} className="border-b border-line last:border-0 hover:bg-paper">
                  <td className="px-4 py-3">
                    <Link href={`/vagas/${vaga.id}`} className="font-medium text-ink hover:text-dawn">
                      {vaga.empresa}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-ink-soft">{vaga.cargo}</td>
                  <td className="px-4 py-3 font-mono text-xs text-ink-soft">
                    {vaga.plataforma ?? "—"}
                  </td>
                  <td className="px-4 py-3">
                    <span className="inline-flex items-center gap-1.5 font-mono text-xs text-ink-soft">
                      <span className={`h-1.5 w-1.5 rounded-full ${STATUS_COR[vaga.statusAtual]}`} />
                      {STATUS_LABEL[vaga.statusAtual]}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

function Campo({
  label,
  value,
  onChange,
  required,
  placeholder,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  placeholder?: string;
}) {
  return (
    <label className="flex flex-col gap-1">
      <span className="font-mono text-xs uppercase tracking-wider text-ink-soft">{label}</span>
      <input
        value={value}
        required={required}
        placeholder={placeholder}
        onChange={(event) => onChange(event.target.value)}
        className="rounded-md border border-line bg-surface px-3 py-2 text-ink outline-none focus:border-dawn focus:ring-1 focus:ring-dawn"
      />
    </label>
  );
}
