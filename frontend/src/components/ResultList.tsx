import OfferCard from "./offercard/OfferCard";
import type { OfferResponseDto } from "../types/offer";

interface Props {
  offers: OfferResponseDto[];
}

export default function ResultsList({ offers }: Props) {
  return (
    <div className="flex-1 min-h-0 overflow-y-auto pr-2">
      {offers.length === 0 ? (
        <p className="opacity-70">
          No offers yet – start by entering your address 👆
        </p>
      ) : (
        offers.map((o) => <OfferCard key={o.productId} offer={o} />)
      )}
    </div>
  );
}
