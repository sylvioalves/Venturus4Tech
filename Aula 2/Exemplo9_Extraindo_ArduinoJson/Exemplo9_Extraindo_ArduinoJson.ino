/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 9 - Extraindo um ArduinoJson

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/
#include <ArduinoJson.h>

void extrai_json(String json);

void setup() {
  pinMode(0, OUTPUT);     // led vermelho

  Serial.begin(115200);
  delay(2000);

  Serial.println("Ligando Arduino...");
  Serial.println();
}

void extrai_json(String json) {

  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& buffer = jsonBuffer.parseObject(json);

  if (!buffer.success()) {
    Serial.println("Json apresenta erros..");
    return;
  }

  if (buffer.containsKey("lampada")) {
    boolean lampada = buffer["lampada"];
    Serial.print("Lâmpada: ");
    Serial.println(lampada == true ? "Ligada" : "Desligada");
  }

  if (buffer.containsKey("temperatura")) {
    double temperatura = buffer["temperatura"];
    Serial.print("Temperatura: ");
    Serial.println(temperatura);
  }
}

void loop() {
  if (Serial.available()) {
    String texto = Serial.readStringUntil('\n');
    extrai_json(texto);
  }
}

