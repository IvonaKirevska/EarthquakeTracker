package com.example.demo.web;

import com.example.demo.model.Earthquake;
import com.example.demo.service.EarthquakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/earthquakes")
@CrossOrigin(origins = "http://localhost:3000")
public class EarthquakeController {
    private final EarthquakeService earthquakeService;

    public EarthquakeController(EarthquakeService earthquakeService) {
        this.earthquakeService = earthquakeService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<List<Earthquake>> fetchAndStore(){
        List<Earthquake> earthquakes=earthquakeService.fetchAndStore();
        return ResponseEntity.ok(earthquakes);
    }

    @GetMapping
    public ResponseEntity<List<Earthquake>> getAll(){
        return ResponseEntity.ok(earthquakeService.getAll());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Earthquake>> getByMagnitude(@RequestParam Double minMag) {
        return ResponseEntity.ok(earthquakeService.getByMagnitudeGreaterThan(minMag));
    }

    @GetMapping("/after")
    public ResponseEntity<List<Earthquake>> getAfterTime(@RequestParam String time){
        Instant instant=Instant.parse(time);
        return ResponseEntity.ok(earthquakeService.getAfterTime(instant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        earthquakeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
