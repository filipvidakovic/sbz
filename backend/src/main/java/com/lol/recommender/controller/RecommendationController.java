package com.lol.recommender.controller;

import com.lol.recommender.model.*;
import com.lol.recommender.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // Vite default port
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    /**
     * POST /api/recommend
     *
     * Standardna analiza + CEP ako su orderedEnemyPicks prosleđeni.
     *
     * Minimalni body:
     *   { "allyChampions": ["Garen"], "enemyChampions": ["Zed"] }
     *
     * Sa CEP:
     *   {
     *     "allyChampions": ["Garen"],
     *     "enemyChampions": ["Zed", "Talon", "Katarina"],
     *     "orderedEnemyPicks": ["Zed", "Talon", "Katarina"],
     *     "simulatedPickIntervalMs": 6000
     *   }
     */
    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> recommend(
            @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(service.recommend(request));
    }

    /**
     * POST /api/check
     *
     * Backward Chaining provera: da li je određeni champion dobar pick?
     *
     * Body: { "allyChampions": [...], "enemyChampions": [...] }
     * Param: ?champion=Malphite
     */
    @PostMapping("/check")
    public ResponseEntity<BCResult> checkChampion(
            @RequestParam String champion,
            @RequestBody RecommendationRequest context) {
        return ResponseEntity.ok(service.checkChampion(context, champion));
    }

    /**
     * GET /api/champions
     */
    @GetMapping("/champions")
    public ResponseEntity<List<String>> getChampions() {
        return ResponseEntity.ok(service.getAvailableChampions());
    }
}