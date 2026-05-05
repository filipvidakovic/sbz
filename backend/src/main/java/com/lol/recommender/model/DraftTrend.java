package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftTrend {

    public enum TrendType {
        AGGRESSIVE_ASSASSIN_DRAFT,
        TANK_SPAM,
        AP_BURST_TREND,
        AD_HEAVY_TREND,
        BURST_DRAFTING,
        ENGAGE_CHAIN,
        BALANCED_DRAFT
    }
    private TrendType trendType;
    private String team;
    private double strength;
    private String description;
}