// components/PlzCityAutocomplete.tsx
import { useState } from "react";
import { usePlzSuggestions } from "../hooks/usePlzSuggestions";

interface Props {
  value: string;
  onChange: (val: string) => void;
  className?: string;
}

export default function PlzCityAutocomplete({
  value,
  onChange,
  className = "",
}: Props) {
  const [open, setOpen]   = useState(false);
  const suggestions       = usePlzSuggestions(value);

  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value);
    setOpen(true);
  };

  const handleSelect = (label: string) => {
    onChange(label);
    setOpen(false);
  };

  return (
    <div className={`relative ${className}`}>
      <input
        type="text"
        placeholder="Postal code or City"
        value={value}
        onChange={handleInput}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
        autoComplete="off"
        className="input input-bordered w-full"
      />

      {open && suggestions.length > 0 && (
        <ul className="absolute left-0 right-0 z-10 mt-1 menu bg-base-200 rounded-box shadow-lg max-h-60 overflow-y-auto">
          {suggestions.map((s) => (
            <li
              key={s.label}
              className="p-2 hover:bg-primary hover:text-primary-content cursor-pointer"
              onMouseDown={() => handleSelect(s.label)}
            >
              {s.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
