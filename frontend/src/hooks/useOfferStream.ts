import { useEffect, useRef, useState } from "react";
import type {
    OfferResponseDto,
    SearchCriteria,
} from "../types/offer";
import { streamOffers, type OfferStream } from "../api/offers";

export function useOfferStream(
    criteria: SearchCriteria | null,
    initialOffers: OfferResponseDto[] = []
  ) {
    const [offers, setOffers]   = useState<OfferResponseDto[]>(initialOffers);
    const [loading, setLoading] = useState(false);
    const [error,   setError]   = useState<unknown>(null);
    const streamRef = useRef<OfferStream | null>(null);
  
    useEffect(() => {
      // cancel any open stream
      streamRef.current?.cancel();
  
      if (!criteria) {
        // reload (or user hasn’t searched yet) ⇒ just show cached offers
        setLoading(false);
        return;
      }
  
      // ── a NEW search is starting ───────────────────────────────
      setOffers([]);
      setError(null);
      setLoading(true);
      localStorage.setItem("lastOffers", JSON.stringify([]));   // clear cache
  
      streamRef.current = streamOffers(
        criteria,
        offer =>
          setOffers(prev => {
            const next = [...prev, offer];
            next.sort((a, b) => a.monthlyCostInCent - b.monthlyCostInCent);
            localStorage.setItem("lastOffers", JSON.stringify(next)); // cache
            return next;
          }),
        () => setLoading(false),
        err => {
          console.error("Stream error", err);
          setError(err);
          setLoading(false);
        }
      );
  
      return () => streamRef.current?.cancel();
    }, [criteria]);
  
    return { offers, loading, error };
  }
  
  