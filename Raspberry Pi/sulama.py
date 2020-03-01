import RPi.GPIO as GPIO
from firebase import firebase
from gpiozero import CPUTemperature
from datetime import datetime
import time

relay_pin = 26
motion_sensor_pin = 19
step = 0

GPIO.setmode(GPIO.BCM)
GPIO.setup(relay_pin, GPIO.OUT)
GPIO.setup(motion_sensor_pin, GPIO.IN)
GPIO.output(relay_pin, 0)

time.sleep(2)

firebase = firebase.FirebaseApplication("https://xxxxx.firebaseio.com/", None)

try:
    while True:

        if step == 0:
            cpu_temp = CPUTemperature().temperature
            firebase.put("https://xxxxx.firebaseio.com/", 'temperature', str(cpu_temp))

        step = (step+1)%5

        sulama_mode = firebase.get("https://xxxxx.firebaseio.com/",'sulama_mode')
        shutdown = firebase.get("https://xxxxx.firebaseio.com/",'shutdown')
        co = firebase.get("https://xxxxx.firebaseio.com/",'co')
        motion = firebase.get("https://xxxxx.firebaseio.com/",'motion')
        temp = firebase.get("https://xxxxx.firebaseio.com/",'temperature')
        timer_start = firebase.get("https://xxxxx.firebaseio.com/",'timer_start')
        timer_finish = firebase.get("https://xxxxx.firebaseio.com/",'timer_finish')
        timer_start = firebase.get("https://xxxxx.firebaseio.com/",'timer_start')
        motion_sensor_activated = firebase.get("https://xxxxx.firebaseio.com/",'motion_sensor_activated')

        if GPIO.input(motion_sensor_pin) and motion_sensor_activated == "True":
            firebase.put("https://xxxxx.firebaseio.com/", 'motion', "True")
            print "Motion Detected!"
            time.sleep(2) 

        if shutdown == "True":
            firebase.put("https://xxxxx.firebaseio.com/", 'shutdown', "False")
            firebase.put("https://xxxxx.firebaseio.com/", 'sulama_mode', "False")
            firebase.put("https://xxxxx.firebaseio.com/", 'timer_start', "Not set")
            firebase.put("https://xxxxx.firebaseio.com/", 'timer_finish', "Not set")
            break

        if sulama_mode == "True":
            GPIO.output(relay_pin, 0)
        else:
            GPIO.output(relay_pin, 1)

        if timer_start != "Not set":

            now = datetime.now()
            n_year = int(now.year)
            n_month = int(now.month)
            n_day = int(now.day)
            n_hour = int(now.hour)
            n_minute = int(now.minute)

            now = (n_year, n_month, n_day, n_hour, n_minute)

            set_time = int(timer_start.split('-').strip())
            s_year = int(set_time[0].split('.')[2])
            s_month = int(set_time[0].split('.')[1])
            s_day = int(set_time[0].split('.')[0])
            s_hour = int(set_time[1].split(':')[0])
            s_minute = int(set_time[1].split(':')[1])

            set_time = (s_year, s_month, s_day, s_hour, s_minute)

            if now == set_time:
                firebase.put("https://xxxxx.firebaseio.com/", 'sulama_mode', "True")

        if timer_finish != "Not set":
            
            now = datetime.now()
            n_year = int(now.year)
            n_month = int(now.month)
            n_day = int(now.day)
            n_hour = int(now.hour)
            n_minute = int(now.minute)

            now = (n_year, n_month, n_day, n_hour, n_minute)

            set_time = int(timer_start.split('-').strip())
            s_year = int(set_time[0].split('.')[2])
            s_month = int(set_time[0].split('.')[1])
            s_day = int(set_time[0].split('.')[0])
            s_hour = int(set_time[1].split(':')[0])
            s_minute = int(set_time[1].split(':')[1])

            set_time = (s_year, s_month, s_day, s_hour, s_minute)

            if now == set_time:
                firebase.put("https://xxxxx.firebaseio.com/", 'sulama_mode', "False")

        time.sleep(0.1) 

except KeyboardInterrupt:
    GPIO.cleanup()
except:
    print("Error occured !!!")
    GPIO.cleanup()

if shutdown == "True":
    GPIO.cleanup()
    os.system("sudo shutdown -h -t 1")