package com.example.weather.dto;

public record WeatherResponse(
        String name,
        String city,
        String weather,
        int temperatureCelsius,
        String message) {
}
