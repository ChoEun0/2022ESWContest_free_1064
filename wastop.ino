 #include "Wire.h"
#include "MPU9250.h"
#include "I2Cdev.h"
#include "Servo.h"
#include <SoftwareSerial.h>

//블루투스
#define BT_RXD 12
#define BT_TXD 13
SoftwareSerial bt(BT_RXD, BT_TXD);

unsigned long time_previous, time_current; 

//자이로
const int MPU_ADDR = 0x68; 
int16_t AcX, AcY, AcZ, GyX, GyY, GyZ; 
double accelX, accelY;  // 오일러각(적분) 거친 가속도 
double gyroX, gyroY, gyroZ;  //오차의 평균값 빼준 자이로
double gyro_X = 0, gyro_Y = 0, gyro_Z = 0; //변화량 적분한 자이로
double angle_X = 0, angle_Y = 0, angle_Z = 0; //필터 적용 값

const double RADIAN_TO_DEGREE = 180 / 3.14159;  
const double DEG_PER_SEC = 32767 / 250;    // 자이로센서 1초에 회전하는 각도
const double ALPHA = 1 / (1 + 0.04);//(상보필터) 실험 통해 가장 맞는 필터 적용

unsigned long now = 0;   
unsigned long past = 0;  
double dt = 0;          

double averAcX, averAcY, averAcZ; 
double averGyX, averGyY, averGyZ; 


double sumAcX, sumAcY, sumAcZ;
double sumGyX, sumGyY, sumGyZ;
int i = 0;
int j = 0;
int k = 0;
int l = 0;
int m = 0;
int n = 0;


int Sensor = A1; // 압력센서

const byte interruptPin = 2; //포토 인터럽터
int count = 0; // 포토 인터럽터

#define trig 11 //초음파
#define echo 10 //초음파

Servo servo1, servo2, servo3, servo4; //서보

int buzzerPin = 3; //부저



void initSensor() {
  Wire.begin();
  Wire.beginTransmission(MPU_ADDR);   // I2C 통신용 주소
  Wire.write(0x68);    // MPU9250과 통신 시작용 주소   
  Wire.write(0);
  Wire.endTransmission(true);
} //자이로

void getData() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B);   // AcX 레지스터 주소
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDR, 12, true);  // AcX 주소 이후 12byte 데이터 요청
  AcX = Wire.read() << 8 | Wire.read(); 
  AcY = Wire.read() << 8 | Wire.read();
  AcZ = Wire.read() << 8 | Wire.read();
  GyX = Wire.read() << 8 | Wire.read();
  GyY = Wire.read() << 8 | Wire.read();
  GyZ = Wire.read() << 8 | Wire.read();
} //자이로

void getDT() {
  now = millis();   
  dt = (now - past) / 1000.0;  
  past = now;
} //자이로



void caliSensor() {
  double sumAcX = 0 , sumAcY = 0, sumAcZ = 0;
  double sumGyX = 0 , sumGyY = 0, sumGyZ = 0;
  getData();
  for (int i=0;i<10;i++) {
    getData();
    sumAcX+=AcX;  sumAcY+=AcY;  sumAcZ+=AcZ;
    sumGyX+=GyX;  sumGyY+=GyY;  sumGyZ+=GyZ;
    delay(50);
  }
  averAcX=sumAcX/10;  averAcY=sumAcY/10;  averAcZ=sumAcY/10;
  averGyX=sumGyX/10;  averGyY=sumGyY/10;  averGyZ=sumGyZ/10;
}//자이로


void blink() {
  count++;
} //포토


void setup() {
  initSensor();//자이로
  Wire.begin(); 
  caliSensor();   
  past = millis(); 
  
  Serial.begin(115200);
  
  pinMode(interruptPin, INPUT_PULLUP); //포토
  attachInterrupt(digitalPinToInterrupt(interruptPin), blink, FALLING);

  bt.begin(9600);  //블루투스
  time_previous = millis();
  
  pinMode(trig, OUTPUT); //초음파
  pinMode(echo, INPUT);  
  
  servo1.attach(6); //서보
  servo2.attach(7);
  servo3.attach(8);
  servo4.attach(9);
  
  servo1.write(90); //서보 초기 각도
  servo2.write(90);
  servo3.write(120);
  servo4.write(90);
  
  pinMode(buzzerPin, OUTPUT); //부저
}

void loop() {

  time_current = millis(); //블루투스
  
  int SensorReading = analogRead(Sensor); //압력
  int fsr = map(SensorReading, 0, 1024, 0, 255); 

  //포토
  float distance = (count/2.0) * (3.5 * PI); //unit : cm
  float velocity = distance / 1; //unit : cm/s
  velocity = velocity / 100000 * 3600; //unit : km/h 

  //초음파
  long duration, distance2;     
  digitalWrite(trig, LOW); 
  digitalWrite(echo, LOW);
  delayMicroseconds(2);
  digitalWrite(trig, HIGH);
  delayMicroseconds(10);     
  digitalWrite(trig, LOW);
  duration = pulseIn(echo, HIGH);
  distance = ((float)(340 * duration) / 10000) /2;  

  //자이로
  i++;
  if(i>20){
    caliSensor();
    i = 0;
  }

  getData(); 
  getDT();

  
  accelX = atan(AcY / sqrt(pow(AcX, 2) + pow(AcZ, 2)));
  accelX *= RADIAN_TO_DEGREE;
  accelY = atan(-AcX / sqrt(pow(AcY, 2) + pow(AcZ, 2)));
  accelY *= RADIAN_TO_DEGREE;

  gyroX = (GyX - averGyX) / DEG_PER_SEC; //오차의 평균값 빼줌
  gyroY = (GyY - averGyY) / DEG_PER_SEC;
  gyroZ = (GyZ - averGyZ) / DEG_PER_SEC;

  gyro_X = angle_X + dt * gyroX;  //변화량 적분
  gyro_Y = angle_Y + dt * gyroY;
  gyro_Z = angle_Z + dt * gyroZ;

  angle_X = ALPHA * gyro_X + (1.0 - ALPHA) * accelX; //상보필터
  angle_Y = ALPHA * gyro_Y + (1.0 - ALPHA) * accelY;
  angle_Z = gyro_Z; 

  //블루투스
  if(time_current - time_previous >= 1000){
    bt.print(angle_Y);
    bt.print(",");
    bt.println();
   }
        
  //flowchart 시작
  if (fsr > 100){
    //1압력센서 감지o → 경사 o -> 서보 (안전보조장치)
    Serial.println(String("압력이 감지되었습니다. ( ") + fsr + String(" )")); 
    if(velocity > 0){
      j++;
      Serial.print("j : ");
      Serial.println(j);
      if(j == 5){   
        
        if (10 < angle_X < 20 || -20 < angle_X < -10){
          Serial.println("경사가 있습니다. 안전보조장치 START");
          //안전보조장치 -> 자이로 출력 각도에 따라 서보 각도가 달라짐
          servo1.write(80);
          servo2.write(80);
          servo3.write(150);
          servo4.write(120);
                 
        }
        else if(20 < angle_X < 40 || -40 < angle_X < -20 ){
           servo1.write(70);
           servo2.write(70);
           servo3.write(160);
           servo4.write(130);
        }
        else if( angle_X > 40 || angle_X < -40){
           servo1.write(60);
           servo2.write(60);
           servo3.write(165);
           servo4.write(135);
        }
        delay(500);  
        j=0;  
      }
      
    }
  }
  else{
    //1서보 초기화. 압력센서 감지x -> 속도 감지
    servo1.write(90);
    servo2.write(90);
    servo3.write(120);
    servo4.write(90);
    Serial.println("압력이 0 입니다. 속도 감지를 시작합니다."); 
  
    if(velocity > 0){   //2압력센서 감지x -> 속도ㅇ -> 경사기울기 감지
        
      Serial.println(String("속도 : ") + velocity + String(" km/h"));
      Serial.println("경사기울기 감지를 시작합니다."); 
      Serial.println(String("경사 기울기 : ") + angle_X);  
  
      if(angle_X > 20 || angle_X < -20){  //3경사기울기ㅇ -> 장애물 감지
        
        Serial.println("경사가 있습니다. 장애물 감지를 시작합니다.");
        Serial.println(String("장애물과의 거리 : ") + distance + String(" cm"));
        
        if(distance < 70){  //4경사기울기ㅇ -> 장애물ㅇ -> 브레이크 경사에 따라 서서히 작동(continue)
          Serial.println("장애물 근접. 브레이크를 작동합니다.");  

          if(5 < angle_X < 10 || -10 < angle_X < -5){         
            servo1.write(3);
            servo2.write(3);
            servo3.write(178);
            servo4.write(178); 
            delay(200); 
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(10000);
         
          }            
          else if(10 < angle_X < 20 || -20 < angle_X < -10){
            servo1.write(5);
            servo2.write(5);
            servo3.write(175);
            servo4.write(175);
            delay(200);
            servo1.write(3);
            servo2.write(3);
            servo3.write(178);
            servo4.write(178); 
            delay(200);
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(10000);
           
          }
          else if(20 < angle_X < 40 || -40 < angle_X < -20 ){
            servo1.write(5);
            servo2.write(5);
            servo3.write(175);
            servo4.write(175);
            delay(200);
            servo1.write(4);
            servo2.write(4);
            servo3.write(176);
            servo4.write(176);
            delay(200);
            servo1.write(3);
            servo2.write(3);
            servo3.write(178);
            servo4.write(178); 
            delay(200);
            servo1.write(2);
            servo2.write(2);
            servo3.write(179);
            servo4.write(179);
            delay(200);
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(10000);  
          
          }
          else if( angle_X > 40 || angle_X < -40){
            servo1.write(5);
            servo2.write(5);
            servo3.write(175);
            servo4.write(175);
            delay(200);
            servo1.write(3);
            servo2.write(3);
            servo3.write(177);
            servo4.write(177); 
            delay(200);
            servo1.write(2);
            servo2.write(2);
            servo3.write(178);
            servo4.write(178);
            delay(200);
            servo1.write(1);
            servo2.write(1);
            servo3.write(179);
            servo4.write(179);
            delay(200);
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(10000);
            
          }
          delay(500);    
   
        }
        else{  //4경사기울기ㅇ -> 장애물x -> 장애물 감지x 누적 5회시 브레이크 작동(continue)
          k++;
          Serial.print("k : ");
          Serial.println(k);
          if(k == 30){
            if(5 < angle_X < 10 || -10 < angle_X < -5){         
            servo1.write(3);
            servo2.write(3);
            servo3.write(178);
            servo4.write(178); 
            delay(200);
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(10000);
             
          }            
          else if(10 < angle_X < 20 || -20 < angle_X < -10){
            servo1.write(5);
            servo2.write(5);
            servo3.write(175);
            servo4.write(175);
            delay(200);
            servo1.write(3);
            servo2.write(3);
            servo3.write(178);
            servo4.write(178); 
            delay(200);
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(10000);  
            
          } 
            else if(20 < angle_X < 40 || -40 < angle_X < -20 ){
              servo1.write(5);
              servo2.write(5);
              servo3.write(175);
              servo4.write(175);
              delay(200);
              servo1.write(4);
              servo2.write(4);
              servo3.write(176);
              servo4.write(176);
              delay(200);
              servo1.write(3);
              servo2.write(3);
              servo3.write(178);
              servo4.write(178); 
              delay(200);
              servo1.write(2);
              servo2.write(2);
              servo3.write(179);
              servo4.write(179);
              delay(200);
              servo1.write(0);
              servo2.write(0);
              servo3.write(180);
              servo4.write(180);
              delay(10000); 
              
            }
            else if( angle_X > 40 || angle_X < -40){
              servo1.write(5);
              servo2.write(5);
              servo3.write(175);
              servo4.write(175);
              delay(500);
              servo1.write(4);
              servo2.write(4);
              servo3.write(176);
              servo4.write(176);
              delay(300);
              servo1.write(3);
              servo2.write(3);
              servo3.write(177);
              servo4.write(177); 
              delay(200);
              servo1.write(2);
              servo2.write(2);
              servo3.write(178);
              servo4.write(178);
              delay(200);
              servo1.write(1);
              servo2.write(1);
              servo3.write(179);
              servo4.write(179);
              delay(200);
              servo1.write(0);
              servo2.write(0);
              servo3.write(180);
              servo4.write(180);
              delay(10000);
              
             
            }
            k=0;  
          }
        }
      }
      else{  //3경사기울기x -> 장애물 감지
  
        Serial.println("평지입니다. 장애물 감지를 시작합니다.");
        Serial.println(String("장애물과의 거리 : ") + distance + String(" cm"));
  
        if(distance < 50){  //4경사기울기x -> 장애물ㅇ -> 브레이크 작동(3sec)
          Serial.println("장애물 근접. 브레이크를 작동합니다.");  
  
          servo1.write(0);
          servo2.write(0);
          servo3.write(180);
          servo4.write(180);
          delay(3000);    
        }
        else{  //4경사기울기x -> 장애물x -> 장애물 감지x 누적 5회시 브레이크 작동(3sec)
          l++;
          Serial.print("l : ");
          Serial.println(l);
          if(l == 30){
            Serial.println("누적 30회. 브레이크를 작동합니다.");
            servo1.write(0);
            servo2.write(0);
            servo3.write(180);
            servo4.write(180);
            delay(3000);  
            l =0;
          }
              
        }
        
      }
        
    
      //WAS 기울기 감지
      Serial.print("기울기를 확인합니다 : ");
      Serial.println(angle_Y);

        
  
     
      if(40 <= angle_Y && angle_Y<= 45 || -45 < angle_Y && angle_Y < -40){  //WAS 기울기 -> +-50이상이면 부저&블루투스 알림
        m++;
        if(m == 5){
          Serial.println("WARNING. Please grab handle. PUSH");
          m = 0;
        }      
      }
      else if(angle_Y > 45 || angle_Y < -45){  //WAS 기울기 -> +-45up -> WAS falling
        n++;
        if(n = 3){
          Serial.println("WAS falling. Buzzer On");
          digitalWrite(buzzerPin, HIGH);
          delay(1000);
          digitalWrite(buzzerPin, LOW);  
          delay(500);
          n = 0;  
        }
      }
    
      
    }  
    else{  //2압력센서 감지x -> 속도x -> 브레이크 작동x(유모차 자체 브레이크 or 평지 = 압력감지로 돌아가기)
      
    }  
        
  }
  
  count = 0; // 포토
  delay(100);
    
  
}
