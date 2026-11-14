import type { Balance } from "@/api/types";

const delay = (ms: number) =>
  new Promise<void>((resolve) => setTimeout(resolve, ms));

export async function getBalances(): Promise<Balance[]> {
  await delay(500); // TODO: replace with GET /api/balances (Dec 7 week)
  return [
    {
      currency: "EUR",
      amount: "2450.00",
      inThisMonth: "2800.00",
      outThisMonth: "350.00",
    },
    {
      currency: "USD",
      amount: "1120.50",
      inThisMonth: "1205.50",
      outThisMonth: "85.00",
    },
    {
      currency: "TRY",
      amount: "38900.00",
      inThisMonth: "4500.00",
      outThisMonth: "0.00",
    },
  ];
}
