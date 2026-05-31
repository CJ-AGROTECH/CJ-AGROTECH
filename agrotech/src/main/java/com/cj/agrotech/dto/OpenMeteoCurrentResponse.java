package com.cj.agrotech.dto;

import java.util.List;

public record OpenMeteoCurrentResponse(
        CurrentWeather current_weather,
        Hourly hourly
) {
    public record CurrentWeather(
            Float temperature,
            Float windspeed,
            Float winddirection,
            Integer weathercode,
            String time
    ) {}

    public record Hourly(
            List<String> time,
            List<Float> relativehumidity_2m,
            List<Float> precipitation,
            List<Float> wind_speed_10m,
            List<Float> surface_pressure,
            List<Float> soil_temperature_0_to_7cm,
            List<Float> soil_moisture_0_to_7cm
    ) {}
}
