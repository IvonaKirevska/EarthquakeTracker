package com.example.demo.service;

import com.example.demo.model.Earthquake;
import com.example.demo.repository.EarthquakeRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.convert.ValueConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.util.JSONPObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EarthquakeService {
    private final EarthquakeRepository earthquakeRepository;
    private final RestTemplate restTemplate;

    @Value("${usgs.api.url}")
    private String usgsApiUrl;

    public EarthquakeService(EarthquakeRepository earthquakeRepository) {
        this.earthquakeRepository = earthquakeRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Earthquake> fetchAndStore() {
        String response = restTemplate.getForObject(usgsApiUrl, String.class);

        if (response == null) {
            throw new RuntimeException("Failed to fetch data from USGS API");
        }

        JSONObject geoJson = new JSONObject(response);
        JSONArray features = geoJson.getJSONArray("features");

        List<Earthquake> earthquakes = new ArrayList<>();

        for (int i = 0; i < features.length(); i++) {
            try {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");

                if (properties.isNull("mag") || properties.isNull("place") || properties.isNull("time") || properties.isNull("title")) {
                    continue;
                }

                Double mag = properties.getDouble("mag");
                String magType = properties.optString("magType", "unknown");
                String place = properties.getString("place");
                String title = properties.getString("title");
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                Double longitude = coordinates.getDouble(0);
                Double latitude = coordinates.getDouble(1);
                Instant time = Instant.ofEpochMilli(properties.getLong("time"));


                earthquakes.add(new Earthquake(mag, magType, place, title, time, latitude, longitude));
            } catch (Exception e) {
                System.err.println("Skipping malformed entry: " + e.getMessage());
            }
        }

        earthquakeRepository.deleteAll();
        return earthquakeRepository.saveAll(earthquakes);
    }

    public List<Earthquake> getAll() {
        return earthquakeRepository.findAll();
    }

    public List<Earthquake> getByMagnitudeGreaterThan(Double minMag) {
        return earthquakeRepository.findAll()
                .stream()
                .filter(e -> e.getMagnitude() != null && e.getMagnitude() > minMag)
                .toList();
    }

    public List<Earthquake> getAfterTime(Instant after) {
        return earthquakeRepository.findAll()
                .stream()
                .filter(e -> e.getTime().isAfter(after))
                .toList();
    }

    public void deleteById(Long id) {
        if (!earthquakeRepository.existsById(id)) {
            throw new RuntimeException("Earthquake with id " + id + " not found");
        }
        earthquakeRepository.deleteById(id);
    }
}
