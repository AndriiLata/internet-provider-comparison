import { fetchEventSource, type EventSourceMessage } from "@microsoft/fetch-event-source";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";

/** Open POST-SSE for a search; returns the AbortController. */
export function streamOffers(
  criteria:   SearchCriteria,
  onId:       (id: string)               => void,
  onOffer:    (o:  OfferResponseDto)     => void,
  onError:    (e: unknown)               => void,
  onClose:    ()                         => void,   // 🔸 new
) {
  const ctrl = new AbortController();

  fetchEventSource("/api/offers/stream", {
    method:  "POST",
    headers: { "Content-Type": "application/json" },
    body:    JSON.stringify(criteria),
    signal:  ctrl.signal,

    onopen:  async () => {},               // must return Promise<void>
    onclose: () => { onClose(); },         // 🔸 notify hook
    onerror: e  => { onError(e); },

    onmessage(msg: EventSourceMessage) {
      if (msg.event === "sessionId") {
        try { onId(JSON.parse(msg.data).sessionId); } catch {}
        return;
      }
      try { onOffer(JSON.parse(msg.data) as OfferResponseDto); } catch {}
    },
  });

  return ctrl;
}

/** Fetch full list for an existing share-ID. */
export async function fetchOffersForSession(id: string) {
  const r = await fetch(`/api/offers/session/${id}`);
  if (!r.ok) throw new Error(`HTTP ${r.status}`);
  return (await r.json()) as OfferResponseDto[];
}
