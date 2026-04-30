package com.lol.recommender.config;

import com.lol.recommender.model.Champion;
import com.lol.recommender.model.Champion.DamageType;
import com.lol.recommender.model.Champion.PlayStyle;
import com.lol.recommender.model.Champion.Role;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registar svih podržanih championa sa njihovim atributima.
 *
 * Ovo je "baza znanja" – atributi su ručno definisani na osnovu
 * domenskog znanja o League of Legends.
 *
 * Dodavanje novog championa: dodaj unos u konstruktoru.
 */
@Component
public class ChampionRegistry {

    private final Map<String, Champion> champions = new HashMap<>();

    public ChampionRegistry() {
        // ── TANKOVI ──────────────────────────────────────────────
        register("Malphite",  Role.TANK,     DamageType.AP,    PlayStyle.ENGAGE);
        register("Ornn",      Role.TANK,     DamageType.MIXED, PlayStyle.ENGAGE);
        register("Leona",     Role.TANK,     DamageType.AD,    PlayStyle.ENGAGE);
        register("Nautilus",  Role.TANK,     DamageType.AD,    PlayStyle.ENGAGE);
        register("Alistar",   Role.TANK,     DamageType.AD,    PlayStyle.UTILITY);
        register("Rammus",    Role.TANK,     DamageType.AD,    PlayStyle.ENGAGE);

        // ── MAGI ─────────────────────────────────────────────────
        register("Lux",       Role.MAGE,     DamageType.AP,    PlayStyle.POKE);
        register("Syndra",    Role.MAGE,     DamageType.AP,    PlayStyle.BURST);
        register("Orianna",   Role.MAGE,     DamageType.AP,    PlayStyle.UTILITY);
        register("Viktor",    Role.MAGE,     DamageType.AP,    PlayStyle.POKE);
        register("Cassiopeia",Role.MAGE,     DamageType.AP,    PlayStyle.SUSTAIN);

        // ── ASSASSINI ─────────────────────────────────────────────
        register("Zed",       Role.ASSASSIN, DamageType.AD,    PlayStyle.BURST);
        register("Talon",     Role.ASSASSIN, DamageType.AD,    PlayStyle.BURST);
        register("Katarina",  Role.ASSASSIN, DamageType.AP,    PlayStyle.BURST);
        register("Fizz",      Role.ASSASSIN, DamageType.AP,    PlayStyle.BURST);
        register("Akali",     Role.ASSASSIN, DamageType.AP,    PlayStyle.BURST);

        // ── ADC ───────────────────────────────────────────────────
        register("Caitlyn",   Role.ADC,      DamageType.AD,    PlayStyle.POKE);
        register("Jinx",      Role.ADC,      DamageType.AD,    PlayStyle.SUSTAIN);
        register("Jhin",      Role.ADC,      DamageType.AD,    PlayStyle.BURST);
        register("Ashe",      Role.ADC,      DamageType.AD,    PlayStyle.UTILITY);
        register("Vayne",     Role.ADC,      DamageType.AD,    PlayStyle.SUSTAIN);

        // ── SUPPORTI ──────────────────────────────────────────────
        register("Thresh",    Role.SUPPORT,  DamageType.AD,    PlayStyle.UTILITY);
        register("Janna",     Role.SUPPORT,  DamageType.AP,    PlayStyle.UTILITY);
        register("Soraka",    Role.SUPPORT,  DamageType.AP,    PlayStyle.SUSTAIN);
        register("Lulu",      Role.SUPPORT,  DamageType.AP,    PlayStyle.UTILITY);

        // ── FIGHTERI ──────────────────────────────────────────────
        register("Garen",     Role.FIGHTER,  DamageType.AD,    PlayStyle.SUSTAIN);
        register("Darius",    Role.FIGHTER,  DamageType.AD,    PlayStyle.SUSTAIN);
        register("Fiora",     Role.FIGHTER,  DamageType.AD,    PlayStyle.SUSTAIN);
        register("Irelia",    Role.FIGHTER,  DamageType.MIXED, PlayStyle.BURST);
    }

    private void register(String name, Role role, DamageType dmg, PlayStyle style) {
        Champion c = new Champion(name, role, dmg, style, null);
        champions.put(name.toLowerCase(), c);
    }

    /**
     * Vraća Champion objekat za dato ime (case-insensitive), ili empty ako nije poznat.
     */
    public Optional<Champion> find(String name) {
        return Optional.ofNullable(champions.get(name.toLowerCase()));
    }

    public Map<String, Champion> getAll() {
        return champions;
    }
}
