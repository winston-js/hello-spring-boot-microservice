package com.example.weather.service;

public class WeatherNotFoundException extends RuntimeException {

    public WeatherNotFoundException(String name) {
        super("No weather record found for name: " + name);
    }
}
