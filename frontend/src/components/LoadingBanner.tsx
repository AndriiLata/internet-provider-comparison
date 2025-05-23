import { useState, useEffect } from "react";
import type { OfferResponseDto } from "../types/offer";

interface Props {
  offers: OfferResponseDto[];
}

// providers we expect to get offers from
const expectedProviders = [
  { key: "ping", label: "Ping Perfect" },
  { key: "byte", label: "Byte Me" },
  { key: "verbyndich", label: "VerbynDich" },
  { key: "webwunder", label: "WebWunder" },
  { key: "servus", label: "Servus Speed" },
];

export default function LoadingBanner({ offers }: Props) {
  // which providers are still pending
  const pending = expectedProviders.filter(
    ({ key }) =>
      !offers.some((o) =>
        o.provider.toLowerCase().includes(key.toLowerCase())
      )
  );

  const total = expectedProviders.length;
  const checked = total - pending.length;

  const [idx, setIdx] = useState(0);

  // reset rotation when pending list changes
  useEffect(() => {
    setIdx(0);
  }, [pending.length]);

  // rotate every 1 second
  useEffect(() => {
    if (pending.length === 0) return;
    const timer = setInterval(() => {
      setIdx((i) => (i + 1) % pending.length);
    }, 1000);
    return () => clearInterval(timer);
  }, [pending]);

  const waitingFor =
  pending.length > 0
    ? pending[idx]?.label ?? pending[0].label
    : "last results";


  return (
    <div className="alert shadow-lg mb-6 bg-base-200 flex items-center">
      <span className="loading loading-spinner loading-sm mr-2"></span>
      <span>
        Fetching offers… Waiting for{" "}
        <strong>{waitingFor}</strong> — Checked {checked}/{total} providers
      </span>
    </div>
  );
}
