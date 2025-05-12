export interface OfferResponseDto {
    productId: string;
    provider: string;
    speed: number; // mbps
    monthlyCostInCent: number;
    monthlyCostAfter24mInCent: number | null;
    durationInMonths: number;
    connectionType: string;
    tvIncluded: boolean;
    installationService: boolean;
    voucherValueInCent: number | null;
    voucherType: "ABSOLUTE" | "PERCENTAGE" | null;
    discountInCent: number | null;
  }
  
  export interface SearchCriteria {
    street: string;
    houseNumber: number;          // 👈 changed
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
    connectionTypes: string[]; // DSL, FIBER …
    maxPrice: number; // €
    includeTV: boolean;
    installationService: boolean;
  }

  function splitCityPostal(raw: string): { city: string; postalCode: string } {
    const trimmed = raw.trim();
    const m = trimmed.match(/(\d{5})/);          // look for a 5-digit number
    if (!m) return { city: trimmed, postalCode: "" };
  
    const postal = m[1];
    const city = trimmed
      .replace(postal, "")
      .replace(/[,\s]+$/, "")                     // kill trailing space/comma
      .replace(/^\s+/, "")                       // kill leading space
      .trim();
  
    return { city, postalCode: postal };
  }
  
  // Helper to turn form state into API DTO
  export function toCriteria(q: SearchQuery): SearchCriteria {
    const { city, postalCode } = splitCityPostal(q.cityOrPostal);
  
    return {
      street: q.street.trim(),
      houseNumber: parseInt(q.number.trim(), 10) || 0,
      city,
      postalCode,
      connectionTypes: q.connectionTypes,          // ✅ send full list
      maxPriceInCent: q.maxPrice ? q.maxPrice * 100 : null,
      includeTv: q.includeTV,
      includeInstallation: q.installationService,
    };
  }
  