package com.lol.recommender;

import com.lol.recommender.config.ChampionRegistry;
import com.lol.recommender.model.Recommendation;
import com.lol.recommender.model.RecommendationRequest;
import com.lol.recommender.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RecommendationServiceTest {

    @Autowired
    private RecommendationService service;

    /**
     * Primer iz predloga projekta:
     * Enemy: Zed, Talon, Caitlyn → mostly AD + assassins
     * Ally: Garen, Ashe → nema tanka, nema AP, nema engage
     * Ocekivano: Malphite ili Rammus na vrhu preporuka
     */
    @Test
    void testClassicADComposition() {
        RecommendationRequest req = new RecommendationRequest(
            List.of("Garen", "Ashe"),
            List.of("Zed", "Talon", "Caitlyn")
        );

        List<Recommendation> result = service.recommend(req);

        assertThat(result).isNotEmpty();
        List<String> names = result.stream()
                .map(Recommendation::getChampionName)
                .toList();
        System.out.println("Preporuke: " + names);

        // Malphite ili Rammus moraju biti preporuceni
        assertThat(names).containsAnyOf("Malphite", "Rammus");
    }

    /**
     * Enemy mostly AP: Syndra, Orianna, Katarina
     * Ally: Garen, Ashe → nema MR tanka
     * Ocekivano: Ornn na vrhu
     */
    @Test
    void testAPComposition() {
        RecommendationRequest req = new RecommendationRequest(
            List.of("Garen", "Ashe"),
            List.of("Syndra", "Orianna", "Katarina")
        );

        List<Recommendation> result = service.recommend(req);
        List<String> names = result.stream().map(Recommendation::getChampionName).toList();
        System.out.println("Preporuke vs AP: " + names);

        assertThat(names).contains("Ornn");
    }

    /**
     * Ally tim vec ima tanka – ne sme ponovo preporuciti tanka
     */
    @Test
    void testAllyAlreadyHasTank() {
        RecommendationRequest req = new RecommendationRequest(
            List.of("Malphite", "Ashe"),
            List.of("Zed", "Talon", "Caitlyn")
        );

        List<Recommendation> result = service.recommend(req);
        List<String> names = result.stream().map(Recommendation::getChampionName).toList();
        System.out.println("Preporuke kad ima tank: " + names);

        assertThat(names).doesNotContain("Malphite");
    }
}
