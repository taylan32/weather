package com.example.weather.controller;

import com.example.weather.controller.validation.CityNameConstraint;
import com.example.weather.dto.WeatherDto;
import com.example.weather.service.WeatherService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/weather")
@Validated // String gibi primitive type validator çalışmasını sağlar. Request için @Valid yazmak yeterli bunda olmaz
@Tag(name = "Open Weather API v1", description = "Open Weather API filtering by city for current temperature.")
public class WeatherController {


    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }


    @Operation(
            method = "GET",
            summary = "Open Weather API summary",
            description = "Open Weather API filtering by city for current temperature.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = WeatherDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    schema = @Schema(implementation = ConstraintViolationException.class)
                            )
                    )
                    ,@ApiResponse(
                            responseCode = "429",
                            description = "Too many request",
                            content = @Content(
                                    schema = @Schema(hidden = true)
                            )
                    )
            }
    )
    @GetMapping("/{city}")
    @RateLimiter(name = "basic")
    public ResponseEntity<WeatherDto> getWeather(@PathVariable("city") @CityNameConstraint String city) {
        return ResponseEntity.ok(weatherService.getWeatherByCityName(city));
    }

}
