
.PHONY: generate
generate: build sensor.proto beacon.proto
	protoc -I=. --java_out=build sensor.proto beacon.proto

build:
	mkdir build

	
.PHONY: clean
clean:
	rm -rf build
