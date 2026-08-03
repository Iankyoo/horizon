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
