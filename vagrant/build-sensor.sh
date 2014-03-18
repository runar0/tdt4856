#!/bin/bash

if [ ! -f "/root/vagrant-updated" ]; then
	apt-get update
	#apt-get upgrade -y
	apt-get install openjdk-7-jdk bluez bluez-hcidump -y
	touch /root/vagrant-updated
fi

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386/jre
export PATH=/usr/lib/jvm/java-7-openjdk-i386/jre/bin:$PATH

# Test for hci0
hciconfig hci0 up
if [ "$?" -ne "0" ]; then
    echo ""
    echo "Unable to bring bluetooth up, is USB filter configured?"
fi

# Download sensor jar
if [ ! -f "sensor.jar" ]; then
	wget https://github.com/Runar0/tdt4856/releases/download/0.0.1/sensor.jar
fi

source /vagrant/settings.sh
java -jar sensor.jar $SENSOR_ALIAS

