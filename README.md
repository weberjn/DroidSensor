# DroidSensor

Android battery, power and wifi sensor

This is a Android Foreground service that posts battery, power and wifi events of the device to an MQTT server.

## Permissions

see [AndroidManifest.xml](droidsensor/src/main/AndroidManifest.xml)

## Settings

* MQTT URL: e.g. tcp://hostname or ssl://hostname

* topic prefix: e.g. droids/xperia

* post every nth battery level: e.g. 5  : post only every 5th battery level event

* Require Home Wifi: post only if on Home Wifi

* set Home Wifi from current: click to set to current

* WakeLock & Wifi Lock: should be checked to prevent the service from being stopped

## Topics

* battery/state
* battery/level
* power
* wifi

see [MyBroadcastReceiver.java](droidsensor/src/main/java/de/jwi/droidsensor/MyBroadcastReceiver.java)

## Test

    mosquitto_sub -F "%I %t %p" -t 'droidsensor/xperia/#'
	
	2021-11-04T23:28:41+0100 droids/xperia/battery/level 16/100




