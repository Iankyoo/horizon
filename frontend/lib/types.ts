export type StatusVaga =
  | "APLICADO"
  | "TRIAGEM"
  | "ENTREVISTA"
  | "OFERTA"
  | "REJEITADO";

export const STATUS_ORDEM: StatusVaga[] = [
  "APLICADO",
  "TRIAGEM",
  "ENTREVISTA",
  "OFERTA",
];

export interface VagaResponse {
  id: number;
  empresa: string;
  cargo: string;
  plataforma: string | null;
  link: string | null;
  statusAtual: StatusVaga;
  dataCriacao: string;
}

export interface StatusHistoricoResponse {
  id: number;
  status: StatusVaga;
  dataMudanca: string;
  observacao: string | null;
}

export interface VagaDetailResponse extends VagaResponse {
  historico: StatusHistoricoResponse[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface TaxaConversaoResponse {
  aplicadoParaTriagem: number;
  triagemParaEntrevista: number;
  entrevistaParaOferta: number;
}

export interface PlataformaCountResponse {
  plataforma: string;
  total: number;
}

export interface DashboardMetricsResponse {
  totalVagas: number;
  totalUltimos30Dias: number;
  distribuicaoPorStatus: Record<StatusVaga, number>;
  taxaConversao: TaxaConversaoResponse;
  tempoMedioPorEtapaEmDias: Record<StatusVaga, number>;
  topPlataformas: PlataformaCountResponse[];
}

export interface VagaRequest {
  empresa: string;
  cargo: string;
  plataforma?: string;
  link?: string;
}

export interface AtualizarStatusRequest {
  status: StatusVaga;
  observacao?: string;
}
