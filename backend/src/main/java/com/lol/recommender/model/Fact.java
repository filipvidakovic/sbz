package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Međučinjenica koju pravila upisuju u Working Memory.
 *
 * Primeri vrednosti name polja:
 *   - "enemyMostlyAD"
 *   - "enemyMostlyAP"
 *   - "enemyHasAssassin"
 *   - "enemyHasEngage"
 *   - "allyNeedsTank"
 *   - "allyNeedsAP"
 *   - "allyNeedsSupport"
 *   - "allyLacksEngage"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fact {
    private String name;
}
