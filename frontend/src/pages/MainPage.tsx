import { useEffect, useState } from "react";
import { useLocation, useNavigationType } from "react-router-dom";
import Sidebar, { type SearchQuery } from "../components/Sidebar";
import LoadingBanner from "../components/LoadingBanner";
import ResultsList from "../components/ResultList";
import ShareModal from "../components/ShareModal";

import {
  toCriteria,
  type OfferResponseDto,
  type SearchCriteria,
} from "../types/offer";
import { useOfferStream } from "../hooks/useOfferStream";

type Sort = "RANK" | "PRICE" | "SPEED";
const sortOffers = (arr: OfferResponseDto[], mode: Sort) => {
  const price = (o: OfferResponseDto) =>
    o.costInfo.discountedMonthlyCostInCent || o.costInfo.monthlyCostInCent;
  const speed = (o: OfferResponseDto) => o.contractInfo?.speed ?? 0;

  const list = [...arr];
  switch (mode) {
    case "PRICE":
      list.sort((a, b) => price(a) - price(b));
      break;
    case "SPEED":
      list.sort((a, b) => speed(b) - speed(a));
      break;
    default:
      list.sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0));
  }
  return list;
};

const cached: OfferResponseDto[] = (() => {
  try {
    return JSON.parse(localStorage.getItem("lastOffers") || "[]");
  } catch {
    return [];
  }
})();

export default function MainPage() {
  const loc = useLocation() as { state?: { query: SearchQuery } };
  const navOk = useNavigationType() === "PUSH" && loc.state?.query;
  const [criteria, setCrit] = useState<SearchCriteria | null>(
    navOk ? toCriteria(loc.state!.query) : null
  );
  const { offers, sessionId, loading } = useOfferStream(criteria, cached);

  useEffect(() => {
    if (offers.length)
      localStorage.setItem("lastOffers", JSON.stringify(offers));
  }, [offers]);

  const [sort, setSort] = useState<Sort>("PRICE");
  const displayed = sortOffers(offers, sort);

  const [showModal, setShowModal] = useState(false);
  const [shareUrl, setShareUrl] = useState("");

  const handleShareClick = () => {
    if (!sessionId) return;
    setShareUrl(`${window.location.origin}/share/${sessionId}`);
    setShowModal(true);
  };

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(shareUrl);
      alert("Link copied!");
    } catch {
      alert("Copy failed, please try manually.");
    }
  };

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar onSearch={(q) => setCrit(toCriteria(q))} />
      <main className="flex-1 p-10 flex flex-col">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-semibold">Results</h2>
          <div className="flex gap-3">
            <select
              className="select select-bordered select-sm"
              value={sort}
              onChange={(e) => setSort(e.target.value as Sort)}
            >
              <option value="RANK">USER RATING</option>
              <option value="PRICE">CHEAPEST FIRST</option>
              <option value="SPEED">FASTEST FIRST</option>
            </select>
            <button
              className="btn btn-outline btn-sm"
              disabled={!sessionId}
              onClick={handleShareClick}
            >
              Share Results
            </button>
          </div>
        </div>

        {/* Content */}
        {loading && <LoadingBanner offers={offers} />}
        <ResultsList offers={displayed} />

        {/* Share Modal */}
        <ShareModal
            isOpen={showModal}
            shareUrl={shareUrl}
            onClose={() => setShowModal(false)}
        />
      </main>
    </div>
  );
}