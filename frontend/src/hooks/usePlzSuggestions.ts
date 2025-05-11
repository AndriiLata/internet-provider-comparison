// hooks/usePlzSuggestions.ts
import { useEffect, useRef, useState } from "react";

export interface PlzSuggestion {
  label: string;   // "80331 München"
  plz: string;     // "80331"
}

const ENDPOINT = "https://public.opendatasoft.com/api/records/1.0/search/";
const DATASET  = "georef-germany-postleitzahl";

export function usePlzSuggestions(query: string, limit = 5) {
  const [data, setData] = useState<PlzSuggestion[]>([]);
  const abort = useRef<AbortController | null>(null);

  useEffect(() => {
    if (!query.trim()) { setData([]); return; }

    const debounce = setTimeout(() => {
      abort.current?.abort();
      abort.current = new AbortController();

      const isNumeric = /^\d+$/.test(query);
      const q = isNumeric
        ? `#startswith(plz_name_long,"${query}")`   // Präfix-Suche
        : `#search(plz_name_long,"${query}")`;      // Unterstring-Suche

      /* URLSearchParams kodiert das # korrekt zu %23  */
      const params = new URLSearchParams({
        dataset: DATASET,
        q,
        rows: String(limit),
        fields: "plz,plz_name_long",
      }).toString();

      fetch(`${ENDPOINT}?${params}`, { signal: abort.current.signal })
        .then(r => (r.ok ? r.json() : Promise.reject(r)))
        .then(json =>
          setData(
            json.records.map((rec: any) => ({
              label: rec.fields.plz_name_long,
              plz:   rec.fields.plz,
            })),
          ),
        )
        .catch(err => {
          if (err.name !== "AbortError") console.error("PLZ-API-Fehler:", err);
        });
    }, 250);

    return () => clearTimeout(debounce);
  }, [query, limit]);

  return data;
}
