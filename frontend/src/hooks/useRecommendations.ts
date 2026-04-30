import { useState, useEffect } from 'react';
import { fetchChampions, fetchRecommendations } from '../api';
import type { Recommendation } from '../types';

export function useChampions() {
  const [champions, setChampions] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchChampions()
      .then(setChampions)
      .catch(() => setError('Backend is not available. Please start the Spring Boot server.'))
      .finally(() => setLoading(false));
  }, []);

  return { champions, loading, error };
}

export function useRecommendations() {
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getRecommendations = async (
    allyChampions: string[],
    enemyChampions: string[]
  ) => {
    if (allyChampions.length === 0 && enemyChampions.length === 0) return;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchRecommendations({ allyChampions, enemyChampions });
      setRecommendations(result);
    } catch {
      setError('Error fetching recommendations. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const clearRecommendations = () => setRecommendations([]);

  return { recommendations, loading, error, getRecommendations, clearRecommendations };
}