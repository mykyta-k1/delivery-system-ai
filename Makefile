.PHONY: run test clean compile

run:
	./mvnw spring-boot:run

test:
	./mvnw clean test

clean:
	./mvnw clean

compile:
	./mvnw clean compile
