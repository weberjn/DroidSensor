# DroidSensor

Android battery, power and wifi sensor

This is an Android Foreground service that posts battery, power, wifi and telephony events of the device to an MQTT server.

The service should start at boot.

## Permissions

Lots needed.

see [AndroidManifest.xml](droidsensor/src/main/AndroidManifest.xml)

## Settings

* MQTT Server URL: e.g. tcp://hostname or ssl://hostname

* topic prefix: e.g. droids/xperia

* post every nth battery level: e.g. 10  : post only every 10th battery level event

* Require Home Wifi: post only if on Home Wifi

* set Home Wifi from current: click to set to current

* WakeLock & Wifi Lock: should be checked to prevent the service from being stopped

## Topics Payloads

* battery/state {low,okay}
* battery/level $level/$scale
* power {connected,disconnected}
* wifi $networkInfoState
* telephony/{idle,ringing,offhook} $number

see [MyBroadcastReceiver.java](droidsensor/src/main/java/de/jwi/droidsensor/MyBroadcastReceiver.java)

## Test

    mosquitto_sub -F "%I %t %p" -t 'droids/xperia/#'
	
	2021-11-04T23:28:41+0100 droids/xperia/battery/level 16/100
	2021-12-29T20:51:11+0100 droids/xperia/telephony/ringing +49030



