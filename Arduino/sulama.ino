// Relay pin is controlled with D8. The active wire is connected to Normally Closed and common
int relay = 8;
const unsigned long SECOND = 1000;
const unsigned long MINUTE = 60*SECOND;
const unsigned long HOUR = 3600*SECOND;

void setup() {
  pinMode(relay, OUTPUT);
  digitalWrite(relay, HIGH);
  Serial.begin(9600);
}

void loop() {
  digitalWrite(relay, LOW);
  Serial.println("ON");
  delay(30*MINUTE);
  //delay(MINUTE);
  digitalWrite(relay, HIGH);
  Serial.println("OFF");
  //delay(2*MINUTE);
  delay(48*HOUR-30*MINUTE);
}

