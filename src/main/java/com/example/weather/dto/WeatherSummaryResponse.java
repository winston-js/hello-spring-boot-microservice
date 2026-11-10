package com.example.weather.dto;

import java.math.BigDecimal;
import java.util.Map;

public record WeatherSummaryResponse(
        int totalRecords,
        BigDecimal averageTemperature,
        String warmestCity,
        String coldestCity,
        Map<String, Long> weatherCounts) {
}
