import { applyTheme, isDark, saveTheme, type Theme } from "@/lib/theme";
import { useState } from "react";
const useTheme = () => {
  const [theme, setTheme] = useState<Theme>(() =>
    isDark() ? "dark" : "light",
  );

  function toggleTheme() {
    const newTheme = theme === "dark" ? "light" : "dark";
    setTheme(newTheme);
    applyTheme(newTheme);
    saveTheme(newTheme);
  }

  return { theme, toggleTheme };
};

export default useTheme;
