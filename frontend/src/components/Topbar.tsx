// src/components/Topbar.tsx
import { useEffect, useState } from "react";

/** Tailwind/daisyUI v5 theme names */
const THEMES = [
  { id: "light",     label: "Default" },
  { id: "night",     label: "Night" },
  { id: "retro",     label: "Retro" }
] as const;

export default function Topbar() {
  const [theme, setTheme] = useState(
    () => localStorage.getItem("theme") ?? "light",
  );

  useEffect(() => {
    // daisyUI’s <input class="theme-controller"> sets data-theme for us,
    // we only persist the choice:
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
    <header className="navbar bg-base-100 px-4 shadow-lg sticky top-0 z-50">
      <div className="flex-1 text-2xl font-extrabold tracking-wide">
        internet<span className="text-primary">CHECK</span>
      </div>

      {/* horizontal radio group */}
      <div className="flex-none">
        <div className="join">
          {THEMES.map(({ id, label }) => (
            <label key={id} className="join-item cursor-pointer font-medium">
              <input
                type="radio"
                name="theme"
                value={id}
                checked={theme === id}
                onChange={() => setTheme(id)}
                className="theme-controller hidden"
              />
              <span
                className={`btn btn-xs sm:btn-sm rounded-none
                  ${theme === id ? "btn-primary" : "btn-ghost"}
                `}
              >
                {label}
              </span>
            </label>
          ))}
        </div>
      </div>
    </header>
  );
}
