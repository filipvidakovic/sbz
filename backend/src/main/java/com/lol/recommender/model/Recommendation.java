package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Preporuka championa sa pratećim objašnjenjima.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    private String championName;

    /**
     * Lista razloga zašto je ovaj champion preporučen.
     * Npr: ["enemy mostly AD", "enemy has assassin", "ally needs tank"]
     */
    private List<String> reasons = new ArrayList<>();

    /**
     * Prioritet preporuke – viši broj = bolja preporuka.
     * Koristi se za sortiranje na frontendu.
     */
    private int priority;

    public Recommendation(String championName, int priority) {
        this.championName = championName;
        this.priority = priority;
    }

    public void addReason(String reason) {
        this.reasons.add(reason);
    }
}
