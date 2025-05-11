// components/Sidebar.tsx
import { useState } from "react";
import PlzCityAutocomplete from "./PlzCityAutocomplete";
import { usePlzSuggestions } from "../hooks/usePlzSuggestions";

export interface SearchQuery {
  cityOrPostal: string;
  street: string;
  number: string;
  connectionTypes: string[];   // DSL, FIBER, MOBILE, CABLE
  maxPrice: number;            // €
  includeTV: boolean;
  installationService: boolean;
}

const DEFAULT_FORM: SearchQuery = {
  cityOrPostal: "",
  street: "",
  number: "",
  connectionTypes: [],
  maxPrice: 60,
  includeTV: false,
  installationService: false,
};

interface Props {
  onSearch: (q: SearchQuery) => void;
}

const CONNECTION_OPTIONS = ["DSL", "FIBER", "MOBILE", "CABLE"];

export default function Sidebar({ onSearch }: Props) {
  const [form, setForm] = useState<SearchQuery>(() => {
    const saved = localStorage.getItem("lastSearch");
    return saved ? JSON.parse(saved) : DEFAULT_FORM;
  });

  /** Liste nur für die Validierung beim Submit */
  const suggestions = usePlzSuggestions(form.cityOrPostal);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Ungültige Eingaben abfangen
    if (
      form.cityOrPostal &&
      !suggestions.some((s) => s.label === form.cityOrPostal)
    ) {
      alert("Bitte eine gültige Stadt/PLZ aus den Vorschlägen wählen.");
      return;
    }

    const clean = {
      ...form,
      cityOrPostal: form.cityOrPostal.trim(),
      street: form.street.trim(),
      number: form.number.trim(),
    };
    localStorage.setItem("lastSearch", JSON.stringify(clean));
    onSearch(clean);
  };

  const toggleConnection = (type: string) => {
    setForm((f) => {
      const exists = f.connectionTypes.includes(type);
      return {
        ...f,
        connectionTypes: exists
          ? f.connectionTypes.filter((t) => t !== type)
          : [...f.connectionTypes, type],
      };
    });
  };

  return (
    <aside className="w-80 shrink-0 bg-base-200 p-5 flex flex-col shadow-xl">
      <h2 className="text-2xl font-semibold mb-6">Address</h2>

      <form className="flex-grow flex flex-col" onSubmit={handleSubmit}>
        <div className="space-y-5 mb-7">
          {/* Autocomplete */}
          <PlzCityAutocomplete
            value={form.cityOrPostal}
            onChange={(v) => setForm((f) => ({ ...f, cityOrPostal: v }))}
          />

          <div className="flex gap-2">
            <input
              name="street"
              value={form.street}
              onChange={handleChange}
              type="text"
              placeholder="Street"
              className="input input-bordered flex-grow"
            />
            <input
              name="number"
              value={form.number}
              onChange={handleChange}
              type="number"
              placeholder="Nr"
              className="input input-bordered w-24"
            />
          </div>
        </div>

        <h2 className="text-2xl font-semibold mb-6">Filters</h2>

        {/* Connection type checkboxes */}
        <div className="grid grid-cols-2 gap-3 mb-7">
          {CONNECTION_OPTIONS.map((type) => (
            <label
              key={type}
              className="flex items-center gap-2 cursor-pointer"
            >
              <input
                type="checkbox"
                className="checkbox checkbox-sm checkbox-primary"
                checked={form.connectionTypes.includes(type)}
                onChange={() => toggleConnection(type)}
              />
              <span className="label-text">{type}</span>
            </label>
          ))}
        </div>

        {/* Max price slider */}
        <div className="mb-7 space-y-4">
          <label className="label">
            <span className="label-text">Max price (€)</span>
            <span className="label-text-alt font-semibold">
              {form.maxPrice}
            </span>
          </label>
          <input
            name="maxPrice"
            type="range"
            min="10"
            max="60"
            step="1"
            value={form.maxPrice}
            onChange={(e) =>
              setForm((f) => ({ ...f, maxPrice: Number(e.target.value) }))
            }
            className="range range-primary"
          />
          <div className="w-full flex justify-between text-xs px-2 opacity-50">
            {[10, 20, 30, 40, 50, 60].map((v) => (
              <span key={v}>€{v}</span>
            ))}
          </div>
        </div>

        {/* Additional services */}
        <div className="grid grid-cols-1 gap-5 mb-6">
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              className="checkbox checkbox-sm checkbox-primary"
              checked={form.includeTV}
              onChange={() =>
                setForm((f) => ({ ...f, includeTV: !f.includeTV }))
              }
            />
            <span className="label-text">Include TV Connection</span>
          </label>
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              className="checkbox checkbox-sm checkbox-primary"
              checked={form.installationService}
              onChange={() =>
                setForm((f) => ({ ...f, installationService: !f.installationService }))
              }
            />
            <span className="label-text">Installation Service</span>
          </label>
        </div>

        <button className="btn btn-primary mt-auto" type="submit">
          Search
        </button>
      </form>
    </aside>
  );
}
