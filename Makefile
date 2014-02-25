

# Create distribution jars
.PHONY: dist
dist: 
	if [ ! -d "./dist" ]; then mkdir dist; fi
	cd protocol; make
	cd sensor; ant; mv -v sensor.jar ../dist/
	cd vlcplayer; ant; mv -v vlcplayer.jar ../dist/
	cd central; ant; mv -v central.jar ../dist/

.PHONY: clean
clean:
	cd sensor; ant clean
	cd vlcplayer; ant clean
	cd central; ant clean
	
.PHONY: clean
purge: clean
	rm -rf dist
	cd protocol; make clean
