import { useState } from "react";
import {
  useLocation,
  useNavigationType,
  type NavigationType,
} from "react-router-dom";
import Sidebar, { type SearchQuery } from "../components/Sidebar";
import LoadingBanner from "../components/LoadingBanner";
import ResultsList from "../components/ResultList";
import {
  toCriteria,
  type OfferResponseDto,
  type SearchCriteria,
} from "../types/offer";
import { useOfferStream } from "../hooks/useOfferStream";

/* helper: sort according to dropdown selection */
function sortOffers(list: OfferResponseDto[], mode: SortMode) {
  const arr = [...list];
  switch (mode) {
    case "CHEAP":
      arr.sort((a, b) => a.monthlyCostInCent - b.monthlyCostInCent);
      break;
    case "FAST":
      arr.sort((a, b) => b.speed - a.speed);
      break;
    case "BEST":
    default:
      arr.sort((a, b) => {
        const scoreA = a.monthlyCostInCent / a.speed;
        const scoreB = b.monthlyCostInCent / b.speed;
        return scoreA === scoreB
          ? a.monthlyCostInCent - b.monthlyCostInCent
          : scoreA - scoreB;
      });
  }
  return arr;
}

type SortMode = "BEST" | "CHEAP" | "FAST";

export default function MainPage() {
  /* 1 ▪ cached offers for instant paint */
  const savedOffers: OfferResponseDto[] = (() => {
    const raw = localStorage.getItem("lastOffers");
    return raw ? JSON.parse(raw) : [];
  })();

  /* 2 ▪ read router state + navigation type */
  const location = useLocation() as { state?: { query: SearchQuery } };
  const navType: NavigationType = useNavigationType(); // "PUSH" | "POP" | "REPLACE"

  /* treat as “fresh landing-page navigation” only on PUSH */
  const cameFromLanding = navType === "PUSH" && !!location.state?.query;

  /* 3 ▪ criteria:
        • PUSH from landing → start fetch
        • reload/POP        → null (show cache only)                    */
  const [criteria, setCriteria] = useState<SearchCriteria | null>(
    cameFromLanding ? toCriteria(location.state!.query) : null,
  );

  /* 4 ▪ streaming hook (fetches only when criteria != null) */
  const { offers, loading, error } = useOfferStream(criteria, savedOffers);

  /* 5 ▪ sidebar search */
  const handleSearch = (q: SearchQuery) => {
    const crit = toCriteria(q);
    setCriteria(crit);
    localStorage.setItem("lastCriteria", JSON.stringify(crit));
  };

  /* 6 ▪ sorting */
  const [sortMode, setSortMode] = useState<SortMode>("CHEAP");
  const displayed = sortOffers(offers, sortMode);

  /* 7 ▪ UI */
  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar onSearch={handleSearch} />

      <main className="flex-1 p-10 flex flex-col">
        <div className="prose flex justify-between items-center mb-4">
          <h2 className="m-3">Results</h2>

          <label className="form-control w-60 ml-auto">
            <div className="label p-0 pb-1">
              <span className="label-text text-sm opacity-70">Sort by</span>
            </div>
            <select
              className="select select-bordered select-sm"
              value={sortMode}
              onChange={(e) => setSortMode(e.target.value as SortMode)}
            >
              <option value="BEST">Best</option>
              <option value="CHEAP">Cheapest First</option>
              <option value="FAST">Fastest First</option>
            </select>
          </label>
        </div>

        {loading && <LoadingBanner offers={offers} />}
    

        <ResultsList offers={displayed} />
      </main>
    </div>
  );
}
