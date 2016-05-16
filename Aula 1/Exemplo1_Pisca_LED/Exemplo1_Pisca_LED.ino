/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 1 - Pisca LED com delay()

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/

// Setup é executada apenas na inicialização
void setup() {
  pinMode(0, OUTPUT); // configura porta 0 (LED vermelho) como saída
}

// Função executada sem parar
void loop() {
  digitalWrite(0, LOW); // liga o LED
  delay(1000);          // aguarda 1 segundo
  digitalWrite(0, HIGH);// desliga o LED
  delay(1000);          // aguarda 1 segundo
}

