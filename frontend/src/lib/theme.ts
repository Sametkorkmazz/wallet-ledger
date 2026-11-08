export type Theme = "light" | "dark";
export const THEME_KEY = "theme";

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle("dark", theme === "dark");
}

export function saveTheme(theme: Theme) {
  try {
    localStorage.setItem(THEME_KEY, theme);
  } catch {
    // storage unavailable (private mode); fall back to system preference
  }
}
export function isDark() {
  return document.documentElement.classList.contains("dark");
}
