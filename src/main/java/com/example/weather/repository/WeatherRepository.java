package com.example.weather.repository;

import com.example.weather.model.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, String> {

    // select * from weathers where city_name=city order by updated_time desc limit 1
    Optional<Weather> findFirstByRequestedCityNameOrderByUpdatedTimeDesc(String city);

}
