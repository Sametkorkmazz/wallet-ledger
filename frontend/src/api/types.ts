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

export type TransactionType = "TRANSFER_IN" | "TRANSFER_OUT";
export type TransactionStatus = "COMPLETED" | "PENDING" | "FAILED";

export type Transaction = {
  id: string;
  reference: string;
  type: TransactionType;
  status: TransactionStatus;
  currency: Currency;
  amount: string;
  counterparty: string;
  createdAt: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
