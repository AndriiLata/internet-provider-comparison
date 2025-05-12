
import type { OfferResponseDto, SearchCriteria } from "../types/offer";

export interface OfferStream {
  cancel: () => void;
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