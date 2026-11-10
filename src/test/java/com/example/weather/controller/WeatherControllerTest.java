package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.service.WeatherNotFoundException;
import com.example.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void returnsWeatherJsonForSuccessfulLookup() throws Exception {
        when(weatherService.findByName("Winston")).thenReturn(
                new WeatherResponse("Winston", "London", "pleasant", 18,
                        "Hello Winston, the weather in London is pleasant."));

        mockMvc.perform(get("/api/weather/Winston"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Winston"))
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.weather").value("pleasant"))
                .andExpect(jsonPath("$.temperatureCelsius").value(18))
                .andExpect(jsonPath("$.message").value("Hello Winston, the weather in London is pleasant."));

        verify(weatherService).findByName("Winston");
    }

    @Test
    void returnsStructuredJson404ForUnknownName() throws Exception {
        when(weatherService.findByName("Unknown")).thenThrow(new WeatherNotFoundException("Unknown"));

        mockMvc.perform(get("/api/weather/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No weather record found for name: Unknown"))
                .andExpect(jsonPath("$.path").value("/api/weather/Unknown"));
    }

    @Test
    void passesOptionalFilterParametersToService() throws Exception {
        when(weatherService.findWeather("sunny", 15, 25)).thenReturn(
                List.of(new WeatherResponse("Emma", "Bristol", "sunny", 22,
                        "Hello Emma, the weather in Bristol is sunny.")));

        mockMvc.perform(get("/api/weather")
                        .param("weather", "sunny")
                        .param("minTemperature", "15")
                        .param("maxTemperature", "25"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Emma"))
                .andExpect(jsonPath("$[0].temperatureCelsius").value(22));

        verify(weatherService).findWeather("sunny", 15, 25);
    }

    @Test
    void returnsSummaryEndpointAsJson() throws Exception {
        when(weatherService.getSummary()).thenReturn(new WeatherSummaryResponse(
                8, new BigDecimal("15.4"), "Bristol", "Edinburgh",
                Map.of("sunny", 2L, "rainy", 2L, "pleasant", 2L, "cold", 1L, "cloudy", 1L)));

        mockMvc.perform(get("/api/weather/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalRecords").value(8))
                .andExpect(jsonPath("$.averageTemperature").value(15.4))
                .andExpect(jsonPath("$.warmestCity").value("Bristol"))
                .andExpect(jsonPath("$.coldestCity").value("Edinburgh"))
                .andExpect(jsonPath("$.weatherCounts.sunny").value(2));

        verify(weatherService).getSummary();
    }

    @Test
    void returnsGroupedCitiesEndpointAsJson() throws Exception {
        when(weatherService.getCitiesGroupedByWeather()).thenReturn(
                Map.of("sunny", List.of("Bristol", "London"), "rainy", List.of("Glasgow", "Manchester")));

        mockMvc.perform(get("/api/weather/grouped"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sunny[0]").value("Bristol"))
                .andExpect(jsonPath("$.sunny[1]").value("London"))
                .andExpect(jsonPath("$.rainy[0]").value("Glasgow"));

        verify(weatherService).getCitiesGroupedByWeather();
    }
}
