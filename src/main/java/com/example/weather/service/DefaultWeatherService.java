package com.example.weather.service;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.model.WeatherRecord;
import com.example.weather.repository.WeatherRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

@Service
public class DefaultWeatherService implements WeatherService {

    private final WeatherRepository weatherRepository;

    public DefaultWeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    @Override
    public WeatherResponse findByName(String name) {
        WeatherRecord record = weatherRepository.findByName(name)
                .orElseThrow(() -> new WeatherNotFoundException(name));
        return toResponse(record);
    }

    @Override
    public List<WeatherResponse> findWeather(String weather, Integer minTemperature, Integer maxTemperature) {
        if (minTemperature != null && maxTemperature != null && minTemperature > maxTemperature) {
            throw new InvalidWeatherFilterException("minTemperature must be less than or equal to maxTemperature.");
        }

        return weatherRepository.findAll().stream()
                .filter(record -> weather == null || record.weather().equalsIgnoreCase(weather))
                .filter(record -> minTemperature == null || record.temperatureCelsius() >= minTemperature)
                .filter(record -> maxTemperature == null || record.temperatureCelsius() <= maxTemperature)
                .sorted((first, second) -> {
                    int byTemperature = Integer.compare(first.temperatureCelsius(), second.temperatureCelsius());
                    return byTemperature != 0 ? byTemperature : first.name().compareToIgnoreCase(second.name());
                })
                .map(this::toResponse)
                .toList();
    }

    @Override
    public WeatherSummaryResponse getSummary() {
        List<WeatherRecord> records = weatherRepository.findAll();
        double average = records.stream()
                .mapToInt(WeatherRecord::temperatureCelsius)
                .average()
                .orElse(0.0);

        String warmestCity = records.stream()
                .max((first, second) -> Integer.compare(first.temperatureCelsius(), second.temperatureCelsius()))
                .map(WeatherRecord::city)
                .orElse(null);
        String coldestCity = records.stream()
                .min((first, second) -> Integer.compare(first.temperatureCelsius(), second.temperatureCelsius()))
                .map(WeatherRecord::city)
                .orElse(null);

        Map<String, Long> weatherCounts = records.stream()
                .collect(groupingBy(WeatherRecord::weather, TreeMap::new, counting()));

        return new WeatherSummaryResponse(
                records.size(),
                BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP),
                warmestCity,
                coldestCity,
                weatherCounts);
    }

    @Override
    public Map<String, List<String>> getCitiesGroupedByWeather() {
        return weatherRepository.findAll().stream()
                .collect(groupingBy(
                        WeatherRecord::weather,
                        TreeMap::new,
                        mapping(WeatherRecord::city,
                                Collectors.collectingAndThen(Collectors.toList(),
                                        cities -> cities.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList()))));
    }

    private WeatherResponse toResponse(WeatherRecord record) {
        String message = "Hello " + record.name() + ", the weather in " + record.city()
                + " is " + record.weather() + ".";
        return new WeatherResponse(record.name(), record.city(), record.weather(), record.temperatureCelsius(), message);
    }
}
