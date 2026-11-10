# Weather Service

A small Java 17 and Spring Boot REST microservice that demonstrates a clear controller-service-repository architecture, JSON APIs, dependency injection, Java Streams, Maven, and automated testing. Weather data is stored in an immutable in-memory collection; no database or external service is required.

## What this project demonstrates

- Java 17, including records, immutable collection factories, and Stream APIs.
- Spring Boot REST controllers and JSON serialization.
- Layered architecture with interfaces at the service and repository boundaries.
- Constructor-based dependency injection.
- Java Streams and Collectors for filtering, sorting, aggregation, and grouping.
- JUnit 5, Mockito, and Spring MockMvc tests.
- Maven build and test lifecycle.

## Architecture

```text
Client
  |
  v
WeatherController
  |
  v
WeatherService
  |
  v
WeatherRepository
  |
  v
In-memory WeatherRecord collection
```

The controller handles HTTP request mapping and delegates to the service. The service contains the application logic and maps immutable domain records into response DTOs. The repository provides weather records and is replaceable through its interface. The controller never accesses the repository directly.

### Request flow

For `GET /api/weather/Winston`, Spring routes the request to `WeatherController`. The controller calls `WeatherService.findByName`, which asks `WeatherRepository` for an optional matching record, converts it to a response DTO, and returns it. Spring serializes that DTO to JSON. Missing names are translated by `ApiExceptionHandler` into a structured HTTP 404 JSON response.

### Domain model and data

`WeatherRecord` is an immutable Java record containing `name`, `city`, `weather`, and `temperatureCelsius`. `InMemoryWeatherRepository` holds eight sample records and performs case-insensitive name lookup. No persistence layer or database is used.

## REST endpoints

All endpoints are under `/api/weather`.

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/weather/{name}` | Find one record by name (case-insensitive). |
| GET | `/api/weather` | List records, optionally filtering by `weather`, `minTemperature`, and/or `maxTemperature`. Results are sorted by temperature ascending. |
| GET | `/api/weather/summary` | Return record count, average temperature, warmest/coldest city, and counts by weather condition. |
| GET | `/api/weather/grouped` | Return city names grouped by weather condition; cities in each group are alphabetically sorted. |

### Example requests and responses

```bash
curl http://localhost:8080/api/weather/Winston
```

```json
{
  "name": "Winston",
  "city": "London",
  "weather": "pleasant",
  "temperatureCelsius": 18,
  "message": "Hello Winston, the weather in London is pleasant."
}
```

Name matching ignores case, so `/api/weather/wInStOn` returns the same record. Unknown names return HTTP 404 with an error body such as:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "No weather record found for name: Unknown",
  "path": "/api/weather/Unknown"
}
```

The timestamp is generated when the error occurs.

```bash
curl 'http://localhost:8080/api/weather?weather=sunny'
curl 'http://localhost:8080/api/weather?minTemperature=15&maxTemperature=20'
curl 'http://localhost:8080/api/weather?weather=pleasant&minTemperature=15'
```

The filter endpoint returns a JSON array. For example, the sunny filter returns Emma's Bristol record and Sophia's London record, ordered by temperature:

```json
[
  {
    "name": "Sophia",
    "city": "London",
    "weather": "sunny",
    "temperatureCelsius": 21,
    "message": "Hello Sophia, the weather in London is sunny."
  },
  {
    "name": "Emma",
    "city": "Bristol",
    "weather": "sunny",
    "temperatureCelsius": 22,
    "message": "Hello Emma, the weather in Bristol is sunny."
  }
]
```

```bash
curl http://localhost:8080/api/weather/summary
```

```json
{
  "totalRecords": 8,
  "averageTemperature": 15.4,
  "warmestCity": "Bristol",
  "coldestCity": "Edinburgh",
  "weatherCounts": {
    "cloudy": 1,
    "cold": 1,
    "pleasant": 2,
    "rainy": 2,
    "sunny": 2
  }
}
```

```bash
curl http://localhost:8080/api/weather/grouped
```

```json
{
  "cloudy": ["Birmingham"],
  "cold": ["Edinburgh"],
  "pleasant": ["Leeds", "London"],
  "rainy": ["Glasgow", "Manchester"],
  "sunny": ["Bristol", "London"]
}
```

Map key order is not part of the JSON API contract. City names within each weather group are sorted alphabetically.

## Java Streams

Streams are used where they make collection operations clear and declarative:

- Filter queries use `filter()` for optional weather and temperature criteria, `sorted()` for ascending temperature, `map()` to create response DTOs, and `toList()` to collect results.
- The summary uses `mapToInt().average()` for the mean, `max()` and `min()` for the warmest and coldest records, and `Collectors.groupingBy()` with `Collectors.counting()` for weather totals.
- The grouped endpoint uses `Collectors.groupingBy()` and `mapping()` to collect cities by condition, then sorts the city names within each group.
- Repository lookup uses a short stream pipeline to find the first case-insensitive name match.

The streams operate on a small in-memory list, so no database or persistence-specific behavior is hidden in the examples.

## Dependency injection

`WeatherController` depends on the `WeatherService` interface, and `DefaultWeatherService` depends on the `WeatherRepository` interface. Spring discovers the controller, service, and repository beans using component annotations and injects dependencies through constructors. This keeps the layers loosely coupled and allows the service to be tested with a Mockito repository mock.

## Build and run

Requirements: JDK 17 or newer and Maven 3.6+.

Run the automated tests and package the application:

```bash
mvn clean verify
```

Start the service:

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. To build an executable jar and run it directly:

```bash
mvn package
java -jar target/weather-service-1.0.0.jar
```

## Tests: JUnit 5, Mockito, and MockMvc

The test suite includes:

- **Repository tests** that instantiate the in-memory implementation and cover all records, known and unknown names, and case-insensitive lookup.
- **Service unit tests** that use JUnit 5 with Mockito's `@Mock` and `@InjectMocks`. They isolate the service from the real repository and cover lookup, filtering, sorting, summary calculations, grouping, empty data, and invalid ranges. Repository interactions are verified.
- **Controller tests** using Spring's `@WebMvcTest` and `MockMvc` to exercise HTTP routing, query parameters, JSON structure, and structured 404 handling while mocking the service.

Run only the tests with:

```bash
mvn test
```
