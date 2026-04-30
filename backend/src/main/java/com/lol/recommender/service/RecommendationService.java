package com.lol.recommender.service;

import com.lol.recommender.config.ChampionRegistry;
import com.lol.recommender.model.*;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ObjectProvider<KieSession> kieSessionProvider;
    private final ChampionRegistry registry;

    public RecommendationService(ObjectProvider<KieSession> kieSessionProvider,
                                  ChampionRegistry registry) {
        this.kieSessionProvider = kieSessionProvider;
        this.registry = registry;
    }

    /**
     * Pokreće Drools engine na osnovu sastava timova i vraća sortirane preporuke.
     *
     * @param request DTO sa imenima championa po timovima
     * @return lista preporuka sortirana po prioritetu (opadajuće)
     */
    public List<Recommendation> recommend(RecommendationRequest request) {
        // Dobijamo novu KieSession za ovaj zahtev (prototype scope)
        KieSession session = kieSessionProvider.getObject();

        try {
            // 1. Ubacujemo Champion objekte u Working Memory
            insertChampions(session, request.getAllyChampions(), "ally");
            insertChampions(session, request.getEnemyChampions(), "enemy");

            // 2. Pokrenemo sva pravila (forward chaining)
            session.fireAllRules();

            // 3. Izvučemo Recommendation objekte iz Working Memory
            List<Recommendation> recommendations = session.getObjects(
                    obj -> obj instanceof Recommendation
            ).stream()
                .map(obj -> (Recommendation) obj)
                .sorted(Comparator.comparingInt(Recommendation::getPriority).reversed())
                .collect(Collectors.toList());

            // 4. (Debug) Možemo i logirati izvučene Fact-ove i Strategy-je
            logWorkingMemory(session);

            return recommendations;

        } finally {
            // OBAVEZNO: oslobodi resurse, nikad ne zaboravi dispose!
            session.dispose();
        }
    }

    /**
     * Vraća sve poznate champione (za prikaz u dropdownu na frontendu).
     */
    public List<String> getAvailableChampions() {
        return registry.getAll().values().stream()
                .map(Champion::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    // ── HELPERS ──────────────────────────────────────────────────

    private void insertChampions(KieSession session, List<String> names, String team) {
        if (names == null) return;
        for (String name : names) {
            registry.find(name).ifPresentOrElse(
                champion -> {
                    Champion copy = new Champion(
                        champion.getName(),
                        champion.getRole(),
                        champion.getDamageType(),
                        champion.getPlayStyle(),
                        team
                    );
                    session.insert(copy);
                },
                () -> System.err.println("Nepoznat champion: " + name)
            );
        }
    }

    private void logWorkingMemory(KieSession session) {
        System.out.println("\n=== Working Memory nakon fireAllRules ===");

        session.getObjects(o -> o instanceof Fact)
               .forEach(f -> System.out.println("  FACT: " + ((Fact) f).getName()));

        session.getObjects(o -> o instanceof Strategy)
               .forEach(s -> System.out.println("  STRATEGY: " + ((Strategy) s).getName()));

        session.getObjects(o -> o instanceof Recommendation)
               .forEach(r -> System.out.println("  RECOMMENDATION: " + ((Recommendation) r).getChampionName()
                       + " (priority=" + ((Recommendation) r).getPriority() + ")"));

        System.out.println("=========================================\n");
    }
}
