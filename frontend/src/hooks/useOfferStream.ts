import { useEffect, useRef, useState } from "react";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";
import { streamOffers, type OfferStream } from "../api/offers";

/* helper: calc price used for default sort & CHEAP mode */
const effectivePrice = (o: OfferResponseDto) =>
  o.costInfo.discountedMonthlyCostInCent > 0
    ? o.costInfo.discountedMonthlyCostInCent
    : o.costInfo.monthlyCostInCent;

/* add a random rating once (kept stable in state/localStorage) */
const withRating = (o: OfferResponseDto): OfferResponseDto =>
  o.avgRating == null
    ? { ...o, avgRating: Math.round(Math.random() * 50) / 10 } // 0.0-5.0
    : o;

export function useOfferStream(
  criteria: SearchCriteria | null,
  initialOffers: OfferResponseDto[] = [],
) {
  /* apply rating to cached offers, too */
  const [offers, setOffers] = useState<OfferResponseDto[]>(
    initialOffers.map(withRating),
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const streamRef = useRef<OfferStream | null>(null);

  useEffect(() => {
    // cancel old stream
    streamRef.current?.cancel();

    if (!criteria) {
      setLoading(false);
      return;
    }

    // ── new search ──────────────────────────────────────────────
    setOffers([]);
    setError(null);
    setLoading(true);
    localStorage.setItem("lastOffers", JSON.stringify([]));

    streamRef.current = streamOffers(
      criteria,
      raw => {
        const offer = withRating(raw);
        setOffers(prev => {
          const next = [...prev, offer];
          next.sort((a, b) => effectivePrice(a) - effectivePrice(b));
          localStorage.setItem("lastOffers", JSON.stringify(next));
          return next;
        });
      },
      () => setLoading(false),
      err => {
        console.error("Stream error", err);
        setError(err);
        setLoading(false);
      },
    );

    return () => streamRef.current?.cancel();
  }, [criteria]);

  return { offers, loading, error };
}
