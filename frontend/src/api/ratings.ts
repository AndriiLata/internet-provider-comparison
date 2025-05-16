import type { RatingRequestDto, RatingResponseDto } from "../types/ratings";

/** POST stays the same */
export async function submitRating(dto: RatingRequestDto) {
  const res = await fetch("/api/ratings", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error("Could not send rating");
}

/** ← NEW: NDJSON-aware fetch */
export async function fetchRatings(
  serviceName: string,
): Promise<RatingResponseDto[]> {
  const res = await fetch(`/api/ratings/${encodeURIComponent(serviceName)}`, {
    headers: {
      // ask for JSON or NDJSON; backend will pick what it likes
      Accept: "application/json, application/x-ndjson",
    },
  });
  if (!res.ok) throw new Error("Could not load ratings");

  const ct = res.headers.get("Content-Type") ?? "";
  /* regular JSON array */
  if (ct.includes("application/json") && !ct.includes("ndjson")) {
    return res.json();
  }

  /* fall back to NDJSON/stream parsing */
  const reader = res.body?.getReader();
  if (!reader) return [];

  const decoder = new TextDecoder();
  let buffer = "";
  const list: RatingResponseDto[] = [];

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const parts = buffer.split("\n");
    buffer = parts.pop() ?? "";
    for (const part of parts) {
      const trimmed = part.trim();
      if (trimmed) list.push(JSON.parse(trimmed));
    }
  }
  if (buffer.trim()) list.push(JSON.parse(buffer));
  return list;
}
