/* ----------  UI DTO matching the new backend  ---------- */

export interface OfferResponseDto {
    productId: string;
    provider: string;
    contractInfo: ContractInfo;
    costInfo: CostInfo;
    tvInfo: TvInfo;
    /** avg. user rating 0-5 – generated client-side for now */
    avgRating?: number;
  }
  
  /* …nested records … */
  export interface ContractInfo {
    connectionType: string;
    speed: number;                       // Mbit/s
    speedLimitFrom?: number | null;      // GB (optional)
    contractDurationInMonths?: number | null;
    maxAge?: number | null;              // years (optional)
  }
  
  export interface CostInfo {
    discountedMonthlyCostInCent: number;
    monthlyCostInCent: number;
    monthlyCostAfter24mInCent?: number | null;
    monthlyDiscountValueInCent?: number | null;
    maxDiscountInCent?: number | null;
    installationService: boolean;
  }
  
  export interface TvInfo {
    tvIncluded: boolean;
    tvBrand?: string | null;
  }
  
  /* ---------- search ---------- */
  
  export interface SearchCriteria {
    street: string;
    houseNumber: number;
    city: string;
    postalCode: string;
    connectionTypes: string[] | null;
    maxPriceInCent: number | null;
    includeTv: boolean;
    includeInstallation: boolean;
  }
  
  export interface SearchQuery {
    cityOrPostal: string;
    street: string;
    number: string;
    connectionTypes: string[];
    maxPrice: number;             // €
    includeTV: boolean;
    installationService: boolean;
  }
  
  /* helper ----------------------------------------------------------------- */
  function splitCityPostal(raw: string): { city: string; postalCode: string } {
    const trimmed = raw.trim();
    const m = trimmed.match(/(\d{5})/);           // first 5-digit number
    if (!m) return { city: trimmed, postalCode: "" };
  
    const postal = m[1];
    const city = trimmed
      .replace(postal, "")
      .replace(/[,\s]+$/, "")
      .replace(/^\s+/, "")
      .trim();
  
    return { city, postalCode: postal };
  }
  
  /** turn form state into API DTO */
  export function toCriteria(q: SearchQuery): SearchCriteria {
    const { city, postalCode } = splitCityPostal(q.cityOrPostal);
  
    return {
      street: q.street.trim(),
      houseNumber: parseInt(q.number.trim(), 10) || 0,
      city,
      postalCode,
      connectionTypes: q.connectionTypes,
      maxPriceInCent: q.maxPrice ? q.maxPrice * 100 : null,
      includeTv: q.includeTV,
      includeInstallation: q.installationService,
    };
  }
  