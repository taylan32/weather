package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public record WeatherResponse(
        Request request,
        Location location,
        Current current
) {
}
