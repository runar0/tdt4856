
.PHONY: generate
generate: device.proto beacon.proto sensor.proto
	if [ ! -d "build"]; then mkdir build; fi
	protoc -I=. --java_out=build device.proto beacon.proto sensor.proto
	
.PHONY: clean
clean:
	rm -rf build
