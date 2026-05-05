package com.lol.recommender.service;

import com.lol.recommender.config.ChampionRegistry;
import com.lol.recommender.model.*;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final long DEFAULT_PICK_INTERVAL_MS = 8_000L;

    private final ObjectProvider<KieSession> kieSessionProvider;
    private final ChampionRegistry registry;

    public RecommendationService(ObjectProvider<KieSession> kieSessionProvider,
                                 ChampionRegistry registry) {
        this.kieSessionProvider = kieSessionProvider;
        this.registry = registry;
    }

    // ── GLAVNA METODA ─────────────────────────────────────────

    public RecommendationResponse recommend(RecommendationRequest request) {
        KieSession session = kieSessionProvider.getObject();
        try {
            // 1. Standardni Champion objekti za facts/strategies/recommendations
            insertChampions(session, request.getAllyChampions(), "ally");
            insertChampions(session, request.getEnemyChampions(), "enemy");

            // 2. CEP: ako su prosleđeni ordered pickovi, simuliramo draft tempo
            boolean cepActive = request.getOrderedEnemyPicks() != null
                    && !request.getOrderedEnemyPicks().isEmpty();

            if (cepActive) {
                insertPickEvents(session, request.getOrderedEnemyPicks(),
                        request.getSimulatedPickIntervalMs());
            }

            // 3. Pokrenemo sva pravila
            session.fireAllRules();

            // 4. Izvučemo preporuke
            List<Recommendation> recommendations = extractRecommendations(session);

            // 5. Izvučemo detektovane CEP trendove
            List<DraftTrend> trends = extractTrends(session);

            logWorkingMemory(session);

            return new RecommendationResponse(recommendations, trends, cepActive);

        } finally {
            session.dispose();
        }
    }

    // ── BACKWARD CHAINING PROVJERA ────────────────────────────

    /**
     * Pita engine: "Da li je ovaj champion dobar pick u trenutnom draftu?"
     * Engine gradi stablo unazad i vraća BCResult sa razlozima.
     */
    public BCResult checkChampion(RecommendationRequest context, String championName) {
        KieSession session = kieSessionProvider.getObject();
        try {
            // Ubacimo kontekst (timovi) u session
            insertChampions(session, context.getAllyChampions(), "ally");
            insertChampions(session, context.getEnemyChampions(), "enemy");

            // Ubacimo champion iz registra u WM (bez tima – on je kandidat)
            registry.find(championName).ifPresent(c -> {
                Champion candidate = new Champion(
                        c.getName(), c.getRole(), c.getDamageType(), c.getPlayStyle(), "candidate"
                );
                session.insert(candidate);
            });

            // Aktiviramo standardna pravila da popunimo Fact/Strategy WM
            session.fireAllRules();

            // Ubacimo BackwardChainingGoal za traženog championa
            session.insert(new BackwardChainingGoal(championName));

            // Ponovo pokrenemo da BC pravila evaluiraju cilj
            session.fireAllRules();

            // Izvučemo BCResult iz WM
            Optional<BCResult> result = session.getObjects(o -> o instanceof BCResult)
                    .stream()
                    .map(o -> (BCResult) o)
                    .filter(r -> r.getChampionName().equalsIgnoreCase(championName))
                    .findFirst();

            return result.orElse(new BCResult(championName, false,
                    List.of("Champion not found in registry")));

        } finally {
            session.dispose();
        }
    }

    public List<String> getAvailableChampions() {
        return registry.getAll().values().stream()
                .map(Champion::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    // ── CEP: INSERT PICK EVENTS ───────────────────────────────

    /**
     * Kreira PickEvent objekte sa simuliranim timestamp-ovima i
     * ubacuje ih u session koristeći pseudo clock.
     *
     * Pseudo clock napreduje za intervalMs između svakog picka,
     * što Drools vidi kao proteklo vreme za window:time() prozore.
     */
    private void insertPickEvents(KieSession session, List<String> orderedPicks, Long intervalMs) {
        long interval = intervalMs != null ? intervalMs : DEFAULT_PICK_INTERVAL_MS;
        SessionPseudoClock clock = session.getSessionClock();

        for (int i = 0; i < orderedPicks.size(); i++) {
            String name = orderedPicks.get(i);
            int finalI = i;
            registry.find(name).ifPresent(champion -> {
                PickEvent event = new PickEvent(
                        champion.getName(),
                        "enemy",
                        champion.getRole(),
                        champion.getDamageType(),
                        champion.getPlayStyle(),
                        clock.getCurrentTime(),
                        finalI + 1
                );
                session.insert(event);
            });

            // Napredujemo pseudo clock za simulirani interval
            if (i < orderedPicks.size() - 1) {
                clock.advanceTime(interval, TimeUnit.MILLISECONDS);
            }
        }
    }

    // ── HELPERS ───────────────────────────────────────────────

    private void insertChampions(KieSession session, List<String> names, String team) {
        if (names == null) return;
        for (String name : names) {
            registry.find(name).ifPresentOrElse(
                    champion -> {
                        Champion copy = new Champion(
                                champion.getName(), champion.getRole(),
                                champion.getDamageType(), champion.getPlayStyle(), team
                        );
                        session.insert(copy);
                    },
                    () -> System.err.println("Nepoznat champion: " + name)
            );
        }
    }

    private List<Recommendation> extractRecommendations(KieSession session) {
        return session.getObjects(o -> o instanceof Recommendation)
                .stream()
                .map(o -> (Recommendation) o)
                .sorted(Comparator.comparingInt(Recommendation::getPriority).reversed())
                .collect(Collectors.toList());
    }

    private List<DraftTrend> extractTrends(KieSession session) {
        return session.getObjects(o -> o instanceof DraftTrend)
                .stream()
                .map(o -> (DraftTrend) o)
                .sorted(Comparator.comparingDouble(DraftTrend::getStrength).reversed())
                .collect(Collectors.toList());
    }

    private void logWorkingMemory(KieSession session) {
        System.out.println("\n=== Working Memory ===");
        session.getObjects(o -> o instanceof Fact)
                .forEach(f -> System.out.println("  FACT: " + ((Fact) f).getName()));
        session.getObjects(o -> o instanceof Strategy)
                .forEach(s -> System.out.println("  STRATEGY: " + ((Strategy) s).getName()));
        session.getObjects(o -> o instanceof DraftTrend)
                .forEach(t -> System.out.printf("  TREND: %s (strength=%.2f)%n",
                        ((DraftTrend) t).getTrendType(), ((DraftTrend) t).getStrength()));
        session.getObjects(o -> o instanceof Recommendation)
                .forEach(r -> System.out.printf("  REC: %s (priority=%d)%n",
                        ((Recommendation) r).getChampionName(), ((Recommendation) r).getPriority()));
        System.out.println("======================\n");
    }
}