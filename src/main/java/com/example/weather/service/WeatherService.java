package com.example.weather.service;

import com.example.weather.constant.Constants;
import com.example.weather.dto.WeatherDto;
import com.example.weather.dto.WeatherResponse;
import com.example.weather.model.Weather;
import com.example.weather.repository.WeatherRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"weathers"})
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherService(WeatherRepository weatherRepository,
                          RestTemplate restTemplate) {
        this.weatherRepository = weatherRepository;
        this.restTemplate = restTemplate;
    }

    @Cacheable(key = "#city")
    public WeatherDto getWeatherByCityName(String city) {
        logger.info("Requested city: " + city);
        Optional<Weather> weather = weatherRepository.findFirstByRequestedCityNameOrderByUpdatedTimeDesc(city);
        return weather.map(w -> {
            if(w.getUpdatedTime().isBefore(LocalDateTime.now().minusMinutes(30))) {
                return WeatherDto.convert(getWeatherFromWeatherStack(city));
            }
            return WeatherDto.convert(w);
        }).orElseGet(() -> WeatherDto.convert(getWeatherFromWeatherStack(city)));

    }

    private Weather getWeatherFromWeatherStack(String city) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getWeatherStackUrl(city), String.class);
        try {
            WeatherResponse weatherResponse = objectMapper.readValue(responseEntity.getBody(), WeatherResponse.class);
            return saveWeather(city, weatherResponse);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }

    }

    private Weather saveWeather(String city, WeatherResponse weatherResponse) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Weather weather = new Weather(
                city,
                weatherResponse.location().name(),
                weatherResponse.location().country(),
                weatherResponse.current().temperature(),
                LocalDateTime.now(),
                LocalDateTime.parse(weatherResponse.location().localtime(), dateTimeFormatter)

        );
        return weatherRepository.save(weather);
    }

    private String getWeatherStackUrl(String city) {
        return Constants.API_URL + Constants.ACCESS_KEY_PARAM + Constants.API_KEY + Constants.QUERY_KEY_PARAM + city;
    }

    // Cache methodları public yazılmak zorunda.
    @CacheEvict(allEntries = true) // allEntries tüm cache temizle
    @PostConstruct // construct edildikten sonra calis
    @Scheduled(fixedRateString = "10000") // 10 sn de bir çalıştır
    public void clearCache() {
        logger.info("Cache cleared.");
    }

}
