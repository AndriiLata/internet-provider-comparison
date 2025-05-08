import { useState } from "react";
import Sidebar, { type SearchQuery } from "../components/Sidebar";
import LoadingBanner from "../components/LoadingBanner";
import ResultsList from "../components/ResultList";
import { toCriteria } from "../types/offer";
import { useOfferStream } from "../hooks/useOfferStream";
import type { OfferResponseDto, SearchCriteria } from "../types/offer";

export default function MainPage() {
  // —— restore ONLY the offers ————————————
  const savedOffers: OfferResponseDto[] = (() => {
    const raw = localStorage.getItem("lastOffers");
    return raw ? JSON.parse(raw) : [];
  })();

  // criteria starts null ⇒ no refetch on reload
  const [criteria, setCriteria] = useState<SearchCriteria | null>(null);
  const { offers, loading, error } = useOfferStream(criteria, savedOffers);

  const handleSearch = (q: SearchQuery) => {
    if (!q.connectionTypes.length) {
      alert("Pick at least one connection type");
      return;
    }
    const crit = toCriteria(q);
    setCriteria(crit);                                // start new stream
    localStorage.setItem("lastCriteria", JSON.stringify(crit)); // (optional)
  };

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar onSearch={handleSearch} />

      <main className="flex-1 p-10 flex flex-col">
        <div className="prose max-w-2xl">
          <h1 className="mb-4">Results</h1>
        </div>

        {loading && <LoadingBanner />}

        <ResultsList offers={offers} />
      </main>
    </div>
  );
}
