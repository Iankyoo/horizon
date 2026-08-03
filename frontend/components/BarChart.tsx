"use client";

import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  LinearScale,
  Tooltip,
  type ChartOptions,
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip);

export default function BarChart({
  labels,
  data,
  colors,
  horizontal = false,
  sufixo = "",
}: {
  labels: string[];
  data: number[];
  colors: string[];
  horizontal?: boolean;
  sufixo?: string;
}) {
  const eixoValor = horizontal ? "x" : "y";

  const options: ChartOptions<"bar"> = {
    indexAxis: horizontal ? "y" : "x",
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      tooltip: {
        backgroundColor: "#14181f",
        titleFont: { family: "var(--font-plex-mono)" },
        bodyFont: { family: "var(--font-plex-mono)" },
        callbacks: {
          label: (item) => `${item.formattedValue}${sufixo}`,
        },
      },
    },
    scales: {
      [eixoValor]: {
        beginAtZero: true,
        grid: { color: "#d7dce4" },
        ticks: {
          font: { family: "var(--font-plex-mono)", size: 11 },
          color: "#4b5563",
        },
      },
      [horizontal ? "y" : "x"]: {
        grid: { display: false },
        ticks: {
          font: { family: "var(--font-plex-mono)", size: 11 },
          color: "#4b5563",
        },
      },
    },
  };

  return (
    <div className="h-56">
      <Bar
        data={{
          labels,
          datasets: [
            {
              data,
              backgroundColor: colors,
              borderRadius: 4,
              barThickness: horizontal ? 18 : 28,
            },
          ],
        }}
        options={options}
      />
    </div>
  );
}
