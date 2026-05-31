package com.cj.agrotech.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;
import java.util.UUID;

@Document(collection = "telemetria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Telemetria {

    @Id
    private String id;

    @Field("dispositivo_id")
    private UUID dispositivoId;

    @Field("lote_id")
    private UUID loteId;

    @Field("finca_id")
    private UUID fincaId;

    private Instant timestamp;

    private Lecturas lecturas;
    private Diagnostico diagnostico;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Lecturas {
        private Ambiente ambiente;
        private Suelo suelo;
        private Clima clima;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Ambiente {
        private Float tempAire;
        private Float humAire;
        private Float presion;
        private Float lux;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Suelo {
        private Float humSuelo;
        private Float tempSuelo;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Clima {
        private Float precipitacion;
        private Float viento;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Diagnostico {
        private Integer bateria;
        private Integer rssiWifi;
    }
}