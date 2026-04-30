package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Predstavlja jednog championa sa svim atributima relevantnim za rule engine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Champion {

    private String name;

    /**
     * Uloga: TANK, ASSASSIN, MAGE, ADC, SUPPORT, FIGHTER
     */
    private Role role;

    /**
     * Tip štete: AD, AP, MIXED
     */
    private DamageType damageType;

    /**
     * Stil igre: BURST, SUSTAIN, ENGAGE, POKE, UTILITY
     */
    private PlayStyle playStyle;

    /**
     * Tim: "ally" ili "enemy"
     */
    private String team;

    public enum Role {
        TANK, ASSASSIN, MAGE, ADC, SUPPORT, FIGHTER
    }

    public enum DamageType {
        AD, AP, MIXED
    }

    public enum PlayStyle {
        BURST, SUSTAIN, ENGAGE, POKE, UTILITY
    }
}
