

# Create distribution jars
.PHONY: dist
dist: clean
	mkdir dist
	cd protocol; make
	cd sensor; ant; mv -v sensor.jar ../dist/
	cd vlcplayer; ant; mv -v vlcplayer.jar ../dist/

.PHONY: clean
clean:
	rm -rf dist/
	cd protocol; make clean
	cd sensor; ant clean
	cd vlcplayer; ant clean
	
	
