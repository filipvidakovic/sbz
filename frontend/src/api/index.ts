import type { Recommendation, RecommendationRequest } from '../types';

const BASE_URL = 'http://localhost:8080/api';

export async function fetchChampions(): Promise<string[]> {
  const res = await fetch(`${BASE_URL}/champions`);
  if (!res.ok) throw new Error('Failed to fetch champions');
  return res.json();
}

export async function fetchRecommendations(
  request: RecommendationRequest
): Promise<Recommendation[]> {
  const res = await fetch(`${BASE_URL}/recommend`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error('Failed to fetch recommendations');
  return res.json();
}