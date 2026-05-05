import { useState, useEffect } from 'react';
import { fetchChampions, fetchRecommendations, checkChampion } from '../api';
import type { Recommendation, DraftTrend, BCResult, RecommendationRequest } from '../types';

export function useChampions() {
  const [champions, setChampions] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchChampions()
      .then(setChampions)
      .catch(() => setError('Backend nije dostupan. Pokrenite Spring Boot server.'))
      .finally(() => setLoading(false));
  }, []);

  return { champions, loading, error };
}

export function useRecommendations() {
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [trends, setTrends] = useState<DraftTrend[]>([]);
  const [cepActive, setCepActive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getRecommendations = async (request: RecommendationRequest) => {
    if (!request.allyChampions.length && !request.enemyChampions.length) return;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchRecommendations(request);
      setRecommendations(result.recommendations);
      setTrends(result.detectedTrends ?? []);
      setCepActive(result.cepAnalysisActive);
    } catch {
      setError('Greška pri dobijanju preporuka.');
    } finally {
      setLoading(false);
    }
  };

  const clear = () => {
    setRecommendations([]);
    setTrends([]);
    setCepActive(false);
  };

  return { recommendations, trends, cepActive, loading, error, getRecommendations, clear };
}

export function useChampionCheck() {
  const [result, setResult] = useState<BCResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const check = async (champion: string, context: RecommendationRequest) => {
    if (!champion) return;
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await checkChampion(champion, context);
      setResult(res);
    } catch {
      setError('Greška pri proveri championa.');
    } finally {
      setLoading(false);
    }
  };

  const clear = () => { setResult(null); setError(null); };

  return { result, loading, error, check, clear };
}