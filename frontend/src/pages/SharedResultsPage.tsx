import { useEffect, useState } from "react";
import { Link, useParams }   from "react-router-dom";
import ResultsList           from "../components/ResultList";
import { fetchOffersForSession } from "../api/offers";
import type { OfferResponseDto } from "../types/offer";

type Sort = "RANK" | "PRICE" | "SPEED";
const sortOffers = (arr: OfferResponseDto[], mode: Sort) => {
  const price = (o: OfferResponseDto) =>
    o.costInfo.discountedMonthlyCostInCent || o.costInfo.monthlyCostInCent;
  const speed = (o: OfferResponseDto) => o.contractInfo?.speed ?? 0;

  const list = [...arr];
  switch (mode) {
    case "PRICE": list.sort((a, b) => price(a) - price(b)); break;
    case "SPEED": list.sort((a, b) => speed(b)  - speed(a)); break;
    default:      list.sort((a, b) => (b.averageRating ?? 0) -
                                      (a.averageRating ?? 0));
  }
  return list;
};

export default function SharedResultsPage() {
  const { sessionId = "" } = useParams();
  const [offers, setOffers] = useState<OfferResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [sort, setSort] = useState<Sort>("PRICE");

  useEffect(() => {
    fetchOffersForSession(sessionId)
      .then(setOffers)
      .catch(e => setErr(String(e)))
      .finally(() => setLoading(false));
  }, [sessionId]);

  const displayed = sortOffers(offers, sort);

  return (
    <div className="h-screen flex flex-col p-10">
      <div className="prose flex justify-between items-center mb-6">
        <div>
          <h2>Shared Results</h2>
          <p className="opacity-60 text-sm">Session ID: {sessionId}</p>
        </div>
        <select className="select select-bordered select-sm"
                value={sort}
                onChange={e => setSort(e.target.value as Sort)}>
          <option value="RANK">RATING</option>
          <option value="PRICE">PRICE</option>
          <option value="SPEED">SPEED</option>
        </select>
      </div>

      {loading && <p className="opacity-70">Loading…</p>}
      {err      && <p className="text-error">{err}</p>}
      {!loading && !err && <ResultsList offers={displayed} />}

      <Link to="/" className="link mt-6">← New search</Link>
    </div>
  );
}
