

# Create distribution jars
.PHONY: dist
dist: 
	if [ ! -d "./dist" ]; then mkdir dist; fi
	cd protocol; make
	cd sensor; mvn package; mv -v target/sensor-1.0-SNAPSHOT.jar ../dist/sensor.jar
	cd vlcplayer; mvn package; mv -v target/vlcplayer-1.0-SNAPSHOT.jar ../dist/vlcplayer.jar
	cd central; mvn package; mv -v target/central-1.0-SNAPSHOT.jar ../dist/central.jar

.PHONY: clean
clean:
	cd sensor; mvn clean
	cd vlcplayer; mvn clean
	cd central; mvn clean
	
.PHONY: clean
purge: clean
	rm -rf dist
	cd protocol; make clean
