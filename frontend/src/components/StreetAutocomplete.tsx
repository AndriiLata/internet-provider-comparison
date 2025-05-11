// components/StreetAutocomplete.tsx
import { useState } from "react";
import { useStreetSuggestions } from "../hooks/useStreetSuggestions";

interface Props {
  value: string;
  onChange: (val: string) => void;
  plz: string;
  city: string;
  className?: string;
}

export default function StreetAutocomplete({
  value,
  onChange,
  plz,
  city,
  className = "",
}: Props) {
  const [open, setOpen]   = useState(false);
  const [error, setError] = useState(false);
  const suggs             = useStreetSuggestions(value, plz, city);

  const isValid = !value || suggs.some(s => s.street === value);

  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(false);
    onChange(e.target.value);
    setOpen(true);
  };

  const pick = (street: string) => {
    onChange(street);
    setError(false);
    setOpen(false);
  };

  const handleBlur = () => {
    setTimeout(() => {
      setOpen(false);
      setError(!isValid);
    }, 150);
  };

  return (
    <div className={`relative ${className}`}>
      <input
        type="text"
        placeholder="Street"
        value={value}
        onChange={handleInput}
        onBlur={handleBlur}
        autoComplete="off"
        className={`input input-bordered w-full ${
          error ? "input-error" : ""
        }`}
      />
      {error && (
        <p className="label-text-alt text-error mt-1">
          Keine passende Straße für diese PLZ/Stadt
        </p>
      )}

      {open && suggs.length > 0 && (
        <ul className="absolute left-0 right-0 z-10 mt-1 menu bg-base-200 rounded-box shadow-lg max-h-60 overflow-y-auto">
          {suggs.map(s => (
            <li
              key={s.street}
              className="p-2 hover:bg-primary hover:text-primary-content cursor-pointer"
              onMouseDown={() => pick(s.street)}
            >
              {s.street}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
