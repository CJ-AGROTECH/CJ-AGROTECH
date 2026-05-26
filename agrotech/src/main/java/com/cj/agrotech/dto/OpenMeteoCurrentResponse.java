package com.cj.agrotech.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMeteoCurrentResponse {
    private Current current;

    @Getter
    @Setter
    public static class Current {
        private Float temperature_2m;
        private Float relative_humidity_2m;
        private Float wind_speed_10m;
        private Float precipitation;
    }
}
