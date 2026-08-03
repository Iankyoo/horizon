"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import BarChart from "@/components/BarChart";
import { ApiError, getDashboardMetrics, getToken } from "@/lib/api";
import { STATUS_HEX, STATUS_LABEL } from "@/lib/status";
import { STATUS_ORDEM, type DashboardMetricsResponse } from "@/lib/types";

const TODOS_STATUS = [...STATUS_ORDEM, "REJEITADO"] as const;

export default function DashboardPage() {
  const router = useRouter();
  const [metrics, setMetrics] = useState<DashboardMetricsResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      setMetrics(await getDashboardMetrics());
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : "Não foi possível carregar as métricas.");
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    // eslint-disable-next-line react-hooks/set-state-in-effect
    carregar();
  }, [carregar, router]);

  if (carregando) {
    return (
      <div className="mx-auto max-w-5xl px-6 py-10">
        <p className="text-sm text-ink-soft">Carregando…</p>
      </div>
    );
  }

  if (!metrics) {
    return (
      <div className="mx-auto max-w-5xl px-6 py-10">
        <p className="text-sm text-amber">{erro ?? "Não foi possível carregar as métricas."}</p>
      </div>
    );
  }

  const conversoes = [
    { rotulo: "Aplicado → Triagem", valor: metrics.taxaConversao.aplicadoParaTriagem },
    { rotulo: "Triagem → Entrevista", valor: metrics.taxaConversao.triagemParaEntrevista },
    { rotulo: "Entrevista → Oferta", valor: metrics.taxaConversao.entrevistaParaOferta },
  ];

  return (
    <div className="mx-auto max-w-5xl px-6 py-10">
      <h1 className="font-display text-2xl font-bold text-ink">Dashboard</h1>
      <p className="mt-1 text-sm text-ink-soft">Visão geral do funil de candidaturas.</p>

      {erro && (
        <p role="alert" className="mt-4 text-sm text-amber">
          {erro}
        </p>
      )}

      <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-2">
        <Card titulo="Total de vagas" valor={metrics.totalVagas} />
        <Card titulo="Últimos 30 dias" valor={metrics.totalUltimos30Dias} />
      </div>

      <Secao titulo="Distribuição por status">
        <BarChart
          labels={TODOS_STATUS.map((status) => STATUS_LABEL[status])}
          data={TODOS_STATUS.map((status) => metrics.distribuicaoPorStatus[status] ?? 0)}
          colors={TODOS_STATUS.map((status) => STATUS_HEX[status])}
          sufixo=" vaga(s)"
        />
      </Secao>

      <Secao titulo="Taxa de conversão entre etapas">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {conversoes.map((item) => (
            <div key={item.rotulo} className="rounded-lg border border-line bg-surface p-6 text-center">
              <p className="font-display text-3xl font-bold text-ink">{item.valor}%</p>
              <p className="mt-1 font-mono text-xs uppercase tracking-wider text-ink-soft">{item.rotulo}</p>
            </div>
          ))}
        </div>
      </Secao>

      <Secao titulo="Tempo médio até a próxima mudança">
        <BarChart
          labels={TODOS_STATUS.map((status) => STATUS_LABEL[status])}
          data={TODOS_STATUS.map((status) => metrics.tempoMedioPorEtapaEmDias[status] ?? 0)}
          colors={TODOS_STATUS.map((status) => STATUS_HEX[status])}
          sufixo=" dia(s)"
        />
      </Secao>

      <Secao titulo="Top plataformas">
        {metrics.topPlataformas.length === 0 ? (
          <p className="text-sm text-ink-soft">Nenhuma plataforma registrada ainda.</p>
        ) : (
          <BarChart
            horizontal
            labels={metrics.topPlataformas.map((item) => item.plataforma)}
            data={metrics.topPlataformas.map((item) => item.total)}
            colors={metrics.topPlataformas.map(() => STATUS_HEX.APLICADO)}
            sufixo=" vaga(s)"
          />
        )}
      </Secao>
    </div>
  );
}

function Card({ titulo, valor }: { titulo: string; valor: number }) {
  return (
    <div className="rounded-lg border border-line bg-surface p-6">
      <p className="font-display text-3xl font-bold text-ink">{valor}</p>
      <p className="mt-1 font-mono text-xs uppercase tracking-wider text-ink-soft">{titulo}</p>
    </div>
  );
}

function Secao({ titulo, children }: { titulo: string; children: React.ReactNode }) {
  return (
    <div className="mt-8">
      <h2 className="font-display text-lg font-semibold text-ink">{titulo}</h2>
      <div className="mt-4 rounded-lg border border-line bg-surface p-6">{children}</div>
    </div>
  );
}
