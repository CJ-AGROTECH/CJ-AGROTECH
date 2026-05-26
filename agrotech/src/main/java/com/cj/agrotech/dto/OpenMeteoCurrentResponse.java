package com.cj.agrotech.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public record OpenMeteoCurrentResponse(Current current) {
    @Getter
    @Setter
    public record Current(Float temperature_2m, Float relative_humidity_2m, Float wind_speed_10m, Float precipitation) {}
}
