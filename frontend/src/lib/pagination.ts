export function parsePage(searchParams: URLSearchParams): number {
  const raw = Number(searchParams.get("page"));
  return Number.isInteger(raw) && raw >= 1 ? raw : 1;
}
