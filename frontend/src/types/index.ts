export type TeamSide = 'ally' | 'enemy';

export interface Recommendation {
  championName: string;
  reasons: string[];
  priority: number;
}

export interface DraftTrend {
  trendType: TrendType;
  team: string;
  strength: number;
  description: string;
}

export type TrendType =
  | 'AGGRESSIVE_ASSASSIN_DRAFT'
  | 'TANK_SPAM'
  | 'AP_BURST_TREND'
  | 'AD_HEAVY_TREND'
  | 'BURST_DRAFTING'
  | 'ENGAGE_CHAIN'
  | 'BALANCED_DRAFT';

export interface RecommendationResponse {
  recommendations: Recommendation[];
  detectedTrends: DraftTrend[];
  cepAnalysisActive: boolean;
}

export interface RecommendationRequest {
  allyChampions: string[];
  enemyChampions: string[];
  orderedEnemyPicks?: string[];
  simulatedPickIntervalMs?: number;
}

export interface BCResult {
  championName: string;
  valid: boolean;
  reasons: string[];
}