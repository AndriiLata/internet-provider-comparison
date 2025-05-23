import { useEffect, useRef, useState } from "react";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";
import { streamOffers } from "../api/offers";

interface Result {
  offers:     OfferResponseDto[];
  sessionId:  string | null;
  loading:    boolean;
  error:      string | null;
}

export function useOfferStream(
  criteria: SearchCriteria | null,
  initial:  OfferResponseDto[] = [],
): Result {
  const [offers,    setOffers]   = useState(initial);
  const [sessionId, setId]       = useState<string | null>(null);
  const [loading,   setLoading]  = useState(false);
  const [error,     setError]    = useState<string | null>(null);

  const ctrlRef   = useRef<AbortController | null>(null);
  const bufferRef = useRef<OfferResponseDto[]>(initial);

  useEffect(() => {
    // cancel previous request!!
    ctrlRef.current?.abort();

    if (!criteria) return;

    setOffers([]);
    bufferRef.current = [];
    setId(null);
    setLoading(true);
    setError(null);

    ctrlRef.current = streamOffers(
      criteria,
      setId,
      o => {
        bufferRef.current = [...bufferRef.current, o];
        setOffers(bufferRef.current);
      },
      e => { setError(String(e)); setLoading(false); },
      () => {                      // onClose
        setLoading(false);
        localStorage.setItem("lastOffers", JSON.stringify(bufferRef.current));
      },
    );

    return () => ctrlRef.current?.abort();
  }, [criteria]);

  return { offers, sessionId, loading, error };
}
