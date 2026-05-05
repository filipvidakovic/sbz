package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Odgovor koji backend šalje frontendu.
 * Sadrži i preporuke i CEP trendove i BC status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private List<Recommendation> recommendations;
    private List<DraftTrend> detectedTrends;
    private boolean cepAnalysisActive;
}