package com.example.weather.service;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.dto.WeatherSummaryResponse;

import java.util.List;
import java.util.Map;

public interface WeatherService {

    WeatherResponse findByName(String name);

    List<WeatherResponse> findWeather(String weather, Integer minTemperature, Integer maxTemperature);

    WeatherSummaryResponse getSummary();

    Map<String, List<String>> getCitiesGroupedByWeather();
}
