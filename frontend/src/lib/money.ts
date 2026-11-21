import { CURRENCIES, type Currency } from "@/api/types";

const LOCALE = "en-US";

const dateFormatter = new Intl.DateTimeFormat(LOCALE, {
  dateStyle: "medium",
  timeStyle: "short",
});

const dateFormatterShort = new Intl.DateTimeFormat(LOCALE, {
  dateStyle: "medium",
});

const moneyFormats = Object.fromEntries(
  CURRENCIES.map((currency) => [currency, makeFormat(currency)]),
) as Record<Currency, Intl.NumberFormat>;

function makeFormat(currency: Currency) {
  return new Intl.NumberFormat(LOCALE, {
    style: "currency",
    currency,
    currencyDisplay: "narrowSymbol",
  });
}

export function formatMoney(amount: string, currency: Currency) {
  return moneyFormats[currency].format(amount as Intl.StringNumericLiteral);
}

export function formatDate(date: number) {
  return dateFormatter.format(date);
}

export function formatDateShort(date: number) {
  return dateFormatterShort.format(date);
}
