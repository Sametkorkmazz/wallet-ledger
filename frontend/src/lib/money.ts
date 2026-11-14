import type { Currency } from "@/api/types";

export function formatMoney(amount: string, currency: Currency) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    currencyDisplay: "narrowSymbol",
  }).format(amount as Intl.StringNumericLiteral);
}
