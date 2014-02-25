
.PHONY: generate
generate: device.proto beacon.proto sensor.proto
	mkdir build
	protoc -I=. --java_out=build device.proto beacon.proto sensor.proto
	
.PHONY: clean
clean:
	rm -rf build
