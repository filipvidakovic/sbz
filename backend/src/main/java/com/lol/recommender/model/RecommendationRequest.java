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

    /**
     * Nazivi championa u savezničkom timu (već izabrani).
     * Npr: ["Garen", "Ashe"]
     */
    private List<String> allyChampions;

    /**
     * Nazivi championa u neprijateljskom timu.
     * Npr: ["Zed", "Talon", "Caitlyn"]
     */
    private List<String> enemyChampions;
}
