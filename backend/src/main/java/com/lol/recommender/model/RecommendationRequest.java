package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO koji prima frontend – nazivi championa po timovima.
 * Backend ih mapira na Champion objekte sa atributima iz ChampionRegistry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    private List<String> allyChampions;
    private List<String> enemyChampions;
    private List<String> orderedEnemyPicks;
    private Long simulatedPickIntervalMs;
}
