/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 10 - Sensor de Temperatura

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/
#include <Wire.h>
#include "Adafruit_MCP9808.h"

Adafruit_MCP9808 sensor = Adafruit_MCP9808();

void mostra_temperatura();

void setup() {
  pinMode(0, OUTPUT);     // led vermelho

  Serial.begin(115200);
  delay(2000);

  Serial.println("Ligando Arduino...");
  Serial.println();

  // inicializa comunicação com sensor de temperatura
  if (!sensor.begin()) {
    Serial.println("Sensor de temperatura não encontrado!");
    while (1);
  }

  // inicializa sensor
  sensor.shutdown_wake(0);
}

void mostra_temperatura() {
  float c = sensor.readTempC();
  delay(250);
  
  Serial.print("Temperatura: ");
  Serial.print(c);
  Serial.println(" C");
}

void loop() {
  mostra_temperatura();
  delay(1000);
}

