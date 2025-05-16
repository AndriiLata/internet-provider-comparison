import { useEffect, useRef, useState } from "react";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";
import { streamOffers, type OfferStream } from "../api/offers";

/* cheapest-price helper for default order */
const effectivePrice = (o: OfferResponseDto) =>
  o.costInfo.discountedMonthlyCostInCent > 0
    ? o.costInfo.discountedMonthlyCostInCent
    : o.costInfo.monthlyCostInCent;

export function useOfferStream(
  criteria: SearchCriteria | null,
  initialOffers: OfferResponseDto[] = [],
) {
  const [offers, setOffers] = useState<OfferResponseDto[]>(initialOffers);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const streamRef = useRef<OfferStream | null>(null);

  useEffect(() => {
    streamRef.current?.cancel();
    if (!criteria) {
      setLoading(false);
      return;
    }

    setOffers([]);
    setError(null);
    setLoading(true);
    localStorage.setItem("lastOffers", JSON.stringify([]));

    streamRef.current = streamOffers(
      criteria,
      offer => {
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
