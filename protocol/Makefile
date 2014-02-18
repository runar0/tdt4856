
.PHONY: generate
generate: build sensor.proto
	protoc -I=. --java_out=build sensor.proto

build:
	mkdir build

	
.PHONY: clean
clean:
	rm -r build
