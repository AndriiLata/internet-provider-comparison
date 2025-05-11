// hooks/useStreetSuggestions.ts
import { useEffect, useRef, useState } from "react";

/** Vorschlag aus Photon (Straßenname ohne Hausnummer) */
export interface StreetSuggestion {
  street: string;  // "Marienplatz"
}

const ENDPOINT = "https://photon.komoot.io/api";

/**
 * Holt Straßen-Vorschläge, die zu `plz` und `city` passen
 * (Debounce 250 ms, max `limit` Resultate).
 */
export function useStreetSuggestions(
  streetQuery: string,
  plz: string,
  city: string,
  limit = 5,
) {
  const [data, setData] = useState<StreetSuggestion[]>([]);
  const abort = useRef<AbortController | null>(null);

  useEffect(() => {
    if (!streetQuery.trim() || !plz || !city) { setData([]); return; }

    const t = setTimeout(() => {
      abort.current?.abort();
      abort.current = new AbortController();

      const q = `${streetQuery} ${plz} ${city}`;
      const params = new URLSearchParams({
        q,
        lang:  "de",
        limit: String(limit),
      }).toString();

      fetch(`${ENDPOINT}?${params}`, { signal: abort.current.signal })
        .then(r => (r.ok ? r.json() : Promise.reject(r)))
        .then(json => {
          const seen = new Set<string>();
          const streets = (json.features as any[])
            .filter((f) =>
              f.properties?.postcode === plz &&
              f.properties?.city?.toLowerCase().includes(city.toLowerCase()),
            )
            .map((f) => f.properties.street || f.properties.name)
            .filter(Boolean)
            .filter((s: string) => {
              if (seen.has(s)) return false;
              seen.add(s);
              return true;
            })
            .slice(0, limit)
            .map((street: string) => ({ street }));
          setData(streets);
        })
        .catch(err => {
          if (err.name !== "AbortError") console.error("Photon-Fehler:", err);
        });
    }, 250);

    return () => clearTimeout(t);
  }, [streetQuery, plz, city, limit]);

  return data;
}
