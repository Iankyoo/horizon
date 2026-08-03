"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { clearToken, getDashboardMetrics, getToken } from "@/lib/api";
import { STATUS_COR, STATUS_LABEL } from "@/lib/status";
import { STATUS_ORDEM, type DashboardMetricsResponse } from "@/lib/types";

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const [autenticado, setAutenticado] = useState(false);
  const [metrics, setMetrics] = useState<DashboardMetricsResponse | null>(null);

  useEffect(() => {
    // localStorage não existe no SSR; ler aqui (pós-montagem) evita
    // divergência de hidratação em vez de checar no render.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setAutenticado(Boolean(getToken()));
  }, [pathname]);

  useEffect(() => {
    if (!autenticado) return;
    getDashboardMetrics()
      .then(setMetrics)
      .catch(() => setMetrics(null));
  }, [autenticado, pathname]);

  function sair() {
    clearToken();
    router.push("/login");
  }

  const emLogin = pathname === "/login";

  return (
    <header className="border-b border-line bg-surface">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link href="/vagas" className="font-display text-xl font-bold tracking-tight text-ink">
          Horizon
        </Link>

        {autenticado && !emLogin && (
          <nav className="flex items-center gap-6 font-mono text-xs uppercase tracking-wider text-ink-soft">
            <Link href="/vagas" className="hover:text-ink">
              Vagas
            </Link>
            <Link href="/dashboard" className="hover:text-ink">
              Dashboard
            </Link>
            <button onClick={sair} className="hover:text-ink" type="button">
              Sair
            </button>
          </nav>
        )}
      </div>

      {autenticado && !emLogin && metrics && (
        <div className="mx-auto max-w-5xl px-6 pb-4">
          <div className="h-1.5 w-full rounded-full bg-[linear-gradient(to_right,var(--color-dawn),var(--color-twilight),var(--color-amber),var(--color-gold))]" />
          <div className="mt-2 flex items-center justify-between font-mono text-[11px] text-ink-soft">
            <div className="flex gap-5">
              {STATUS_ORDEM.map((status) => (
                <span key={status} className="flex items-center gap-1.5">
                  <span className={`h-1.5 w-1.5 rounded-full ${STATUS_COR[status]}`} />
                  {STATUS_LABEL[status]} {metrics.distribuicaoPorStatus[status] ?? 0}
                </span>
              ))}
            </div>
            <span className="flex items-center gap-1.5 text-slate">
              <span className="h-1.5 w-1.5 rounded-full bg-slate" />
              Rejeitado {metrics.distribuicaoPorStatus.REJEITADO ?? 0}
            </span>
          </div>
        </div>
      )}
    </header>
  );
}
