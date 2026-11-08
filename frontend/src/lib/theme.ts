export function setTheme(dark: boolean) {
  document.documentElement.classList.toggle("dark", dark);
  try {
    localStorage.setItem("theme", dark ? "dark" : "light");
  } catch {
    // storage unavailable (private mode); fall back to system preference
  }
}
export function isDark() {
  return document.documentElement.classList.contains("dark");
}
