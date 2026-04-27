#include <WiFi.h>
#include <HTTPClient.h>
#include <DHT.h>

// Configuración WiFi
const char* ssid = "TU_SSID";
const char* password = "TU_PASSWORD";

// URL del endpoint de captura
const char* serverUrl = "http://TU_BACKEND_IP:8080/api/v1/telemetria/captura";

// MAC Address del dispositivo (único)
String macAddress = WiFi.macAddress();

// Pines de sensores
#define DHTPIN 4          // DHT22 para temp/hum aire
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

#define SOIL_MOISTURE_PIN 34  // Humedad suelo (analógico)
#define SOIL_TEMP_PIN 35      // Temperatura suelo (analógico, simulado)
#define LIGHT_PIN 33          // Luminosidad (LDR)
#define PRESSURE_PIN 32       // Presión (BMP180, simulado)
#define RAIN_PIN 25           // Precipitación (sensor lluvia)
#define WIND_PIN 26           // Velocidad viento (anemómetro)

// Variables para lecturas
float tempAire, humAire, presion, lux, humSuelo, tempSuelo, precipitacion, viento;
int bateria, rssiWifi;

void setup() {
  Serial.begin(115200);
  dht.begin();

  // Conectar WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando a WiFi...");
  }
  Serial.println("Conectado a WiFi");

  // Configurar pines
  pinMode(SOIL_MOISTURE_PIN, INPUT);
  pinMode(SOIL_TEMP_PIN, INPUT);
  pinMode(LIGHT_PIN, INPUT);
  pinMode(PRESSURE_PIN, INPUT);
  pinMode(RAIN_PIN, INPUT);
  pinMode(WIND_PIN, INPUT);
}

void loop() {
  // Leer sensores
  tempAire = dht.readTemperature();
  humAire = dht.readHumidity();
  presion = analogRead(PRESSURE_PIN) * 0.1; // Simulado
  lux = analogRead(LIGHT_PIN);
  humSuelo = analogRead(SOIL_MOISTURE_PIN) / 1023.0 * 100.0;
  tempSuelo = analogRead(SOIL_TEMP_PIN) * 0.1; // Simulado
  precipitacion = digitalRead(RAIN_PIN) == HIGH ? 1.0 : 0.0; // Simulado
  viento = analogRead(WIND_PIN) * 0.5; // Simulado

  bateria = 85; // Simulado
  rssiWifi = WiFi.RSSI();

  // Crear JSON
  String jsonPayload = "{";
  jsonPayload += "\"macAddress\":\"" + macAddress + "\",";
  jsonPayload += "\"lecturas\":{";
  jsonPayload += "\"ambiente\":{\"tempAire\":" + String(tempAire) + ",\"humAire\":" + String(humAire) + ",\"presion\":" + String(presion) + ",\"lux\":" + String(lux) + "},";
  jsonPayload += "\"suelo\":{\"humSuelo\":" + String(humSuelo) + ",\"tempSuelo\":" + String(tempSuelo) + "},";
  jsonPayload += "\"clima\":{\"precipitacion\":" + String(precipitacion) + ",\"viento\":" + String(viento) + "}";
  jsonPayload += "},";
  jsonPayload += "\"diagnostico\":{\"bateria\":" + String(bateria) + ",\"rssiWifi\":" + String(rssiWifi) + "}";
  jsonPayload += "}";

  // Enviar POST
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");

    int httpResponseCode = http.POST(jsonPayload);
    if (httpResponseCode > 0) {
      Serial.println("Datos enviados: " + String(httpResponseCode));
    } else {
      Serial.println("Error enviando datos: " + String(httpResponseCode));
    }
    http.end();
  }

  delay(60000); // Enviar cada 1 minuto (ajustar según necesidad)
}
