export const CURRENCIES = ["EUR", "USD", "TRY"] as const;
export type Currency = (typeof CURRENCIES)[number]; // "EUR" | "USD" | "TRY"

export type Balance = {
  currency: Currency;
  amount: string; // decimal string (ADR 4)
};
