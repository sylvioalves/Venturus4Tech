/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 2 - Pisca LED sem delay()

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/

int ledState = LOW;

unsigned long previousMillis = 0;
const long interval = 1000;

// Setup é executada apenas na inicialização
void setup() {
  pinMode(0, OUTPUT); // configura porta 0 (LED vermelho) como saída
}

// Função executada sem parar
void loop() {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    if (ledState == LOW) {
      ledState = HIGH;
    } else {
      ledState = LOW;
    }
    digitalWrite(0, ledState);
  }
}

