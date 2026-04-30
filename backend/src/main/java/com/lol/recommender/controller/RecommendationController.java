package com.lol.recommender.controller;

import com.lol.recommender.model.Recommendation;
import com.lol.recommender.model.RecommendationRequest;
import com.lol.recommender.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // React dev server
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    /**
     * POST /api/recommend
     * Body: { "allyChampions": ["Garen", "Ashe"], "enemyChampions": ["Zed", "Talon", "Caitlyn"] }
     */
    @PostMapping("/recommend")
    public ResponseEntity<List<Recommendation>> recommend(
            @RequestBody RecommendationRequest request) {
        List<Recommendation> result = service.recommend(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/champions
     * Vraća listu svih poznatih championa za prikaz u UI-ju.
     */
    @GetMapping("/champions")
    public ResponseEntity<List<String>> getChampions() {
        return ResponseEntity.ok(service.getAvailableChampions());
    }
}
