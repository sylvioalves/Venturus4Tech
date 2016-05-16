/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 11 - Juntando tudo

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/
#include <ArduinoJson.h>
#include <Wire.h>
#include "Adafruit_MCP9808.h"

Adafruit_MCP9808 sensor = Adafruit_MCP9808();

// funções
void analisaJsonSerial(String json);
void enviaJsonSerial();
void ligaLed(int porta);
void desligaLed(int porta);
void inverteLed(int porta);
void atualiza_led();

// led pode ser: 0 desligado, 1 ligado, 2 piscando
int ledState = 0; 
int intervaloPisca = 1000;  // default 1s
int ultimoPisca = 0;

void setup() {
  pinMode(0, OUTPUT);     // led vermelho
  ligaLed(0);

  Serial.begin(115200);
  delay(2000);

  // inicializa comunicação com sensor de temperatura
  if (!sensor.begin()) {
    Serial.println("Não de temperatura encontrado!");
    while (1);
  }

  // inicializa sensor
  sensor.shutdown_wake(0);

  Serial.println("Ligando Arduino...");
  Serial.println();
}

void loop() {

  // se há dados na serial, leia-os
  if (Serial.available()) {
    // extrai string até final de linha
    String texto = Serial.readStringUntil('\n');
    // trata json
    analisaJsonSerial(texto);
  }

  // atualiza estado do led
  atualiza_led();
}

// recebe um Json da serial e extrai as informações
// {"status":0} -> Deve retornar um json completo com temperatura e estado do LED
// {"seta_led":ESTADO} -> liga ou desliga o LED. Valores: ligado, desligado, piscando
// {"pisca_led":MILLIS} -> led fica piscando. Valores: delay em millis (1000 = 1 segundo)
void analisaJsonSerial(String json) {
  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& buffer = jsonBuffer.parseObject(json);

  if (!buffer.success()) {
    Serial.println("Json apresenta erros..");
    return;
  }

  if (buffer.containsKey("status")) {
    Serial.println("Solicitado status.. enviando:");
    enviaJsonSerial();
  }

  // atualiza estado do led
  if (buffer.containsKey("seta_led")) {
    String lamp = buffer["seta_led"];
    if (lamp == "desligado") {
      ledState = 0;
    } else if (lamp == "ligado") {
      ledState = 1;
    } else if (lamp == "piscando") {
      ledState = 2;
    }
  }

  // pisca se for necessário
  if (buffer.containsKey("pisca_led")) {
    intervaloPisca = buffer["pisca_led"];
    ledState = 2;
  }
}

// envia um json completo
// {"led":ESTADO,"temperatura¨:VALOR} -> ESTADO  "ligado", "desligado", "piscando"
void enviaJsonSerial() {
  String lampada = "";

  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& buffer = jsonBuffer.createObject();

  // define a string do estado do led
  switch (ledState) {
    case 0:
      lampada = "desligado";
      break;
    case 1:
      lampada = "ligado";
      break;
    case 2:
      lampada = "piscando";
      break;
  }

  // le sensor de temperatura
  float temp = sensor.readTempC();
  delay(200);

  // monta json e envia para serial
  buffer["led"] = lampada;
  buffer["temperatura"] = temp;

  buffer.prettyPrintTo(Serial);
  Serial.println();

}

// função liga o led da porta
void ligaLed(int porta) {
  digitalWrite(porta, LOW);
}

// função desliga o led da porta
void desligaLed(int porta) {
  digitalWrite(porta, HIGH);
}

// inverte estado do led da porta
void inverteLed(int porta) {
  digitalWrite(porta, !digitalRead(porta));
}

// liga, desliga ou faz led piscar
void atualiza_led() {

  // define a string do estado do led
  switch (ledState) {
    case 0:
      desligaLed(0);
      break;
    case 1:
      ligaLed(0);
      break;
    case 2:
      if (millis() - ultimoPisca > intervaloPisca) {
        ultimoPisca = millis();
        inverteLed(0);
      }
      break;
  }
}

