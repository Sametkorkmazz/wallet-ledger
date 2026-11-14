import type { Balance } from "@/api/types";

const delay = (ms: number) =>
  new Promise<void>((resolve) => setTimeout(resolve, ms));

export async function getBalances(): Promise<Balance[]> {
  await delay(500); // TODO: replace with GET /api/balances (Dec 7 week)
  return [
    { currency: "EUR", amount: "1250.00" },
    { currency: "USD", amount: "830.50" },
    { currency: "TRY", amount: "42000.00" },
  ];
}
