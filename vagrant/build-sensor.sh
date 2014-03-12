#!/bin/bash

apt-get update
#apt-get upgrade -y
apt-get install build-essential openjdk-7-jdk ant bluez bluez-hcidump -y

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386/jre
export PATH=/usr/lib/jvm/java-7-openjdk-i386/jre/bin:$PATH

# Compile and install newest version of protobuf
which protoc
if [ "$?" -ne "0" ]; then
    cd ~
    wget https://protobuf.googlecode.com/files/protobuf-2.5.0.tar.bz2
    tar xvf protobuf-2.5.0.tar.bz2
    cd protobuf-2.5.0
    ./configure --prefix=/usr
    make && make install
    cd ~
 fi

# Test for hci0
hciconfig hci0 up
if [ "$?" -ne "0" ]; then
    echo ""
    echo "Unable to bring bluetooth up, is USB filter configured?"
    exit 1
fi

# Build our project
cd /source
#make

# Execute sensor app
cd /source/dist
#source /vagrant/settings.sh
java -jar sensor.jar sonos2

