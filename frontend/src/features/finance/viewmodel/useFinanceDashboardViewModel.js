import { useMemo } from "react"
import { buildFinanceDashboard } from "../model/financeDashboard.js"

export default function useFinanceDashboardViewModel(
  transactions,
  clients,
  projects,
  period,
) {
  const today = new Date().toLocaleDateString("en-CA")
  return useMemo(
    () => buildFinanceDashboard(transactions, clients, projects, period, today),
    [transactions, clients, projects, period, today],
  )
}
