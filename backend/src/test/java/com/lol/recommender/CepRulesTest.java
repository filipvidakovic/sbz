package com.lol.recommender;

import com.lol.recommender.config.ChampionRegistry;
import com.lol.recommender.config.DroolsConfig;
import com.lol.recommender.model.*;
import com.lol.recommender.model.Champion.DamageType;
import com.lol.recommender.model.Champion.PlayStyle;
import com.lol.recommender.model.Champion.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieContainer;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CEP testovi – svaki test pokriva jedno CEP pravilo iz cep.drl.
 *
 * Koristimo PSEUDO clock da kontrolišemo vreme bez čekanja.
 * Pattern:
 *   1. Kreiraj novu KieSession (prototype scope)
 *   2. Ubaci PickEvent-e sa napredovanjem pseudo clock-a
 *   3. fireAllRules()
 *   4. Proveri da li je DraftTrend / Fact ubačen u WM
 */
@SpringBootTest
class CepRulesTest {

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private ChampionRegistry registry;

    private KieSession session;
    private SessionPseudoClock clock;

    // ── Pomoćni metodi ────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // Svaki test dobija svežu session sa pseudo clock-om
        var ksConfig = org.kie.api.KieServices.Factory.get().newKieSessionConfiguration();
        ksConfig.setOption(org.kie.api.runtime.conf.ClockTypeOption.get("pseudo"));
        session = kieContainer.newKieSession("defaultKieSession", ksConfig);
        clock = session.getSessionClock();
    }

    /** Ubacuje PickEvent i napreduje clock za intervalMs */
    private void pick(String championName, String team, int order, long intervalMs) {
        registry.find(championName).ifPresent(c -> {
            PickEvent event = new PickEvent(
                    c.getName(), team, c.getRole(),
                    c.getDamageType(), c.getPlayStyle(),
                    clock.getCurrentTime(), order
            );
            session.insert(event);
        });
        clock.advanceTime(intervalMs, TimeUnit.MILLISECONDS);
    }

    /** Direktno ubacuje PickEvent sa zadatim atributima (bez registra) */
    private void pickRaw(String name, String team, Role role,
                         DamageType dmg, PlayStyle style, int order, long intervalMs) {
        PickEvent event = new PickEvent(
                name, team, role, dmg, style,
                clock.getCurrentTime(), order
        );
        session.insert(event);
        clock.advanceTime(intervalMs, TimeUnit.MILLISECONDS);
    }

    private List<DraftTrend> getTrends() {
        return session.getObjects(o -> o instanceof DraftTrend)
                .stream().map(o -> (DraftTrend) o)
                .collect(Collectors.toList());
    }

    private List<Fact> getFacts() {
        return session.getObjects(o -> o instanceof Fact)
                .stream().map(o -> (Fact) o)
                .collect(Collectors.toList());
    }

    private boolean hasTrend(DraftTrend.TrendType type) {
        return getTrends().stream().anyMatch(t -> t.getTrendType() == type);
    }

    private boolean hasFact(String name) {
        return getFacts().stream().anyMatch(f -> f.getName().equals(name));
    }

    // ════════════════════════════════════════════════════════
    //  TEST 1: AGGRESSIVE_ASSASSIN_DRAFT
    //  Pravilo: 2+ assassina unutar 30 sekundi
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-01: 2 assassina unutar 30s → AGGRESSIVE_ASSASSIN_DRAFT")
    void testAggressiveAssassinDraft_triggered() {
        // Zed i Talon, razmak 10s – ukupno unutar 30s prozora
        pick("Zed",   "enemy", 1, 10_000);
        pick("Talon", "enemy", 2, 10_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT))
                .as("Treba detektovati AGGRESSIVE_ASSASSIN_DRAFT")
                .isTrue();

        assertThat(hasFact("cepEnemyAssassinTrend"))
                .as("Treba ubaciti Fact cepEnemyAssassinTrend")
                .isTrue();
    }

    @Test
    @DisplayName("CEP-01b: strength se skalira – 3 assassina = strength 1.0")
    void testAggressiveAssassinDraft_strength() {
        pick("Zed",      "enemy", 1, 5_000);
        pick("Talon",    "enemy", 2, 5_000);
        pick("Katarina", "enemy", 3, 5_000);

        session.fireAllRules();

        DraftTrend trend = getTrends().stream()
                .filter(t -> t.getTrendType() == DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT)
                .findFirst().orElseThrow();

        assertThat(trend.getStrength()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("CEP-01c: 2 assassina ALI vremenski razmak > 30s → NE detektuje")
    void testAggressiveAssassinDraft_notTriggered_tooSlow() {
        // Zed na t=0, Talon na t=35s – van 30s prozora
        pick("Zed",   "enemy", 1, 35_000);
        pick("Talon", "enemy", 2, 0);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT))
                .as("Ne sme detektovati trend ako su pickovi van vremenskog prozora")
                .isFalse();
    }

    @Test
    @DisplayName("CEP-01d: assassini ally tima ne okidaju trend")
    void testAggressiveAssassinDraft_allyIgnored() {
        pick("Zed",   "ally", 1, 5_000);
        pick("Talon", "ally", 2, 5_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT))
                .as("Ally assassini ne smeju okinuti enemy trend")
                .isFalse();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 2: BURST_DRAFTING
    //  Pravilo: 3+ enemy pickova unutar 20 sekundi
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-02: 3 picka unutar 20s → BURST_DRAFTING")
    void testBurstDrafting_triggered() {
        pick("Zed",    "enemy", 1, 5_000);
        pick("Syndra", "enemy", 2, 5_000);
        pick("Leona",  "enemy", 3, 5_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.BURST_DRAFTING))
                .as("3 picka u 15s treba da okinu BURST_DRAFTING")
                .isTrue();

        assertThat(hasFact("cepEnemyBurstDrafting")).isTrue();
    }

    @Test
    @DisplayName("CEP-02b: samo 2 picka unutar 20s → NE detektuje")
    void testBurstDrafting_notTriggered_onlyTwo() {
        pick("Zed",    "enemy", 1, 5_000);
        pick("Syndra", "enemy", 2, 5_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.BURST_DRAFTING)).isFalse();
    }

    @Test
    @DisplayName("CEP-02c: 3 picka ali raspoređena > 20s → NE detektuje")
    void testBurstDrafting_notTriggered_tooSlow() {
        pick("Zed",    "enemy", 1, 10_000);
        pick("Syndra", "enemy", 2, 10_000);
        pick("Leona",  "enemy", 3, 10_000); // t=30s – van 20s prozora

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.BURST_DRAFTING))
                .as("Pickovi raspoređeni na 30s ne smeju okinuti 20s trend")
                .isFalse();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 3: TANK_SPAM
    //  Pravilo: 3 uzastopna TANK picka (pickOrder N, N+1, N+2)
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-03: 3 uzastopna tanka → TANK_SPAM")
    void testTankSpam_triggered() {
        pickRaw("Malphite", "enemy", Role.TANK, DamageType.AP,    PlayStyle.ENGAGE,  1, 8_000);
        pickRaw("Ornn",     "enemy", Role.TANK, DamageType.MIXED, PlayStyle.ENGAGE,  2, 8_000);
        pickRaw("Rammus",   "enemy", Role.TANK, DamageType.AD,    PlayStyle.ENGAGE,  3, 8_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.TANK_SPAM))
                .as("3 uzastopna tanka treba da okinu TANK_SPAM")
                .isTrue();

        assertThat(hasFact("cepEnemyTankSpam")).isTrue();

        DraftTrend trend = getTrends().stream()
                .filter(t -> t.getTrendType() == DraftTrend.TrendType.TANK_SPAM)
                .findFirst().orElseThrow();
        assertThat(trend.getStrength()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("CEP-03b: tanci nisu uzastopni (N, N+2) → NE detektuje")
    void testTankSpam_notTriggered_nonConsecutive() {
        pickRaw("Malphite", "enemy", Role.TANK,    DamageType.AP, PlayStyle.ENGAGE,  1, 8_000);
        pickRaw("Zed",      "enemy", Role.ASSASSIN,DamageType.AD, PlayStyle.BURST,   2, 8_000);
        pickRaw("Ornn",     "enemy", Role.TANK,    DamageType.AP, PlayStyle.ENGAGE,  3, 8_000);
        pickRaw("Rammus",   "enemy", Role.TANK,    DamageType.AD, PlayStyle.ENGAGE,  4, 8_000);

        session.fireAllRules();

        // Ornn(3) i Rammus(4) su uzastopni ali samo 2 → ne okida
        // Malphite(1) → Zed(2) → Ornn(3): ne uzastopni tanci
        assertThat(hasTrend(DraftTrend.TrendType.TANK_SPAM))
                .as("Neuzastopni tanci ne smeju okinuti TANK_SPAM")
                .isFalse();
    }

    @Test
    @DisplayName("CEP-03c: samo 2 uzastopna tanka → NE detektuje")
    void testTankSpam_notTriggered_onlyTwo() {
        pickRaw("Malphite", "enemy", Role.TANK, DamageType.AP, PlayStyle.ENGAGE, 1, 8_000);
        pickRaw("Ornn",     "enemy", Role.TANK, DamageType.AP, PlayStyle.ENGAGE, 2, 8_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.TANK_SPAM)).isFalse();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 4: AP_BURST_TREND
    //  Pravilo: 2+ AP+BURST championa unutar 25 sekundi
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-04: 2 AP burst championa unutar 25s → AP_BURST_TREND")
    void testApBurstTrend_triggered() {
        pick("Syndra",   "enemy", 1, 10_000);
        pick("Katarina", "enemy", 2, 10_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AP_BURST_TREND))
                .as("2 AP burst picka u 20s treba da okine AP_BURST_TREND")
                .isTrue();

        assertThat(hasFact("cepEnemyAPBurstTrend")).isTrue();
    }

    @Test
    @DisplayName("CEP-04b: AP champion bez BURST stila ne računa")
    void testApBurstTrend_notTriggered_wrongStyle() {
        // Viktor je AP ali POKE stil – ne broji se kao burst
        pick("Viktor",   "enemy", 1, 5_000);
        pick("Orianna",  "enemy", 2, 5_000); // AP ali UTILITY

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AP_BURST_TREND))
                .as("AP champions koji nisu BURST stil ne smeju okinuti trend")
                .isFalse();
    }

    @Test
    @DisplayName("CEP-04c: strength skalira sa brojem burst pickova")
    void testApBurstTrend_strengthScaling() {
        pick("Syndra",   "enemy", 1, 5_000);
        pick("Katarina", "enemy", 2, 5_000);
        pick("Fizz",     "enemy", 3, 5_000);

        session.fireAllRules();

        DraftTrend trend = getTrends().stream()
                .filter(t -> t.getTrendType() == DraftTrend.TrendType.AP_BURST_TREND)
                .findFirst().orElseThrow();

        assertThat(trend.getStrength()).isEqualTo(1.0); // min(1.0, 3/3.0)
    }

    // ════════════════════════════════════════════════════════
    //  TEST 5: AD_HEAVY_TREND
    //  Pravilo: 3+ AD pickova unutar 40 sekundi
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-05: 3 AD picka unutar 40s → AD_HEAVY_TREND")
    void testAdHeavyTrend_triggered() {
        pick("Zed",     "enemy", 1, 10_000);
        pick("Caitlyn", "enemy", 2, 10_000);
        pick("Talon",   "enemy", 3, 10_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AD_HEAVY_TREND))
                .as("3 AD picka u 30s treba da okine AD_HEAVY_TREND")
                .isTrue();

        assertThat(hasFact("cepEnemyADHeavyTrend")).isTrue();
    }

    @Test
    @DisplayName("CEP-05b: samo 2 AD picka → NE detektuje")
    void testAdHeavyTrend_notTriggered_onlyTwo() {
        pick("Zed",     "enemy", 1, 5_000);
        pick("Caitlyn", "enemy", 2, 5_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AD_HEAVY_TREND)).isFalse();
    }

    @Test
    @DisplayName("CEP-05c: 3 AD picka ali van 40s prozora → NE detektuje")
    void testAdHeavyTrend_notTriggered_outsideWindow() {
        pick("Zed",     "enemy", 1, 20_000);
        pick("Caitlyn", "enemy", 2, 20_000);
        pick("Talon",   "enemy", 3, 20_000); // t=60s – van 40s prozora

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AD_HEAVY_TREND))
                .as("AD pickovi van 40s prozora ne smeju okinuti trend")
                .isFalse();
    }

    @Test
    @DisplayName("CEP-05d: strength skalira – 4 AD picka = strength 1.0")
    void testAdHeavyTrend_strengthAtMax() {
        pick("Zed",     "enemy", 1, 5_000);
        pick("Caitlyn", "enemy", 2, 5_000);
        pick("Talon",   "enemy", 3, 5_000);
        pick("Jhin",    "enemy", 4, 5_000);

        session.fireAllRules();

        DraftTrend trend = getTrends().stream()
                .filter(t -> t.getTrendType() == DraftTrend.TrendType.AD_HEAVY_TREND)
                .findFirst().orElseThrow();

        assertThat(trend.getStrength()).isEqualTo(1.0); // min(1.0, 4/4.0)
    }

    // ════════════════════════════════════════════════════════
    //  TEST 6: ENGAGE_CHAIN
    //  Pravilo: 2 uzastopna ENGAGE picka (pickOrder N, N+1)
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-06: 2 uzastopna engage picka → ENGAGE_CHAIN")
    void testEngageChain_triggered() {
        pickRaw("Leona",    "enemy", Role.TANK,    DamageType.AD, PlayStyle.ENGAGE, 1, 8_000);
        pickRaw("Nautilus", "enemy", Role.TANK,    DamageType.AD, PlayStyle.ENGAGE, 2, 8_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.ENGAGE_CHAIN))
                .as("2 uzastopna engage picka treba da okine ENGAGE_CHAIN")
                .isTrue();

        assertThat(hasFact("cepEnemyEngageChain")).isTrue();

        DraftTrend trend = getTrends().stream()
                .filter(t -> t.getTrendType() == DraftTrend.TrendType.ENGAGE_CHAIN)
                .findFirst().orElseThrow();
        assertThat(trend.getStrength()).isEqualTo(0.85);
    }

    @Test
    @DisplayName("CEP-06b: engage pickovi nisu uzastopni → NE detektuje")
    void testEngageChain_notTriggered_nonConsecutive() {
        pickRaw("Leona",  "enemy", Role.TANK,    DamageType.AD, PlayStyle.ENGAGE, 1, 8_000);
        pickRaw("Zed",    "enemy", Role.ASSASSIN,DamageType.AD, PlayStyle.BURST,  2, 8_000);
        pickRaw("Ornn",   "enemy", Role.TANK,    DamageType.AP, PlayStyle.ENGAGE, 3, 8_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.ENGAGE_CHAIN))
                .as("Engage pickovi sa razmakom u redosledu ne smeju okinuti ENGAGE_CHAIN")
                .isFalse();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 7: BALANCED_DRAFT
    //  Pravilo: poslednja 4 picka imaju 3+ različite uloge
    //           i nema jačeg trenda
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-07: 4 picka sa 4 različite uloge i bez trenda → BALANCED_DRAFT")
    void testBalancedDraft_triggered() {
        pickRaw("C1", "enemy", Role.TANK,    DamageType.AD, PlayStyle.ENGAGE,  1, 8_000);
        pickRaw("C2", "enemy", Role.MAGE,    DamageType.AP, PlayStyle.POKE,    2, 8_000);
        pickRaw("C3", "enemy", Role.ADC,     DamageType.AD, PlayStyle.SUSTAIN, 3, 8_000);
        pickRaw("C4", "enemy", Role.SUPPORT, DamageType.AP, PlayStyle.UTILITY, 4, 8_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.BALANCED_DRAFT))
                .as("4 različite uloge treba da okine BALANCED_DRAFT")
                .isTrue();
    }

    @Test
    @DisplayName("CEP-07b: samo 2 različite uloge u zadnja 4 picka → NE detektuje")
    void testBalancedDraft_notTriggered_sameRoles() {
        pickRaw("C1", "enemy", Role.ASSASSIN, DamageType.AD, PlayStyle.BURST, 1, 8_000);
        pickRaw("C2", "enemy", Role.ASSASSIN, DamageType.AP, PlayStyle.BURST, 2, 8_000);
        pickRaw("C3", "enemy", Role.MAGE,     DamageType.AP, PlayStyle.BURST, 3, 8_000);
        pickRaw("C4", "enemy", Role.ASSASSIN, DamageType.AD, PlayStyle.BURST, 4, 8_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.BALANCED_DRAFT)).isFalse();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 8: ESKALACIJA – cepEscalatedAssassinThreat
    //  Pravilo: AGGRESSIVE_ASSASSIN_DRAFT sa strength >= 0.6
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-08: assassin trend strength >= 0.6 → cepEscalatedAssassinThreat")
    void testEscalatedAssassinThreat_triggered() {
        // 2 assassina = strength 0.67 (>= 0.6)
        pick("Zed",   "enemy", 1, 5_000);
        pick("Talon", "enemy", 2, 5_000);

        session.fireAllRules();

        assertThat(hasFact("cepEscalatedAssassinThreat"))
                .as("Strength 0.67 treba da okine eskalaciju")
                .isTrue();
    }

    @Test
    @DisplayName("CEP-08b: assassin trend strength < 0.6 → NE eskalira")
    void testEscalatedAssassinThreat_notTriggered_lowStrength() {
        // Ručno ubacimo DraftTrend sa niskom jačinom
        session.insert(new DraftTrend(
                DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT,
                "enemy", 0.4, "test"
        ));

        session.fireAllRules();

        assertThat(hasFact("cepEscalatedAssassinThreat"))
                .as("Strength 0.4 ne sme okinuti eskalaciju")
                .isFalse();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 9: ESKALACIJA – cepEscalatedAPThreat
    //  Pravilo: AP_BURST_TREND sa strength >= 0.6
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-09: AP burst trend strength >= 0.6 → cepEscalatedAPThreat")
    void testEscalatedApThreat_triggered() {
        pick("Syndra",   "enemy", 1, 5_000);
        pick("Katarina", "enemy", 2, 5_000);

        session.fireAllRules();

        assertThat(hasFact("cepEscalatedAPThreat"))
                .as("AP burst strength 0.67 treba da okine eskalaciju")
                .isTrue();
    }

    // ════════════════════════════════════════════════════════
    //  TEST 10: KOMBINOVANI – više trendova istovremeno
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("CEP-10: assassini + AD pickovi → oba trenda paralelno")
    void testMultipleTrends_simultaneously() {
        // Zed (AD assassin), Talon (AD assassin), Caitlyn (AD ADC)
        // → treba okinuti i AGGRESSIVE_ASSASSIN_DRAFT i AD_HEAVY_TREND
        pick("Zed",     "enemy", 1, 5_000);
        pick("Talon",   "enemy", 2, 5_000);
        pick("Caitlyn", "enemy", 3, 5_000);

        session.fireAllRules();

        assertThat(hasTrend(DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT)).isTrue();
        assertThat(hasTrend(DraftTrend.TrendType.AD_HEAVY_TREND)).isTrue();
        assertThat(hasTrend(DraftTrend.TrendType.BURST_DRAFTING)).isTrue();

        // Sva 3 trenda treba da koegzistiraju
        assertThat(getTrends()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("CEP-10b: svaki trend se beleži samo jednom (not duplicate)")
    void testNoDuplicateTrends() {
        // Pokušaj da okine isti trend dva puta
        pick("Zed",      "enemy", 1, 5_000);
        pick("Talon",    "enemy", 2, 5_000);
        pick("Katarina", "enemy", 3, 5_000);

        session.fireAllRules();

        long assassinTrendCount = getTrends().stream()
                .filter(t -> t.getTrendType() == DraftTrend.TrendType.AGGRESSIVE_ASSASSIN_DRAFT)
                .count();

        assertThat(assassinTrendCount)
                .as("Isti trend ne sme biti ubačen dva puta")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("CEP-10c: ally pickovi ne okidaju enemy trendove")
    void testAllyPicksDoNotTriggerEnemyTrends() {
        // 3 ally assassina brzo – ništa ne sme biti detektovano
        pick("Zed",      "ally", 1, 5_000);
        pick("Talon",    "ally", 2, 5_000);
        pick("Katarina", "ally", 3, 5_000);

        session.fireAllRules();

        assertThat(getTrends())
                .as("Ally pickovi ne smeju okinuti nijedan enemy trend")
                .isEmpty();
    }
}