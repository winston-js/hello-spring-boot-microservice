package com.example.weather.repository;

import com.example.weather.model.WeatherRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryWeatherRepositoryTest {

    private final InMemoryWeatherRepository repository = new InMemoryWeatherRepository();

    @Test
    void findAllReturnsAllEightSampleRecords() {
        List<WeatherRecord> records = repository.findAll();

        assertEquals(8, records.size());
        assertEquals("Winston", records.getFirst().name());
    }

    @Test
    void findByNameReturnsKnownRecord() {
        assertEquals(Optional.of(new WeatherRecord("Winston", "London", "pleasant", 18)),
                repository.findByName("Winston"));
    }

    @Test
    void findByNameReturnsEmptyForUnknownName() {
        assertTrue(repository.findByName("Unknown").isEmpty());
    }

    @Test
    void findByNameIsCaseInsensitive() {
        assertEquals("Winston", repository.findByName("wINsTon").orElseThrow().name());
    }
}
