/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 11 - Versao Final

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <Wire.h>
#include <ArduinoJson.h>
#include "Adafruit_MCP9808.h"

// WiFi
char ssid[] = "WSony_Lab";   // Nome da rede WiFi
char pass[] = "Wsony2016";   // Senha

// Sensor de temperatura
Adafruit_MCP9808 sensor = Adafruit_MCP9808();

// Configuração do MQTT
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);
const char topico[] = "v4tech/sylvio/iot";
// AWS MQTT server: IP 54.165.138.84
IPAddress mqttServer(54, 165, 138, 84);

// funções
void analisaJson(String json);
void enviaJson();
void ligaLed(int porta);
void desligaLed(int porta);
void inverteLed(int porta);
void atualiza_led();
void atualiza_status();
void setup_wifi();
void conectaMqtt();

// led pode ser: 0 desligado, 1 ligado, 2 piscando
int ledState = 0;
int intervaloPisca = 1000;  // padrão 1 segundo
int ultimoPisca = 0;

// status da placa
int intervaloStatus = 20000; // padrão 20 segundos
int ultimoStatus = 0;

void setup() {
  pinMode(0, OUTPUT);     // led vermelho
  pinMode(2, OUTPUT);     // led azul

  Serial.begin(115200);
  delay(2000);  // aguarde porta serial funcionar

  // inicia wifi
  setup_wifi();

  // inicializa comunicação com sensor de temperatura
  if (!sensor.begin()) {
    Serial.println("Não de temperatura encontrado!");
    while (1);
  }

  // inicializa sensor
  sensor.shutdown_wake(0);

  // inicia cliente mqtt
  mqttClient.setServer(mqttServer, 1883);
  mqttClient.setCallback(callback);
}

void loop() {

  // se perder conexão, tente novamente
  if (WiFi.status() != WL_CONNECTED) {
    setup_wifi();
  }
  
  // reconecte-se ao servidor se MQTT estiver desconectado
  if (!mqttClient.connected()) {
    conectaMqtt();
  }
  // função loop deve ser chamada para receber mensagens
  mqttClient.loop();

  // atualiza estado do led
  atualiza_led();

  // atualiza status do sistema
  atualiza_status();
}

void callback(char* topic, byte* payload, unsigned int length) {

  // transforma o payload em string
  char content[200];
  int i = 0;
  for (i = 0; i < length; i++) {
    content[i] = (char) payload[i];
  }
  content[i] = '\0';
  
  Serial.print("Nova mensagem no topico: ");
  Serial.println(topic);
  Serial.print("Mensagem: ");
  Serial.println(content);

  analisaJson(content);
}

void setup_wifi() {
  Serial.println();
  Serial.print("Conectando ao SSID: ");
  Serial.println(ssid);

  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    inverteLed(2); // pisca led azul
    Serial.print(".");
  }

  // led azul ligado quando conectar na rede
  ligaLed(2);

  Serial.println("");
  Serial.println("Conectado ao WiFi!");
  Serial.print("Endereço IP: ");
  Serial.println(WiFi.localIP());
}

void conectaMqtt() {
  while (!mqttClient.connected()) {
    Serial.println("Tentando conexão com Broker MQTT...");

    // conecta e insere um id qualquer (coloque seu nome)
    if (mqttClient.connect("v4tech_sylvio")) {
      Serial.println("Conectado!");

      // faz subscribe no topico principal e já envia status
      mqttClient.subscribe(topico);
      enviaJson();
    } else {
      Serial.println("Ocorreu um erro na tentativa de conexão: ");
      Serial.println(mqttClient.state());
      Serial.println("Tentando novamente em 5 segundos...");
      delay(5000);
    }
  }
}

// recebe um Json da serial e extrai as informações
// {"status":0} -> Deve retornar um json completo com temperatura e estado do LED
// {"seta_led":ESTADO} -> liga ou desliga o LED. Valores: ligado, desligado, piscando
// {"pisca_led":MILLIS} -> led fica piscando. Valores: delay em millis (1000 = 1 segundo)
void analisaJson(String json) {
  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& buffer = jsonBuffer.parseObject(json);

  if (!buffer.success()) {
    Serial.println("Json apresenta erros..");
    return;
  }

  // atualiza estado do led
  if (buffer.containsKey("status")) {
    // retorna status
    enviaJson();
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
    
    // retorna estado do led
    enviaJson();
  }

  // pisca se for necessário
  if (buffer.containsKey("pisca_led")) {
    intervaloPisca = buffer["pisca_led"];
    ledState = 2;

    // retorna estado do led
    enviaJson();
  }
}

// envia um json completo
// {"led":ESTADO,"temperatura¨:VALOR} -> ESTADO  "ligado", "desligado", "piscando"
void enviaJson() {
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
  // monta json
  buffer["led"] = lampada;
  buffer["temperatura"] = temp;

  // envia para broker
  char mensagem[100];
  buffer.printTo(mensagem, sizeof(mensagem));
  mqttClient.publish(topico, mensagem, false);

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

// envia status para Broker em intervalos de tempo
void atualiza_status() {
  if (millis() - ultimoStatus > intervaloStatus) {
    ultimoStatus = millis();
    enviaJson();
  }
}

