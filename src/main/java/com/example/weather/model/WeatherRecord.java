package com.example.weather.model;

/** An immutable weather observation associated with a person and city. */
public record WeatherRecord(String name, String city, String weather, int temperatureCelsius) {
}
