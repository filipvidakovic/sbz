export type TeamSide = 'ally' | 'enemy';

export interface Recommendation {
  championName: string;
  reasons: string[];
  priority: number;
}

export interface RecommendationRequest {
  allyChampions: string[];
  enemyChampions: string[];
}

export interface ChampionSlot {
  name: string | null;
}