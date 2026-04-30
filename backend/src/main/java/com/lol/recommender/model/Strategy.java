package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Strategija izvedena iz činjenica.
 *
 * Primeri vrednosti name polja:
 *   - "pickTank"
 *   - "pickMage"
 *   - "pickSupport"
 *   - "buildArmor"
 *   - "buildMagicResist"
 *   - "pickEngage"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {
    private String name;
}
