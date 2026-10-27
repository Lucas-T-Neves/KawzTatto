import { useMemo } from "react"
// Preserve the original project budget alongside independently added artifacts.
export default function useProjectArtifactsViewModel(project, status, stored) {
  const budgets = useMemo(() => {
    if (!project) return []
    const legacy =
      project.total !== undefined
        ? [
            {
              id: `budget-${project.id}`,
              source: "project",
              name: `Orçamento · ${project.name}`,
              date: project.created?.slice(0, 10) || "",
              projectId: project.id,
              clientId: project.clientId,
              total: project.total,
              deposit: project.deposit,
              estimate: project.estimate,
              perSession:
                Number(project.estimate) > 0
                  ? String(
                      Number(project.total || 0) / Number(project.estimate),
                    )
                  : "",
              method: project.method,
              expires: project.expires,
              accepted: project.accepted,
              status,
            },
          ]
        : []
    return [
      ...legacy,
      ...stored
        .filter((item) => item.projectId === project.id)
        .map((item) => ({
          ...item,
          perSession:
            Number(item.estimate) > 0
              ? String(Number(item.total || 0) / Number(item.estimate))
              : "",
        })),
    ]
  }, [project, status, stored])
  return { budgets }
}
