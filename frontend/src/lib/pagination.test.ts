import { parsePage } from "@/lib/pagination";
import { describe, expect, it } from "vitest";

const parse = (query: string) => parsePage(new URLSearchParams(query));

describe("parsePage", () => {
  it.each([
    ["", 1], // no param
    ["page=1", 1],
    ["page=4", 4],
    ["page=0", 1],
    ["page=-3", 1],
    ["page=2.5", 1],
    ["page=abc", 1],
    ["page=", 1], // Number("") is 0
    ["page=1e2", 100],
  ])("%s -> %i", (query, expected) => {
    expect(parse(query)).toBe(expected);
  });
});
