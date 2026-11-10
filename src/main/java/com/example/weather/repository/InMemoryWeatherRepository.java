package com.example.weather.repository;

import com.example.weather.model.WeatherRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryWeatherRepository implements WeatherRepository {

    private final List<WeatherRecord> records = List.of(
            new WeatherRecord("Winston", "London", "pleasant", 18),
            new WeatherRecord("John", "Manchester", "rainy", 12),
            new WeatherRecord("Sarah", "Edinburgh", "cold", 8),
            new WeatherRecord("Emma", "Bristol", "sunny", 22),
            new WeatherRecord("David", "Birmingham", "cloudy", 15),
            new WeatherRecord("Sophia", "London", "sunny", 21),
            new WeatherRecord("James", "Glasgow", "rainy", 10),
            new WeatherRecord("Olivia", "Leeds", "pleasant", 17));

    @Override
    public List<WeatherRecord> findAll() {
        return records;
    }

    @Override
    public Optional<WeatherRecord> findByName(String name) {
        return records.stream()
                .filter(record -> record.name().equalsIgnoreCase(name))
                .findFirst();
    }
}
