import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import PlzCityAutocomplete from "../components/autocomplition/PlzCityAutocomplete";
import StreetAutocomplete from "../components/autocomplition/StreetAutocomplete";
import { usePlzSuggestions } from "../hooks/usePlzSuggestions";
import { useStreetSuggestions } from "../hooks/useStreetSuggestions";
import type { SearchQuery } from "../components/Sidebar";

const EMPTY_QUERY: SearchQuery = {
  cityOrPostal: "",
  street: "",
  number: "",
  connectionTypes: [],
  maxPrice: 60,
  includeTV: false,
  installationService: false,
};

export default function LandingPage() {
  /* cached form --------------------------------------------------------- */
  const cached =
    (JSON.parse(localStorage.getItem("lastSearch") || "null") as
      | SearchQuery
      | null) || EMPTY_QUERY;

  const [form, setForm] = useState<SearchQuery>(cached);

  /* parse plz & city from the cached “80331 München”   */
  const parsed = cached.cityOrPostal.match(/^\\s*(\\d{4,5})\\s+(.+?)\\s*$/);
  const [plz,  setPlz ] = useState(parsed ? parsed[1] : "");
  const [city, setCity] = useState(parsed ? parsed[2] : "");

  const navigate = useNavigate();

  /* validation ---------------------------------------------------------- */
  const plzCitySugg = usePlzSuggestions(form.cityOrPostal);
  const streetSugg  = useStreetSuggestions(form.street, plz, city);

  const validPlzCity = useMemo(() => {
    if (!form.cityOrPostal.trim()) return false;
    if (plzCitySugg.some(s => s.label === form.cityOrPostal)) return true;
    return cached.cityOrPostal === form.cityOrPostal;
  }, [form.cityOrPostal, plzCitySugg, cached]);

  const validStreet = useMemo(() => {
    if (!form.street.trim()) return false;
    if (streetSugg.some(s => s.street === form.street)) return true;
    return cached.street === form.street;
  }, [form.street, streetSugg, cached]);

  const validNumber = form.number.trim() !== "";
  const formValid   = validPlzCity && validStreet && validNumber;

  /* submit -------------------------------------------------------------- */
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
    navigate("/search", { state: { query: clean } });
  };

  /* UI ------------------------------------------------------------------ */
  return (
    <section className="hero min-h-screen bg-gradient-to-br from-sky-600 via-blue-400 to-yellow-300">
      <div className="hero-overlay bg-opacity-70" />
      <div className="hero-content flex-col gap-12 w-full px-4">
        <header className="text-center space-y-4">
          <h1 className="text-6xl md:text-7xl font-black tracking-tight text-base-100 drop-shadow-lg">
            internet<span className="text-neutral-focus">CHECK</span>
          </h1>
          <p className="text-xl md:text-2xl text-base-100/90 font-light">
            Find the best internet provider at your address
          </p>
        </header>

        <form
          onSubmit={handleSubmit}
          className="card w-full max-w-4xl bg-base-100/90 backdrop-blur-md shadow-2xl p-8"
        >
          <div className="flex flex-col md:flex-row gap-6 items-stretch">
            {/* left column */}
            <div className="flex-grow flex flex-col gap-4">
              <PlzCityAutocomplete
                value={form.cityOrPostal}
                onChange={v => setForm(f => ({ ...f, cityOrPostal: v }))}
                onSelect={(p, c) => { setPlz(p); setCity(c); }}
              />

              <div className="flex gap-4">
                <StreetAutocomplete
                  className="flex-grow"
                  value={form.street}
                  onChange={v => setForm(f => ({ ...f, street: v }))}
                  plz={plz}
                  city={city}
                  disabled={!validPlzCity}
                />
                <input
                  type="number"
                  placeholder="Nr"
                  value={form.number}
                  onChange={e => setForm(f => ({ ...f, number: e.target.value }))}
                  className={`input input-bordered w-24 ${!validNumber}`}
                  disabled={!validPlzCity}
                />
              </div>
            </div>

            {/* search button */}
            <button
              type="submit"
              disabled={!formValid}
              className="btn btn-primary btn-lg md:w-48 self-stretch shadow-lg"
            >
              Search
            </button>
          </div>

          {!formValid && (
            <p className="text-error text-sm text-center mt-4">
              Please complete all address fields correctly to continue
            </p>
          )}
        </form>
      </div>
    </section>
  );
}
