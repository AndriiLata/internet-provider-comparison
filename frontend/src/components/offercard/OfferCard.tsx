import { useState } from "react";
import type { OfferResponseDto } from "../../types/offer";
import MoreInfoModal from "./MoreInfoModal";
import RankServiceModal from "./RankServiceModal";

// logos
import byteMeLogo      from "../../assets/logos/byteme.jpg";
import pingPerfectLogo from "../../assets/logos/pingperfect.jpg";
import servusSpeedLogo from "../../assets/logos/servusspeed.jpg";
import verbynDichLogo  from "../../assets/logos/verbyndich.jpg";
import webWunderLogo   from "../../assets/logos/webwunder.jpg";


const logoMap: Record<string, string> = {
  byte:       byteMeLogo,
  ping:       pingPerfectLogo,
  servus:     servusSpeedLogo,
  verbyndich: verbynDichLogo,
  webwunder:  webWunderLogo,
};

const euro = (c: number) => (c / 100).toFixed(2);

export default function OfferCard({ offer }: { offer: OfferResponseDto }) {
  const {
    productId,
    provider,
    contractInfo,
    costInfo,
    tvInfo,
    averageRating: rawRating,
  } = offer;

  // make sure there is always a number
  const rating = rawRating ?? 0;
  const fullStars = Math.round(rating);

  // find the logo by checking if the provider name contains the key
  const providerKey = Object.keys(logoMap).find(k =>
    provider.toLowerCase().includes(k),
  );
  const logoSrc = providerKey ? logoMap[providerKey] : null;

  // check discount
  const hasDiscount =
    costInfo.discountedMonthlyCostInCent !== costInfo.monthlyCostInCent;

  const priceCent = hasDiscount
    ? costInfo.discountedMonthlyCostInCent
    : costInfo.monthlyCostInCent;


  
  const [infoOpen, setInfoOpen] = useState(false);
  const [rankOpen, setRankOpen] = useState(false);

  return (
    <>
      <div className="card bg-base-100 shadow-md hover:shadow-xl transition duration-300">
        <div className="card-body p-4 flex flex-row items-center gap-6">
          {/* provider column */}
          <div className="flex flex-col items-center gap-2 w-32 shrink-0">
            {logoSrc ? (
              <img
                src={logoSrc}
                alt={`${provider} logo`}
                className="w-14 h-14 object-contain rounded-md"
              />
            ) : (
              <div className="w-14 h-14 rounded-md bg-neutral/10 grid place-items-center">
                <span className="text-xs opacity-60">Logo</span>
              </div>
            )}
            <span className="font-semibold text-center">{provider}</span>
          </div>

          {/* rating column */}
          <div className="flex flex-col items-center w-28 shrink-0">
            <span className="text-xl font-bold text-primary mb-1">
              {rating.toFixed(1)}
            </span>
            <div className="rating rating-sm">
              {[1, 2, 3, 4, 5].map(i => (
                <input
                  key={i}
                  type="radio"
                  name={`rating-${productId}`}
                  className="mask mask-star-2 bg-orange-400"
                  readOnly
                  checked={i === fullStars}
                />
              ))}
            </div>
          </div>

          {/* speed/details column */}
          <div className="flex flex-col gap-1 flex-grow min-w-[10rem]">
            <span className="text-3xl font-bold">
              {contractInfo.speed}
              <span className="text-base font-medium"> Mbit/s</span>
            </span>

            <div className="flex flex-wrap gap-1 text-xs opacity-80">
              <span className="badge badge-outline capitalize">
                {contractInfo.connectionType}
              </span>
              {contractInfo.speedLimitFrom && (
                <span className="badge badge-outline">
                  Limit from {contractInfo.speedLimitFrom} GB
                </span>
              )}
              {contractInfo.contractDurationInMonths && (
                <span className="badge badge-outline">
                  {contractInfo.contractDurationInMonths} months
                </span>
              )}
              {contractInfo.maxAge && (
                <span className="badge badge-outline">
                  Age ≤ {contractInfo.maxAge}
                </span>
              )}
              {tvInfo.tvIncluded && tvInfo.tvBrand && (
                <span className="badge badge-outline">TV: {tvInfo.tvBrand}</span>
              )}
              {costInfo.installationService && (
                <span className="badge badge-outline">Installation incl.</span>
              )}
            </div>
          </div>

          {/* price column */}
          <div className="flex flex-col items-end w-48 shrink-0">
            {hasDiscount && (
              <div className="flex items-center gap-2 mb-1">
                <span className="line-through text-sm opacity-60">
                  €{euro(costInfo.monthlyCostInCent)}
                </span>
                <span className="text-error font-semibold">
                  –€{euro(
                    costInfo.monthlyCostInCent -
                      costInfo.discountedMonthlyCostInCent,
                  )}
                </span>
              </div>
            )}

            <div className="flex items-baseline gap-1">
              <span className="text-3xl font-extrabold text-primary">
                €{euro(priceCent)}
              </span>
              <span className="text-sm opacity-70">/ Month</span>
            </div>

            {costInfo.monthlyCostAfter24mInCent && (
              <span className="text-xs opacity-70 mt-1">
                from month 25: €{euro(costInfo.monthlyCostAfter24mInCent)}
              </span>
            )}

            <div className="flex gap-2 mt-2">
              <button
                className="btn btn-soft btn-sm"
                onClick={() => setInfoOpen(true)}
              >
                More Info
              </button>
              <button
                className="btn btn-warning btn-sm"
                onClick={() => setRankOpen(true)}
              >
                RATE SERVICE
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* modals */}
      <MoreInfoModal
        offer={offer}
        open={infoOpen}
        onClose={() => setInfoOpen(false)}
      />
      <RankServiceModal
        serviceName={provider}
        open={rankOpen}
        onClose={() => setRankOpen(false)}
      />
    </>
  );
}
