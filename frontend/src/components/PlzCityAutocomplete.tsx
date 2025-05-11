// components/PlzCityAutocomplete.tsx
import { useState } from "react";
import { usePlzSuggestions, type PlzSuggestion } from "../hooks/usePlzSuggestions";

interface Props {
  value: string;
  onChange: (val: string) => void;
  /** Wird aufgerufen, wenn der Nutzer einen gültigen Vorschlag ausgewählt hat */
  onSelect: (plz: string, city: string) => void;
  className?: string;
}

export default function PlzCityAutocomplete({
  value,
  onChange,
  onSelect,
  className = "",
}: Props) {
  const [open, setOpen]       = useState(false);
  const [error, setError]     = useState(false);
  const [valid, setValid]     = useState<string>("");      // letzter gültiger Wert
  const suggs = usePlzSuggestions(value);

  /* Eingabe ändern */
  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(false);
    onChange(e.target.value);
    setOpen(true);
  };

  /* Auswahl aus Liste */
  const pick = (s: PlzSuggestion) => {
    onChange(s.label);
    setValid(s.label);
    setError(false);
    setOpen(false);
    onSelect(s.plz, s.city);
  };

  /* Validierung bei Blur */
  const handleBlur = () => {
    setTimeout(() => {
      setOpen(false);
      if (value && value !== valid) setError(true);
    }, 150);
  };

  return (
    <div className={`relative ${className}`}>
      <input
        type="text"
        placeholder="PLZ Stadt"
        value={value}
        onChange={handleInput}
        onBlur={handleBlur}
        autoComplete="off"
        className={`input input-bordered w-full ${error ? "input-error" : ""}`}
      />
      {error && (
        <p className="label-text-alt text-error mt-1">
          Please enter a valid postal code and city
        </p>
      )}

      {open && suggs.length > 0 && (
        <ul className="absolute left-0 right-0 z-10 mt-1 menu bg-base-200 rounded-box shadow-lg max-h-60 overflow-y-auto">
          {suggs.map((s) => (
            <li
              key={s.label}
              className="p-2 hover:bg-primary hover:text-primary-content cursor-pointer"
              onMouseDown={() => pick(s)}
            >
              {s.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
