import type { StatusVaga } from "./types";

export const STATUS_LABEL: Record<StatusVaga, string> = {
  APLICADO: "Aplicado",
  TRIAGEM: "Triagem",
  ENTREVISTA: "Entrevista",
  OFERTA: "Oferta",
  REJEITADO: "Rejeitado",
};

export const STATUS_COR: Record<StatusVaga, string> = {
  APLICADO: "bg-dawn",
  TRIAGEM: "bg-twilight",
  ENTREVISTA: "bg-amber",
  OFERTA: "bg-gold",
  REJEITADO: "bg-slate",
};

export const STATUS_TEXTO: Record<StatusVaga, string> = {
  APLICADO: "text-dawn",
  TRIAGEM: "text-twilight",
  ENTREVISTA: "text-amber",
  OFERTA: "text-gold",
  REJEITADO: "text-slate",
};

// Chart.js desenha em <canvas> e não entende classes Tailwind — precisa do
// valor literal, por isso duplicamos a paleta aqui (mesmos hex de globals.css).
export const STATUS_HEX: Record<StatusVaga, string> = {
  APLICADO: "#2e2a6b",
  TRIAGEM: "#6b4a8f",
  ENTREVISTA: "#c97a3d",
  OFERTA: "#e8b23d",
  REJEITADO: "#64748b",
};
