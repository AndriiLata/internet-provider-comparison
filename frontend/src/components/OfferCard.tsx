import type { OfferResponseDto } from "../types/offer";

interface Props {
  offer: OfferResponseDto;
}

export default function OfferCard({ offer }: Props) {
  return (
    <div className="card bg-base-100 shadow-md mb-4">
      <div className="card-body p-4">
        <h3 className="card-title text-lg font-medium">{offer.provider}</h3>
        <p>
          {offer.speed} Mbit/s • {offer.connectionType}
        </p>
        <p className="text-primary font-semibold">
          €{(offer.monthlyCostInCent / 100).toFixed(2)}/mo
        </p>
      </div>
    </div>
  );
}