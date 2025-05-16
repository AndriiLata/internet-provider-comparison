import { useEffect, useState } from "react";
import type { OfferResponseDto } from "../types/offer";
import { fetchRatings } from "../api/ratings";
import type { RatingResponseDto } from "../types/ratings";
import dayjs from "dayjs";

interface Props {
  offer: OfferResponseDto;
  open: boolean;
  onClose: () => void;
}

const euro = (c: number) => (c / 100).toFixed(2);

export default function MoreInfoModal({ offer, open, onClose }: Props) {
  const { provider, contractInfo, costInfo, tvInfo } = offer;

  const [reviews, setReviews] = useState<RatingResponseDto[]>([]);
  const [showReviews, setShowReviews] = useState(false);
  const [loadingReviews, setLoadingReviews] = useState(false);

  useEffect(() => {
    if (!showReviews) return;
    setLoadingReviews(true);
    fetchRatings(offer.provider)
      .then(setReviews)
      .finally(() => setLoadingReviews(false));
  }, [showReviews, offer.provider]);

  return (
    open && (
      <dialog className="modal modal-open" onCancel={onClose}>
        <div className="modal-box w-11/12 max-w-3xl">
          <h3 className="font-bold text-xl mb-6">{provider} – Details</h3>

          {/* details grid */}
          <div className="grid md:grid-cols-2 gap-6">
            <section>
              <h4 className="font-semibold mb-2">Contract</h4>
              <ul className="list-disc list-inside text-sm space-y-1">
                <li>Type: {contractInfo.connectionType}</li>
                <li>Speed: {contractInfo.speed} Mbit/s</li>
                {contractInfo.contractDurationInMonths && (
                  <li>Duration: {contractInfo.contractDurationInMonths} months</li>
                )}
                {contractInfo.speedLimitFrom && (
                  <li>Limitation from {contractInfo.speedLimitFrom} GB</li>
                )}
                {contractInfo.maxAge && <li>Max age: {contractInfo.maxAge}</li>}
              </ul>
            </section>

            <section>
              <h4 className="font-semibold mb-2">Costs</h4>
              <ul className="list-disc list-inside text-sm space-y-1">
                <li>Monthly: €{euro(costInfo.monthlyCostInCent)}</li>
                {costInfo.discountedMonthlyCostInCent !==
                  costInfo.monthlyCostInCent && (
                  <li>
                    Discounted: €{euro(costInfo.discountedMonthlyCostInCent)}
                  </li>
                )}
                {costInfo.monthlyCostAfter24mInCent && (
                  <li>
                    From month 25: €{euro(costInfo.monthlyCostAfter24mInCent)}
                  </li>
                )}
                {costInfo.installationService && <li>Installation included</li>}
              </ul>
            </section>

            {tvInfo.tvIncluded && (
              <section className="md:col-span-2">
                <h4 className="font-semibold mb-2">TV</h4>
                <p className="text-sm">
                  Included – Brand: {tvInfo.tvBrand ?? "n/a"}
                </p>
              </section>
            )}
          </div>

          {/* reviews toggle */}
          <div className="mt-8">
            <button
              className="btn btn-outline btn-sm"
              onClick={() => setShowReviews(!showReviews)}
            >
              {showReviews ? "Hide Reviews" : "View Reviews"}
            </button>
          </div>

          {showReviews && (
            <div className="mt-6 max-h-[45vh] overflow-y-auto pr-1">
              {loadingReviews ? (
                <div className="flex justify-center py-6">
                  <span className="loading loading-spinner" />
                </div>
              ) : reviews.length === 0 ? (
                <p className="opacity-70 text-sm">No reviews yet.</p>
              ) : (
                <div className="flex flex-col gap-4">
                  {reviews.map(r => (
                    <article
                      key={r.userName + r.createdAt}
                      className="p-4 border border-base-200 rounded-lg bg-base-100"
                    >
                      <header className="flex justify-between items-center mb-2">
                        <span className="font-semibold">{r.userName}</span>
                        <time className="text-sm opacity-60">
                          {dayjs(r.createdAt).format("DD MMM YYYY")}
                        </time>
                      </header>

                      <div className="rating rating-sm mb-2">
                        {[1, 2, 3, 4, 5].map(i => (
                          <input
                            key={i}
                            type="radio"
                            name={"rev-" + r.createdAt}
                            className="mask mask-star-2 bg-orange-400"
                            readOnly
                            checked={i === r.ranking}
                          />
                        ))}
                      </div>

                      {r.comment && (
                        <p className="text-sm whitespace-pre-wrap leading-relaxed">
                          {r.comment}
                        </p>
                      )}
                    </article>
                  ))}
                </div>
              )}
            </div>
          )}

          <div className="modal-action mt-8">
            <button className="btn" onClick={onClose}>
              Close
            </button>
          </div>
        </div>
      </dialog>
    )
  );
}
