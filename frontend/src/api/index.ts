import type { RecommendationRequest, RecommendationResponse, BCResult } from '../types';

const BASE_URL = 'http://localhost:8080/api';

export async function fetchChampions(): Promise<string[]> {
  const res = await fetch(`${BASE_URL}/champions`);
  if (!res.ok) throw new Error('Failed to fetch champions');
  return res.json();
}

export async function fetchRecommendations(
  request: RecommendationRequest
): Promise<RecommendationResponse> {
  const res = await fetch(`${BASE_URL}/recommend`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error('Failed to fetch recommendations');
  return res.json();
}

export async function checkChampion(
  champion: string,
  request: RecommendationRequest
): Promise<BCResult> {
  const res = await fetch(`${BASE_URL}/check?champion=${encodeURIComponent(champion)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error('Failed to check champion');
  return res.json();
}