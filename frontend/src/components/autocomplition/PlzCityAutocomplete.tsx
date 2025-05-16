import { useState } from "react";
import {
  usePlzSuggestions,
  type PlzSuggestion,
} from "../../hooks/usePlzSuggestions";

interface Props {
  value: string;
  onChange: (val: string) => void;
  /** fires when user picks a suggestion */
  onSelect: (plz: string, city: string) => void;
  className?: string;
}

export default function PlzCityAutocomplete({
  value,
  onChange,
  onSelect,
  className = "",
}: Props) {
  const [open, setOpen]   = useState(false);
  const [error, setError] = useState(false);

  /** last value we accepted as definitely valid */
  const [valid, setValid] = useState<string>(value); // <- accept cached value

  const suggs = usePlzSuggestions(value);

  /* user typing */
  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(false);
    onChange(e.target.value);
    setOpen(true);
  };

  /* user picks from list */
  const pick = (s: PlzSuggestion) => {
    onChange(s.label);
    setValid(s.label);
    setError(false);
    setOpen(false);
    onSelect(s.plz, s.city);
  };

  /* on blur: verify value */
  const handleBlur = () => {
    setTimeout(() => {
      setOpen(false);
      if (!value) { setError(false); return; }

      // treat as valid if equal to the last accepted OR appears in suggestions
      if (value === valid || suggs.some(s => s.label === value)) {
        setValid(value);
        setError(false);
      } else {
        setError(true);
      }
    }, 150); // wait for click in list
  };

  return (
    <div className={`relative ${className}`}>
      <input
        type="text"
        placeholder="Enter postal code and city"
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
