import { formatDateShort, formatMoney } from "@/lib/money";

import { describe, expect, it } from "vitest";

describe("formatMoney", () => {
  it("formats EUR with symbol and 2 decimals", () => {
    expect(formatMoney("2450.00", "EUR")).toBe("€2,450.00");
  });

  it("formats USD with symbol and 2 decimals", () => {
    expect(formatMoney("2450.00", "USD")).toBe("$2,450.00");
  });

  it("formats TRY with symbol and 2 decimals", () => {
    expect(formatMoney("2450.00", "TRY")).toBe("₺2,450.00");
  });

  it("formats zero with 2 decimals", () => {
    expect(formatMoney("0.00", "TRY")).toBe("₺0.00");
  });

  it("formats whole number", () => {
    expect(formatMoney("5", "TRY")).toBe("₺5.00");
  });

  it("formats large amount", () => {
    expect(formatMoney("1234567.89", "TRY")).toBe("₺1,234,567.89");
  });

  it("does not lose precision on large amounts", () => {
    expect(formatMoney("9007199254740993.00", "TRY")).toBe(
      "₺9,007,199,254,740,993.00",
    );
  });

  it("formats negative amount", () => {
    expect(formatMoney("-5.00", "TRY")).toBe("-₺5.00");
  });
});

describe("formatDateShort", () => {
  it("formats fixed timestamp", () => {
    expect(formatDateShort(Date.parse("2026-11-21T12:00:00Z"))).toBe(
      "Nov 21, 2026",
    );
  });
});
