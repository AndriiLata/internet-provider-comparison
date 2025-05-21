import { useState } from "react";
import { useStreetSuggestions } from "../../hooks/useStreetSuggestions";

interface Props {
  value: string;
  onChange: (val: string) => void;
  plz: string;
  city: string;
  className?: string;
  disabled?: boolean;
}

export default function StreetAutocomplete({
  value,
  onChange,
  plz,
  city,
  className = "",
  disabled = false,
}: Props) {
  const [open, setOpen]   = useState(false);
  const [error, setError] = useState(false);
  const suggs             = useStreetSuggestions(value, plz, city);

  const isValid = !value || suggs.some(s => s.street === value);

  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (disabled) return;
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

      if (!value) { setError(false); return; }

      // If suggestions are empty (e.g. first render) we trust cached value.
      if (isValid || suggs.length === 0) {
        setError(false);
      } else {
        setError(true);
      }
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
        disabled={disabled}
        className={`input input-bordered w-full 
          ${error ? "input-error" : ""}
          ${disabled ? "cursor-not-allowed opacity-60" : ""}`}
      />

      {error && (
        <p className="label-text-alt text-error mt-1">
          No valid street found for this postal code
        </p>
      )}

      {open && !disabled && suggs.length > 0 && (
        <ul className="absolute left-0 right-0 z-10 mt-1 menu bg-base-200 rounded-box shadow-lg max-h-60 overflow-y-auto">
          {suggs.map((s) => (
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
