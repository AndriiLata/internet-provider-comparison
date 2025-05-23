import { useEffect, useState } from "react";
import type { OfferResponseDto } from "../../types/offer";
import { fetchRatings } from "../../api/ratings";
import type { RatingResponseDto } from "../../types/ratings";
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
    fetchRatings(provider)
      .then(setReviews)
      .finally(() => setLoadingReviews(false));
  }, [showReviews, provider]);

  const original = costInfo.monthlyCostInCent;
  const discounted = costInfo.discountedMonthlyCostInCent;
  const after24 = costInfo.monthlyCostAfter24mInCent;
  const saveAmount = original - discounted;

  return (
    <dialog
      className={`modal ${open ? "modal-open" : ""}`}
      onClose={onClose}
      onCancel={onClose}
    >
      {/* Modal adjusts height based on reviews */}
      <div
        className={
          `modal-box w-11/12 max-w-3xl p-0 flex flex-col transition-all duration-300 ` +
          (showReviews
            ? 'h-[90vh]'
            : 'max-h-[70vh]')
        }
      >
        {/* Header */}
        <div className="flex justify-between items-center p-4 border-b">
          <h3 className="font-bold text-2xl">{provider} – Offer Details</h3>
          <button className="btn btn-ghost" onClick={onClose}>✖</button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {/* Info Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Connection */}
            <div className="card bg-base-100 shadow p-4">
              <h4 className="card-title">Connection</h4>
              <dl className="mt-2 space-y-2 text-sm">
                <div className="flex">
                  <dt className="font-semibold w-32">Type:</dt>
                  <dd>{contractInfo.connectionType}</dd>
                </div>
                <div className="flex">
                  <dt className="font-semibold w-32">Speed:</dt>
                  <dd>{contractInfo.speed} Mbit/s</dd>
                </div>
                {contractInfo.contractDurationInMonths && (
                  <div className="flex">
                    <dt className="font-semibold w-32">Duration:</dt>
                    <dd>{contractInfo.contractDurationInMonths} months</dd>
                  </div>
                )}
                {contractInfo.speedLimitFrom && (
                  <div className="flex">
                    <dt className="font-semibold w-32">Throttle from:</dt>
                    <dd>{contractInfo.speedLimitFrom} GB</dd>
                  </div>
                )}
                {contractInfo.maxAge && (
                  <div className="flex">
                    <dt className="font-semibold w-32">Max age:</dt>
                    <dd>{contractInfo.maxAge} years</dd>
                  </div>
                )}
                {costInfo.installationService && (
                  <div className="flex">
                    <dt className="font-semibold w-32">Installation:</dt>
                    <dd>{contractInfo.maxAge} INCLUDED</dd>
                  </div>
                )}
              </dl>
            </div>

            {/* Costs */}
            <div className="card bg-base-100 shadow p-4">
              <h4 className="card-title">Pricing</h4>
              <ul className="mt-4 space-y-2 text-sm">
                <li>
                  <span className="font-semibold">Initial Price:</span>{" "}
                  €{euro(original)}/mo
                </li>
                {discounted < original && (
                  <li>
                    <span className="font-semibold">Discounted Price:</span>{" "}
                    <span className="text-black-600">€{euro(discounted)}/mo</span>
                    <span className="ml-2 badge badge-warning">Save €{euro(saveAmount)}</span>
                  </li>
                )}
                {after24 && (
                  <li>
                    <span className="font-semibold">After 24 Months:</span>{" "}
                    €{euro(after24)}/mo
                  </li>
                )}
            
              </ul>
            </div>

            {/* TV */}
            {tvInfo.tvIncluded && (
              <div className="card bg-base-100 shadow p-4 md:col-span-2">
                <h4 className="card-title">TV Package</h4>
                <p className="mt-2 text-sm">Included: {tvInfo.tvBrand ?? "n/a"}</p>
              </div>
            )}
          </div>

          {/* Reviews Toggle */}
          <button className="btn btn-outline" onClick={() => setShowReviews(v => !v)}>
            {showReviews ? "Hide Reviews" : "View Reviews"}
          </button>

          {/* Reviews */}
          {showReviews && (
            <div className="mt-4 space-y-4">
              {loadingReviews ? (
                <div className="flex justify-center py-6">
                  <span className="loading loading-spinner text-primary" />
                </div>
              ) : reviews.length === 0 ? (
                <p className="opacity-70 text-sm">No reviews yet.</p>
              ) : (
                reviews.map(r => (
                  <article key={r.userName + r.createdAt} className="p-4 border border-base-200 rounded-lg bg-base-100">
                    <header className="flex justify-between items-center mb-2">
                      <span className="font-semibold">{r.userName}</span>
                      <time className="text-xs opacity-60">{dayjs(r.createdAt).format("DD MMM YYYY")}</time>
                    </header>
                    <div className="rating rating-sm mb-2">
                      {[1, 2, 3, 4, 5].map(i => (
                        <input key={i} type="radio" name={"rev-" + r.createdAt} className="mask mask-star-2 bg-warning" readOnly checked={i === r.ranking} />
                      ))}
                    </div>
                    {r.comment && <p className="text-sm whitespace-pre-wrap leading-relaxed">{r.comment}</p>}
                  </article>
                ))
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t bg-base-100 flex justify-end">
          <button className="btn btn-primary" onClick={onClose}>Close</button>
        </div>
      </div>
    </dialog>
  );
}