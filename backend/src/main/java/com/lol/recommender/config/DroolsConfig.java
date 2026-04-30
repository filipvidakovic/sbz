package com.lol.recommender.config;

import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Konfiguriše Drools KieContainer koji učitava .drl fajlove iz resources/rules/.
 *
 * Koristimo programmatic API umesto kmodule.xml da bismo imali
 * jasnu kontrolu nad učitavanjem pravila u Spring kontekstu.
 */
@Configuration
public class DroolsConfig {

    private static final String RULES_PATH = "rules/";

    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Učitaj sve .drl fajlove iz resources/rules/
        for (String drl : new String[]{
                "facts.drl",
                "strategies.drl",
                "recommendations.drl"
        }) {
            kieFileSystem.write(
                kieServices.getResources()
                    .newClassPathResource(RULES_PATH + drl)
            );
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException(
                "Greška pri kompajliranju Drools pravila:\n" +
                results.getMessages(Message.Level.ERROR).toString()
            );
        }

        return kieServices.newKieContainer(
            kieServices.getRepository().getDefaultReleaseId()
        );
    }

    /**
     * Vraća novu KieSession za svaki poziv – session se ne deli između zahteva!
     * Scope je prototype jer KieSession nije thread-safe.
     */
    @Bean
    @Scope("prototype")
    public KieSession kieSession(KieContainer kieContainer) {
        return kieContainer.newKieSession();
    }
}
