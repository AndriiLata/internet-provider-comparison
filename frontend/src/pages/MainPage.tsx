import { useEffect, useState } from "react";
import { useLocation, useNavigationType } from "react-router-dom";
import Sidebar, { type SearchQuery } from "../components/Sidebar";
import LoadingBanner   from "../components/LoadingBanner";
import ResultsList     from "../components/ResultList";
import { toCriteria,
         type OfferResponseDto,
         type SearchCriteria }         from "../types/offer";
import { useOfferStream } from "../hooks/useOfferStream";

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

/* ---------- bootstrap cached offers ---------- */
const cached: OfferResponseDto[] = (() => {
  try { return JSON.parse(localStorage.getItem("lastOffers") || "[]"); }
  catch { return []; }
})();

export default function MainPage() {
  /* form-state coming from landing page? */
  const loc   = useLocation() as { state?: { query: SearchQuery } };
  const navOk = useNavigationType() === "PUSH" && loc.state?.query;
  const [criteria, setCrit] = useState<SearchCriteria | null>(
    navOk ? toCriteria(loc.state!.query) : null);

  const { offers, sessionId, loading } = useOfferStream(criteria, cached);

  /* keep cache fresh */
  useEffect(() => {
    if (offers.length) localStorage.setItem("lastOffers", JSON.stringify(offers));
  }, [offers]);

  /* share link */
  const copy = async () => {
    if (!sessionId) return;
    await navigator.clipboard.writeText(`${location.origin}/#/share/${sessionId}`);
    alert("Share link copied 📋");
  };

  /* sorting */
  const [sort, setSort] = useState<Sort>("PRICE");
  const displayed = sortOffers(offers, sort);

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar onSearch={q => setCrit(toCriteria(q))} />
      <main className="flex-1 p-10 flex flex-col">
        <div className="prose flex justify-between items-center mb-4">
          <h2>Results</h2>
          <div className="flex gap-3">
            <select className="select select-bordered select-sm"
                    value={sort}
                    onChange={e => setSort(e.target.value as Sort)}>
              <option value="RANK">RATING</option>
              <option value="PRICE">PRICE</option>
              <option value="SPEED">SPEED</option>
            </select>
            <button className="btn btn-outline btn-sm"
                    disabled={!sessionId}
                    onClick={copy}>Share Results</button>
          </div>
        </div>

        {loading && <LoadingBanner offers={offers} />}
        <ResultsList offers={displayed} />
      </main>
    </div>
  );
}
