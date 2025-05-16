// src/pages/MainPage.tsx

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

type SortMode = "RANK" | "PRICE" | "SPEED";

/** Sort helper with guards for missing costInfo/contractInfo */
function sortOffers(list: OfferResponseDto[], mode: SortMode) {
    const arr = [...list];
    const price = (o: OfferResponseDto) => {
      const c = o.costInfo;
      return c.discountedMonthlyCostInCent > 0
        ? c.discountedMonthlyCostInCent
        : c.monthlyCostInCent;
    };
    const speedVal = (o: OfferResponseDto) => o.contractInfo?.speed ?? 0;
  
    switch (mode) {
      case "PRICE":
        arr.sort((a, b) => price(a) - price(b));
        break;
      case "SPEED":
        arr.sort((a, b) => speedVal(b) - speedVal(a));
        break;
      case "RANK":
      default:
        arr.sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0));
    }
    return arr;
  }

export default function MainPage() {
  // Filter out any cached offers that don’t match the new DTO shape:
  const savedOffers: OfferResponseDto[] = (() => {
    const raw = localStorage.getItem("lastOffers");
    if (!raw) return [];
    try {
      const arr = JSON.parse(raw);
      if (!Array.isArray(arr)) return [];
      return arr.filter((o) => o.costInfo != null);
    } catch {
      return [];
    }
  })();

  const location = useLocation() as { state?: { query: SearchQuery } };
  const navType: NavigationType = useNavigationType();
  const cameFromLanding = navType === "PUSH" && !!location.state?.query;

  const [criteria, setCriteria] = useState<SearchCriteria | null>(
    cameFromLanding ? toCriteria(location.state!.query) : null
  );

  const { offers, loading, error } = useOfferStream(criteria, savedOffers);

  const handleSearch = (q: SearchQuery) => {
    const crit = toCriteria(q);
    setCriteria(crit);
    localStorage.setItem("lastCriteria", JSON.stringify(crit));
  };

  const [sortMode, setSortMode] = useState<SortMode>("PRICE");
  const displayed = sortOffers(offers, sortMode);

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar onSearch={handleSearch} />

      <main className="flex-1 p-10 flex flex-col">
        <div className="prose flex justify-between items-center mb-4">
          <h2 className="m-3">Results</h2>
          <label className="form-control w-60 ml-auto">
            <div className="label p-0 pb-1">
              <span className="label-text text-sm opacity-70">
                Sort by
              </span>
            </div>
            <select
              className="select select-bordered select-sm"
              value={sortMode}
              onChange={(e) => setSortMode(e.target.value as SortMode)}
            >
              <option value="RANK">RATING</option>
              <option value="PRICE">PRICE</option>
              <option value="SPEED">SPEED</option>
            </select>
          </label>
        </div>

        {loading && <LoadingBanner offers={offers} />}
    

        <ResultsList offers={displayed} />
      </main>
    </div>
  );
}
