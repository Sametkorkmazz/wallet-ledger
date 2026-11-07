export function setTheme(dark: boolean) {
  document.documentElement.classList.toggle("dark", dark);
  try {
    localStorage.setItem("theme", dark ? "dark" : "light");
  } catch {}
}
export function isDark() {
  return document.documentElement.classList.contains("dark");
}
