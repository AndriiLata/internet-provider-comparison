import { useEffect, useRef, useState } from "react";

/**(Straßenname ohne Hausnummer) */
export interface StreetSuggestion {
  street: string;
}

const ENDPOINT = "https://photon.komoot.io/api";

/* 
 *  Normalise city names so "a.d." == "an der", etc.            
*/
function normaliseCity(raw: string): string {
  return raw
    .toLowerCase()
    .replace(/\ba\.d\./g, "an der")
    .replace(/\ba\.m\./g, "am")
    .replace(/\ba\.w\./g, "am")
    .replace(/\bi\.d\./g, "in der")
    .replace(/\bb\./g, "bei")
    .replace(/[^\p{L}\d ]/gu, " ")
    .replace(/\s+/g, " ")
    .trim();
}

/**
 * Straßen-Vorschläge, die zu `plz` oder `city` passen
 */
export function useStreetSuggestions(
  streetQuery: string,
  plz: string,
  city: string,  
  limit = 5,
) {
  const [data, setData] = useState<StreetSuggestion[]>([]);
  const abort = useRef<AbortController | null>(null);

  /* city part before first comma, e.g. "Rottenburg a.d. Laaber"*/
  const mainCity = city.split(",")[0].trim();          // may be ""
  const normMain = mainCity ? normaliseCity(mainCity) : "";

  useEffect(() => {
    if (!streetQuery.trim() || !plz) {        
      setData([]);
      return;
    }

    const t = setTimeout(() => {
      abort.current?.abort();
      abort.current = new AbortController();

      
      const q = mainCity
        ? `${streetQuery} ${plz} ${mainCity}`
        : `${streetQuery} ${plz}`;

      const params = new URLSearchParams({
        q,
        lang: "de",
        limit: String(limit),
      }).toString();

      fetch(`${ENDPOINT}?${params}`, { signal: abort.current.signal })
        .then(r => (r.ok ? r.json() : Promise.reject(r)))
        .then(json => {
          const seen = new Set<string>();

          const streets = (json.features as any[])
            .filter(f => {
              /* postcode must always match */
              if (f.properties?.postcode !== plz) return false;

              
              if (normMain) {
                const normPhoton = normaliseCity(f.properties?.city ?? "");
                return normPhoton.includes(normMain);
              }
              
              return true;
            })
            .map(f => f.properties.street || f.properties.name)
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
  }, [streetQuery, plz, mainCity, normMain, limit]);

  return data;
}
