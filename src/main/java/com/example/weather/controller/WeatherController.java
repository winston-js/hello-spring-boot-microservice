package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{name}")
    public WeatherResponse findByName(@PathVariable String name) {
        return weatherService.findByName(name);
    }

    @GetMapping
    public List<WeatherResponse> findWeather(
            @RequestParam(required = false) String weather,
            @RequestParam(required = false) Integer minTemperature,
            @RequestParam(required = false) Integer maxTemperature) {
        return weatherService.findWeather(weather, minTemperature, maxTemperature);
    }

    @GetMapping("/summary")
    public WeatherSummaryResponse getSummary() {
        return weatherService.getSummary();
    }

    @GetMapping("/grouped")
    public Map<String, List<String>> getCitiesGroupedByWeather() {
        return weatherService.getCitiesGroupedByWeather();
    }
}
