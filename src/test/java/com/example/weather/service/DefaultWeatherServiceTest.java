package com.example.weather.service;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.model.WeatherRecord;
import com.example.weather.repository.WeatherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultWeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private DefaultWeatherService weatherService;

    private final WeatherRecord winston = new WeatherRecord("Winston", "London", "pleasant", 18);
    private final WeatherRecord emma = new WeatherRecord("Emma", "Bristol", "sunny", 22);
    private final WeatherRecord sarah = new WeatherRecord("Sarah", "Edinburgh", "cold", 8);
    private final WeatherRecord olivia = new WeatherRecord("Olivia", "Leeds", "pleasant", 17);

    @Test
    void findsKnownNameAndBuildsResponseMessage() {
        when(weatherRepository.findByName("Winston")).thenReturn(Optional.of(winston));

        assertEquals(new WeatherResponse("Winston", "London", "pleasant", 18,
                "Hello Winston, the weather in London is pleasant."), weatherService.findByName("Winston"));

        verify(weatherRepository).findByName("Winston");
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void throwsForUnknownName() {
        when(weatherRepository.findByName("Nobody")).thenReturn(Optional.empty());

        assertThrows(WeatherNotFoundException.class, () -> weatherService.findByName("Nobody"));

        verify(weatherRepository).findByName("Nobody");
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void delegatesCaseInsensitiveLookupToRepositoryContract() {
        when(weatherRepository.findByName("wINsTon")).thenReturn(Optional.of(winston));

        assertEquals("Winston", weatherService.findByName("wINsTon").name());

        verify(weatherRepository).findByName("wINsTon");
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void filtersByWeatherCaseInsensitively() {
        when(weatherRepository.findAll()).thenReturn(List.of(winston, emma, sarah));

        List<WeatherResponse> result = weatherService.findWeather("SUNNY", null, null);

        assertEquals(List.of("Emma"), result.stream().map(WeatherResponse::name).toList());
        verify(weatherRepository).findAll();
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void filtersByMinimumTemperature() {
        when(weatherRepository.findAll()).thenReturn(List.of(winston, sarah));

        List<WeatherResponse> result = weatherService.findWeather(null, 15, null);

        assertEquals(List.of("Winston"), result.stream().map(WeatherResponse::name).toList());
        verify(weatherRepository).findAll();
    }

    @Test
    void filtersByTemperatureRange() {
        when(weatherRepository.findAll()).thenReturn(List.of(winston, emma, sarah));

        List<WeatherResponse> result = weatherService.findWeather(null, 10, 20);

        assertEquals(List.of("Winston"), result.stream().map(WeatherResponse::name).toList());
        verify(weatherRepository).findAll();
    }

    @Test
    void sortsResultsByTemperatureAscending() {
        when(weatherRepository.findAll()).thenReturn(List.of(winston, emma, sarah, olivia));

        List<WeatherResponse> result = weatherService.findWeather(null, null, null);

        assertEquals(List.of("Sarah", "Olivia", "Winston", "Emma"),
                result.stream().map(WeatherResponse::name).toList());
        verify(weatherRepository).findAll();
    }

    @Test
    void calculatesSummaryUsingRepositoryRecords() {
        when(weatherRepository.findAll()).thenReturn(List.of(winston, emma, sarah, olivia));

        WeatherSummaryResponse summary = weatherService.getSummary();

        assertEquals(4, summary.totalRecords());
        assertEquals(new BigDecimal("16.3"), summary.averageTemperature());
        assertEquals("Bristol", summary.warmestCity());
        assertEquals("Edinburgh", summary.coldestCity());
        assertEquals(Map.of("pleasant", 2L, "sunny", 1L, "cold", 1L), summary.weatherCounts());
        verify(weatherRepository).findAll();
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void groupsCitiesByWeatherAndSortsCitiesAlphabetically() {
        WeatherRecord londonSunny = new WeatherRecord("Alex", "London", "sunny", 20);
        when(weatherRepository.findAll()).thenReturn(List.of(emma, londonSunny, winston, olivia));

        Map<String, List<String>> grouped = weatherService.getCitiesGroupedByWeather();

        assertEquals(List.of("Bristol", "London"), grouped.get("sunny"));
        assertEquals(List.of("Leeds", "London"), grouped.get("pleasant"));
        verify(weatherRepository).findAll();
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void handlesEmptyRepositoryForFiltersSummaryAndGrouping() {
        when(weatherRepository.findAll()).thenReturn(List.of());

        assertTrue(weatherService.findWeather(null, null, null).isEmpty());
        WeatherSummaryResponse summary = weatherService.getSummary();
        assertEquals(0, summary.totalRecords());
        assertEquals(new BigDecimal("0.0"), summary.averageTemperature());
        assertEquals(null, summary.warmestCity());
        assertEquals(null, summary.coldestCity());
        assertTrue(summary.weatherCounts().isEmpty());
        assertTrue(weatherService.getCitiesGroupedByWeather().isEmpty());

        verify(weatherRepository, org.mockito.Mockito.times(3)).findAll();
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void rejectsAnInvalidTemperatureRange() {
        assertThrows(InvalidWeatherFilterException.class,
                () -> weatherService.findWeather(null, 20, 10));
    }
}
