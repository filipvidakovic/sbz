package com.lol.recommender.config;

import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Drools konfiguracija sa STREAM event processing modom.
 *
 * STREAM mod (Fusion) je neophodan za:
 *   - temporalne operatore (after, before, window:time)
 *   - @Role(EVENT) anotacije na PickEvent klasi
 *   - sliding time windows za CEP analizu
 *
 * KieSession koristi PSEUDO clock za simulaciju draft tempa.
 */
@Configuration
public class DroolsConfig {

    private static final String RULES_PATH = "rules/";

    @Bean
    public KieContainer kieContainer() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        for (String drl : new String[]{
                "facts.drl",
                "strategies.drl",
                "recommendations.drl",
                "cep.drl",
                "templates.drl",
                "backward_chaining.drl"
        }) {
            kfs.write(ks.getResources().newClassPathResource(RULES_PATH + drl));
        }

        // STREAM mod aktivira Drools Fusion (CEP)
        KieModuleModel kieModuleModel = ks.newKieModuleModel();
        kieModuleModel.newKieBaseModel("defaultKieBase")
                .setEventProcessingMode(EventProcessingOption.STREAM)
                .newKieSessionModel("defaultKieSession");

        kfs.writeKModuleXML(kieModuleModel.toXML());

        KieBuilder kb = ks.newKieBuilder(kfs);
        kb.buildAll();

        Results results = kb.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            results.getMessages(Message.Level.ERROR)
                    .forEach(m -> System.err.println("DRL ERROR: " + m.getText() + " | File: " + m.getPath() + " | Line: " + m.getLine()));
            throw new IllegalStateException("Rules compilation failed");
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }

    /**
     * PSEUDO clock – kontrolišemo vreme ručno da simuliramo
     * draft tempo bez čekanja stvarnog vremena.
     */
    @Bean
    @Scope("prototype")
    public KieSession kieSession(KieContainer kieContainer) {
        KieServices ks = KieServices.Factory.get();
        KieSessionConfiguration config = ks.newKieSessionConfiguration();
        config.setOption(ClockTypeOption.get("pseudo"));
        return kieContainer.newKieSession("defaultKieSession", config);
    }
}