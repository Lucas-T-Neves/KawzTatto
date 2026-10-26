export function activityInterval({ date, start, end }) {
  const from = new Date(`${date}T${start}:00`)
  const until = new Date(`${date}T${end}:00`)
  if (end < start) until.setDate(until.getDate() + 1)
  return [from.getTime(), until.getTime()]
}
export function activitiesOverlap(a, b) {
  const [aStart, aEnd] = activityInterval(a)
  const [bStart, bEnd] = activityInterval(b)
  return aStart < bEnd && bStart < aEnd
}
