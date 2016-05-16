/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 3 - Porta Serial

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/

int ledState = LOW;

unsigned long previousMillis = 0;
const long interval = 1000;

// Setup é executada apenas na inicialização
void setup() {
  pinMode(0, OUTPUT);   // configura porta 0 (LED vermelho) como saída
  Serial.begin(115200); // Configura porta serial a 115200 baudrate 

  delay(1000);
  Serial.println("Ligando o Arduino...");
}

// Função executada sem parar
void loop() {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    if (ledState == LOW) {
      Serial.println("LED desligado...");
      ledState = HIGH;
    } else {
      Serial.println("LED ligado...");
      ledState = LOW;
    }
    digitalWrite(0, ledState);
  }
}

