export interface RatingRequestDto {
    serviceName: string;
    userName: string;
    email: string;
    ranking: number;      // 1–5
    comment?: string;
  }
  
  export interface RatingResponseDto {
    userName: string;
    ranking: number;      // 1–5
    comment?: string;
    createdAt: string;    // ISO date
  }
  