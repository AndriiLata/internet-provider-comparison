import type { SearchQuery } from "../components/Sidebar";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";

export interface OfferStream {
  cancel: () => void;
}

function splitCityPostal(raw: string): { city: string; postalCode: string } {
    const trimmed = raw.trim();
    const m = trimmed.match(/(\d{5})/);          // look for a 5-digit number
    if (!m) return { city: trimmed, postalCode: "" };
  
    const postal = m[1];
    const city = trimmed
      .replace(postal, "")
      .replace(/[,\s]+$/, "")                     // kill trailing space/comma
      .replace(/^\s+/, "")                       // kill leading space
      .trim();
  
    return { city, postalCode: postal };
  }
  
  export function toCriteria(q: SearchQuery): SearchCriteria {
    const { city, postalCode } = splitCityPostal(q.cityOrPostal);
  
    return {
      street: q.street.trim(),
      houseNumber: parseInt(q.number.trim(), 10) || 0,   // 👈 integer!
      city,
      postalCode,
      connectionType:
        q.connectionTypes.length === 1 ? q.connectionTypes[0] : null,
      maxPriceInCent: q.maxPrice ? q.maxPrice * 100 : null,
      includeTv: q.includeTV,
      includeInstallation: q.installationService,
    };
  }

/**
 * Starts a POST /api/offers/stream request and calls the supplied callbacks
 * for every SSE `data:` event.
 */
export function streamOffers(
  criteria: SearchCriteria,
  onOffer: (offer: OfferResponseDto) => void,
  onFinished: () => void,
  onError: (err: unknown) => void
): OfferStream {
  const controller = new AbortController();
  console.log("▶ sending criteria", criteria);

  fetch("/api/offers/stream", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "text/event-stream",
    },
    body: JSON.stringify(criteria),
    signal: controller.signal,
  })
    .then(async (res) => {
      const reader = res.body?.getReader();
      if (!reader) throw new Error("No readable body on response");
      const decoder = new TextDecoder();
      let buffer = "";
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split("\n\n"); // SSE events are separated by blank line
        buffer = parts.pop() ?? ""; // last chunk (maybe incomplete)
        for (const part of parts) {
          const line = part.trimStart();
          if (line.startsWith("data:")) {
            const json = line.replace(/^data:\s*/, "");
            try {
              const dto: OfferResponseDto = JSON.parse(json);
              onOffer(dto);
            } catch (e) {
              console.error("Could not parse DTO", e);
            }
          }
        }
      }
      onFinished();
    })
    .catch((err) => {
      if ((err as any)?.name === "AbortError") return; // ignore cancel
      onError(err);
    });

  return {
    cancel() {
      controller.abort();
    },
  };
}