/******************************************
  Venturus - Centro de Inovação Tecnológica

  Exemplo 6 - Ler Porta Serial

  @Author: Sylvio Alves
  @Date: Fev/2016
******************************************/

void ligaLed(int porta);    // liga o led na porta
void desligaLed(int porta); // desliga o led na porta

void setup() {
  pinMode(0, OUTPUT);     // led vermelho
  desligaLed(0);

  Serial.begin(115200);
  delay(2000);

  Serial.println("Ligando Arduino...");
  Serial.println("Aguardando entrada...");
}

void loop() {

  // Se existe conteúdo na porta serial
  if (Serial.available()) {

    // le porta serial ate final de linha
    String texto = Serial.readStringUntil('\n');

    if (texto == "led=1") {
      ligaLed(0);
    } else if (texto == "led=0") {
      desligaLed(0);
    }
    
    Serial.print(texto);
    Serial.println();
  }
}

void ligaLed(int porta) {
  digitalWrite(porta, LOW); // liga led vermelho
}

void desligaLed(int porta) {
  digitalWrite(porta, HIGH);  // desliga led vermelho
}

