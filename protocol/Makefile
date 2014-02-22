
.PHONY: generate
generate: device.proto beacon.proto sensor.proto player.proto
	mkdir build
	protoc -I=. --java_out=build device.proto beacon.proto sensor.proto player.proto
	
.PHONY: clean
clean:
	rm -rf build
