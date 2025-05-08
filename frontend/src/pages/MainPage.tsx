import { useState } from "react";
import Sidebar, { type SearchQuery } from "../components/Sidebar";
import LoadingBanner from "../components/LoadingBanner";
import ResultsList from "../components/ResultList";
import { toCriteria } from "../types/offer";
import { useOfferStream } from "../hooks/useOfferStream";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";

// ──────────────────────────────────────────────────────────────
// helper: sort according to dropdown selection
// ──────────────────────────────────────────────────────────────
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
          // price‑per‑Mbps (lower is better) → then absolute price
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
    // — restore cached offers only —
    const savedOffers: OfferResponseDto[] = (() => {
      const raw = localStorage.getItem("lastOffers");
      return raw ? JSON.parse(raw) : [];
    })();
  
    const [criteria, setCriteria] = useState<SearchCriteria | null>(null);
    const [sortMode, setSortMode] = useState<SortMode>("CHEAP");
    const { offers, loading, error } = useOfferStream(criteria, savedOffers);
  
    const handleSearch = (q: SearchQuery) => {
      const crit = toCriteria(q);
      setCriteria(crit);
      localStorage.setItem("lastCriteria", JSON.stringify(crit));
    };
  
    // get a memoised, sorted copy each render
    const displayed = sortOffers(offers, sortMode);
  
    return (
      <div className="h-screen flex overflow-hidden">
        <Sidebar onSearch={handleSearch} />
  
        <main className="flex-1 p-10 flex flex-col">
          {/* header row */}
          <div className="prose flex justify-between items-center mb-4">
            <h2 className="m-3">Results</h2>
  
            {/* — Sort dropdown — */}
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
  
          {loading && <LoadingBanner />}
        
  
          <ResultsList offers={displayed} />
        </main>
      </div>
    );
  }
