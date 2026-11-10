package com.example.weather.repository;

import com.example.weather.model.WeatherRecord;

import java.util.List;
import java.util.Optional;

public interface WeatherRepository {

    List<WeatherRecord> findAll();

    Optional<WeatherRecord> findByName(String name);
}
