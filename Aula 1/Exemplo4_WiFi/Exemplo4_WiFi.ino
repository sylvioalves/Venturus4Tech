/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 4 - WiFi

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/

#include <ESP8266WiFi.h>

// WiFi
char ssid[] = "WSony_Lab";   // Nome da rede WiFi
char pass[] = "Wsony2016";   // Senha

// Declarando funções do código
void setup_wifi();

void setup() {
  pinMode(0, OUTPUT);
  setup_wifi();
}


void setup_wifi() {
  Serial.println();
  Serial.print("Conectando ao SSID: ");
  Serial.println(ssid);

  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("Conectado ao WiFi!");
  Serial.print("Endereço IP: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  
}

