export const CURRENCIES = ["EUR", "USD", "TRY"] as const;
export type Currency = (typeof CURRENCIES)[number]; // "EUR" | "USD" | "TRY"

export type Balance = {
  currency: Currency;
  amount: string; // decimal string (ADR 4)
  inThisMonth: string;
  outThisMonth: string;
};

export const CURRENCY_SYMBOL: Record<Currency, string> = {
  EUR: "€",
  USD: "$",
  TRY: "₺",
};

export const CURRENCY_NAME: Record<Currency, string> = {
  EUR: "Euro",
  USD: "Dollar",
  TRY: "Lira",
};
