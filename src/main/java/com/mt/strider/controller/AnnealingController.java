package com.mt.strider.controller;

import com.mt.strider.model.AnnealingRequest;
import com.mt.strider.model.AnnealingResult;
import com.mt.strider.model.Place;
import com.mt.strider.service.AnnealingService;
import com.mt.strider.service.PlacesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/annealing")
public class AnnealingController {

    private final AnnealingService annealingService;
    private final PlacesService placesService;

    public AnnealingController(AnnealingService annealingService, PlacesService placesService) {
        this.annealingService = annealingService;
        this.placesService = placesService;
    }

    @PostMapping
    public AnnealingResult getAnnealing(@RequestBody AnnealingRequest annealingRequest) {
        return this.annealingService.performAnnealing(annealingRequest);
    }

    @GetMapping
    public List<Place> getAllPlaces() {
        return placesService.getAllPlaces();
    }
}
