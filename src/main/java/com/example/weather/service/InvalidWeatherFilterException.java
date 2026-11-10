package com.example.weather.service;

public class InvalidWeatherFilterException extends RuntimeException {

    public InvalidWeatherFilterException(String message) {
        super(message);
    }
}
