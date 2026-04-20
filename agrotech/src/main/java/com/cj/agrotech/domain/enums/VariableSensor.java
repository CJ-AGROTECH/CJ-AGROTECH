package com.cj.agrotech.domain.enums;


public enum VariableSensor {
    TEMP_AIRE("Temperatura del Aire"),
    HUM_AIRE("Humedad Relativa del Aire"),
    PRESION("Presión Atmosférica"),
    LUX("Luminosidad"),
    HUM_SUELO("Humedad del Suelo"),
    TEMP_SUELO("Temperatura del Suelo"),
    PRECIPITACION("Precipitación"),
    VIENTO("Velocidad del Viento");

    private final String descripcion;

    VariableSensor(String descripcion) { this.descripcion = descripcion; }
    public String getDescripcion() { return descripcion; }
}
