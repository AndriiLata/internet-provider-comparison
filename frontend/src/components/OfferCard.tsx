import type { OfferResponseDto } from "../types/offer";
// 1) import your logo assets
import byteMeLogo from "../assets/logos/byteme.jpg";
import pingPerfectLogo from "../assets/logos/pingperfect.jpg";
import servusSpeedLogo from "../assets/logos/servusspeed.jpg";
import verbynDichLogo from "../assets/logos/verbyndich.jpg";
import webWunderLogo from "../assets/logos/webwunder.jpg";

interface Props {
  offer: OfferResponseDto;
}

export default function OfferCard({ offer }: Props) {
  // helpers
  const formatEuro = (cent: number) => (cent / 100).toFixed(2);

  const hasDiscount =
    typeof offer.discountInCent === "number" && offer.discountInCent > 0;
  const finalPriceCent = hasDiscount
    ? offer.monthlyCostInCent - offer.discountInCent!
    : offer.monthlyCostInCent;

  const voucherLabel = offer.voucherValueInCent
    ? offer.voucherType === "ABSOLUTE"
      ? `€${formatEuro(offer.voucherValueInCent)} Gutschein`
      : `${offer.voucherValueInCent}% Gutschein`
    : null;

  // 2) map keywords to the imported images
  const logoMap: Record<string, string> = {
    byte: byteMeLogo,
    ping: pingPerfectLogo,
    servus: servusSpeedLogo,
    verbyndich: verbynDichLogo,
    webwunder: webWunderLogo,
  };

  // 3) find which key appears in the provider name
  const providerKey = Object.keys(logoMap).find((key) =>
    offer.provider.toLowerCase().includes(key)
  );
  const logoSrc = providerKey ? logoMap[providerKey] : null;

  return (
    <div className="card bg-secondary-50 shadow-md hover:shadow-xl transition-shadow duration-300 hover:-translate-y-0.5">
      <div className="card-body p-6 gap-6">
        {/* header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            {/* 4) render the real logo */}
            {logoSrc ? (
              <img
                src={logoSrc}
                alt={`${offer.provider} logo`}
                className="w-12 h-12 rounded-md object-contain"
              />
            ) : (
              <div className="w-12 h-12 rounded-md bg-neutral/10 grid place-items-center">
                <span className="text-xs font-medium text-neutral-content/60">
                  Logo
                </span>
              </div>
            )}
            <h3 className="card-title text-xl font-semibold leading-tight">
              {offer.provider}
            </h3>
          </div>
          {voucherLabel && (
            <span className="badge badge-success badge-lg font-medium whitespace-nowrap">
              {voucherLabel}
            </span>
          )}
        </div>

        {/* feature badges */}
        <div className="flex flex-wrap gap-2">
          <span className="badge badge-outline capitalize">
            {offer.connectionType}
          </span>
          <span className="badge badge-outline">{offer.speed} Mbit/s</span>
          {offer.tvIncluded && <span className="badge badge-outline">TV inkl.</span>}
          {offer.installationService && (
            <span className="badge badge-outline">Installation inkl.</span>
          )}
        </div>

        {/* pricing + button */}
        <div className="flex items-baseline justify-between">
          <div>
            {hasDiscount ? (
              <div className="flex items-baseline gap-2">
                <span className="line-through text-sm opacity-60">
                  €{formatEuro(offer.monthlyCostInCent)}
                </span>
                <span className="text-4xl font-extrabold text-primary">
                  €{formatEuro(finalPriceCent)}
                </span>
                <span className="text-sm opacity-60">/ Monat</span>
              </div>
            ) : (
              <div className="text-4xl font-extrabold text-primary">
                €{formatEuro(finalPriceCent)}
                <span className="text-sm font-medium opacity-60">
                  / Monat
                </span>
              </div>
            )}
            {offer.monthlyCostAfter24mInCent && (
              <p className="text-xs opacity-70 mt-1">
                Ab Monat 25: €{formatEuro(offer.monthlyCostAfter24mInCent)} / Monat
              </p>
            )}
          </div>
          <button className="btn btn-primary btn-md">Zum Angebot</button>
        </div>
      </div>
    </div>
  );
}
