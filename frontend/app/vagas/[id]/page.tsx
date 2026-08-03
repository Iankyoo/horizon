"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useState, type FormEvent } from "react";
import { ApiError, atualizarStatus, getToken, getVaga } from "@/lib/api";
import { STATUS_COR, STATUS_LABEL, STATUS_TEXTO } from "@/lib/status";
import { STATUS_ORDEM, type StatusVaga, type VagaDetailResponse } from "@/lib/types";

const TODOS_STATUS: StatusVaga[] = [...STATUS_ORDEM, "REJEITADO"];

function formatarData(iso: string) {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function VagaDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [vaga, setVaga] = useState<VagaDetailResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [novoStatus, setNovoStatus] = useState<StatusVaga | "">("");
  const [observacao, setObservacao] = useState("");
  const [atualizando, setAtualizando] = useState(false);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const detalhe = await getVaga(Number(id));
      setVaga(detalhe);
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : "Não foi possível carregar a vaga.");
    } finally {
      setCarregando(false);
    }
  }, [id]);

  useEffect(() => {
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    // eslint-disable-next-line react-hooks/set-state-in-effect
    carregar();
  }, [carregar, router]);

  async function handleAtualizarStatus(event: FormEvent) {
    event.preventDefault();
    if (!novoStatus) return;
    setAtualizando(true);
    setErro(null);
    try {
      await atualizarStatus(Number(id), {
        status: novoStatus,
        observacao: observacao || undefined,
      });
      setNovoStatus("");
      setObservacao("");
      await carregar();
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : "Não foi possível atualizar o status.");
    } finally {
      setAtualizando(false);
    }
  }

  if (carregando) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-10">
        <p className="text-sm text-ink-soft">Carregando…</p>
      </div>
    );
  }

  if (!vaga) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-10">
        <Link href="/vagas" className="font-mono text-xs uppercase tracking-wider text-ink-soft hover:text-ink">
          ← Vagas
        </Link>
        <p className="mt-4 text-sm text-amber">{erro ?? "Vaga não encontrada."}</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-10">
      <Link href="/vagas" className="font-mono text-xs uppercase tracking-wider text-ink-soft hover:text-ink">
        ← Vagas
      </Link>

      <div className="mt-4 flex items-start justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink">{vaga.empresa}</h1>
          <p className="mt-1 text-sm text-ink-soft">{vaga.cargo}</p>
        </div>
        <span className="inline-flex items-center gap-1.5 font-mono text-xs text-ink-soft">
          <span className={`h-1.5 w-1.5 rounded-full ${STATUS_COR[vaga.statusAtual]}`} />
          <span className={STATUS_TEXTO[vaga.statusAtual]}>{STATUS_LABEL[vaga.statusAtual]}</span>
        </span>
      </div>

      <dl className="mt-6 grid grid-cols-2 gap-4 rounded-lg border border-line bg-surface p-6 font-mono text-xs">
        <div>
          <dt className="uppercase tracking-wider text-ink-faint">Plataforma</dt>
          <dd className="mt-1 text-ink">{vaga.plataforma ?? "—"}</dd>
        </div>
        <div>
          <dt className="uppercase tracking-wider text-ink-faint">Aplicado em</dt>
          <dd className="mt-1 text-ink">{formatarData(vaga.dataCriacao)}</dd>
        </div>
        <div className="col-span-2">
          <dt className="uppercase tracking-wider text-ink-faint">Link</dt>
          <dd className="mt-1 text-ink">
            {vaga.link ? (
              <a href={vaga.link} target="_blank" rel="noreferrer" className="text-dawn hover:underline">
                {vaga.link}
              </a>
            ) : (
              "—"
            )}
          </dd>
        </div>
      </dl>

      {erro && (
        <p role="alert" className="mt-4 text-sm text-amber">
          {erro}
        </p>
      )}

      <form
        onSubmit={handleAtualizarStatus}
        className="mt-8 grid grid-cols-1 gap-4 rounded-lg border border-line bg-surface p-6 sm:grid-cols-2"
      >
        <h2 className="col-span-full font-display text-lg font-semibold text-ink">Mudar status</h2>
        <label className="flex flex-col gap-1">
          <span className="font-mono text-xs uppercase tracking-wider text-ink-soft">Novo status</span>
          <select
            value={novoStatus}
            onChange={(event) => setNovoStatus(event.target.value as StatusVaga)}
            required
            className="rounded-md border border-line bg-surface px-3 py-2 text-ink outline-none focus:border-dawn focus:ring-1 focus:ring-dawn"
          >
            <option value="" disabled>
              Selecione…
            </option>
            {TODOS_STATUS.map((status) => (
              <option key={status} value={status}>
                {STATUS_LABEL[status]}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1">
          <span className="font-mono text-xs uppercase tracking-wider text-ink-soft">Observação (opcional)</span>
          <input
            value={observacao}
            onChange={(event) => setObservacao(event.target.value)}
            placeholder="Ex: feedback do recrutador"
            className="rounded-md border border-line bg-surface px-3 py-2 text-ink outline-none focus:border-dawn focus:ring-1 focus:ring-dawn"
          />
        </label>
        <div className="sm:col-span-2">
          <button
            type="submit"
            disabled={atualizando || !novoStatus}
            className="rounded-md bg-dawn px-4 py-2 text-sm font-medium text-paper transition hover:bg-twilight disabled:opacity-50"
          >
            {atualizando ? "Atualizando…" : "Atualizar status"}
          </button>
        </div>
      </form>

      <div className="mt-8">
        <h2 className="font-display text-lg font-semibold text-ink">Histórico</h2>
        <ol className="mt-4 border-l border-line pl-6">
          {vaga.historico.map((evento) => (
            <li key={evento.id} className="relative pb-6 last:pb-0">
              <span
                className={`absolute -left-[29px] mt-1 h-2 w-2 rounded-full ${STATUS_COR[evento.status]}`}
              />
              <div className="flex items-baseline gap-2">
                <span className={`text-sm font-medium ${STATUS_TEXTO[evento.status]}`}>
                  {STATUS_LABEL[evento.status]}
                </span>
                <span className="font-mono text-xs text-ink-faint">{formatarData(evento.dataMudanca)}</span>
              </div>
              {evento.observacao && <p className="mt-1 text-sm text-ink-soft">{evento.observacao}</p>}
            </li>
          ))}
        </ol>
      </div>
    </div>
  );
}
