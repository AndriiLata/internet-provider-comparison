import { useState, useMemo } from "react";
import PlzCityAutocomplete from "./autocomplition/PlzCityAutocomplete";
import StreetAutocomplete from "./autocomplition/StreetAutocomplete";
import { usePlzSuggestions } from "../hooks/usePlzSuggestions";
import { useStreetSuggestions } from "../hooks/useStreetSuggestions";

/* ---------- types ---------- */
export interface SearchQuery {
  cityOrPostal: string;
  street: string;
  number: string;
  connectionTypes: string[];
  maxPrice: number;
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

interface Props { onSearch: (q: SearchQuery) => void; }

const CONNECTION_OPTIONS = ["DSL", "FIBER", "MOBILE", "CABLE"];

/* ---------- component ---------- */
export default function Sidebar({ onSearch }: Props) {
  const [form, setForm] = useState<SearchQuery>(() => {
    const saved = localStorage.getItem("lastSearch");
    return saved ? JSON.parse(saved) : DEFAULT_FORM;
  });

  const cached =
    (JSON.parse(localStorage.getItem("lastSearch") || "null") as
      | SearchQuery
      | null) ?? null;

  /* extract PLZ + city from cached label */
  const parsed = form.cityOrPostal.match(/^\\s*(\\d{4,5})\\s+(.+?)\\s*$/);
  const [plz,  setPlz]  = useState(parsed ? parsed[1] : "");
  const [city, setCity] = useState(parsed ? parsed[2] : "");

  const plzCitySugg = usePlzSuggestions(form.cityOrPostal);
  const streetSugg  = useStreetSuggestions(form.street, plz, city);

  const validPlzCity = useMemo(() => {
    if (!form.cityOrPostal.trim()) return false;
    if (plzCitySugg.some(s => s.label === form.cityOrPostal)) return true;
    return cached?.cityOrPostal === form.cityOrPostal;
  }, [form.cityOrPostal, plzCitySugg, cached]);

  const validStreet = useMemo(() => {
    if (!form.street.trim()) return false;
    if (streetSugg.some(s => s.street === form.street)) return true;
    return cached?.street === form.street;
  }, [form.street, streetSugg, cached]);

  const validNumber = form.number.trim() !== "";
  const formValid   = validPlzCity && validStreet && validNumber;

  /* submit */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formValid) return;

    const clean: SearchQuery = {
      ...form,
      cityOrPostal: form.cityOrPostal.trim(),
      street:       form.street.trim(),
      number:       form.number.trim(),
    };
    localStorage.setItem("lastSearch", JSON.stringify(clean));
    onSearch(clean);
  };

  const toggleConnection = (type: string) => {
    setForm(f => {
      const exists = f.connectionTypes.includes(type);
      return {
        ...f,
        connectionTypes: exists
          ? f.connectionTypes.filter(t => t !== type)
          : [...f.connectionTypes, type],
      };
    });
  };

  /* render */
  return (
    <aside className="w-80 shrink-0 bg-base-200 p-5 flex flex-col shadow-xl">
      <h2 className="text-2xl font-semibold mb-6">Address</h2>

      <form className="flex-grow flex flex-col" onSubmit={handleSubmit}>
        <div className="space-y-5 mb-7">
          <PlzCityAutocomplete
            value={form.cityOrPostal}
            onChange={v => setForm(f => ({ ...f, cityOrPostal: v }))}
            onSelect={(p, c) => { setPlz(p); setCity(c); }}
          />

          <div className="flex gap-2">
            <StreetAutocomplete
              className="flex-grow"
              value={form.street}
              onChange={v => setForm(f => ({ ...f, street: v }))}
              plz={plz}
              city={city}
            />

            <input
              name="number"
              value={form.number}
              onChange={e => setForm(f => ({ ...f, number: e.target.value }))}
              type="number"
              placeholder="Nr"
              className={`input input-bordered w-24 ${!validNumber ? "input-error" : ""}`}
            />
          </div>

          {!validNumber && (
            <p className="label-text-alt text-error -mt-3">
              Please enter a valid house number
            </p>
          )}
        </div>

        {/* filters */}
        <h2 className="text-2xl font-semibold mb-6">Filters</h2>

        <div className="grid grid-cols-2 gap-3 mb-7">
          {CONNECTION_OPTIONS.map(type => (
            <label key={type} className="flex items-center gap-2 cursor-pointer">
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

        {/* max-price slider */}
        <div className="mb-7 space-y-4">
          <label className="label">
            <span className="label-text">Max price (€)</span>
            <span className="label-text-alt font-semibold">{form.maxPrice}</span>
          </label>
          <input
            name="maxPrice"
            type="range"
            min="10"
            max="60"
            step="1"
            value={form.maxPrice}
            onChange={e =>
              setForm(f => ({ ...f, maxPrice: Number(e.target.value) }))}
            className="range range-primary"
          />
          <div className="w-full flex justify-between text-xs px-2 opacity-50">
            {[10, 20, 30, 40, 50, 60].map(v => (
              <span key={v}>€{v}</span>
            ))}
          </div>
        </div>

        {/* Additional services (unverändert) */}
        <div className="grid grid-cols-1 gap-5 mb-6">
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              className="checkbox checkbox-sm checkbox-primary"
              checked={form.includeTV}
              onChange={() =>
                setForm(f => ({ ...f, includeTV: !f.includeTV }))}
            />
            <span className="label-text">Include TV Connection</span>
          </label>
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              className="checkbox checkbox-sm checkbox-primary"
              checked={form.installationService}
              onChange={() =>
                setForm(f => ({
                  ...f,
                  installationService: !f.installationService,
                }))}
            />
            <span className="label-text">Installation Service</span>
          </label>
        </div>

        <button
          className="btn btn-primary mt-auto"
          type="submit"
          disabled={!formValid}
        >
          Search
        </button>
      </form>
    </aside>
  );
}
