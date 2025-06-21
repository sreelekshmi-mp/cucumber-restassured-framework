FROM maven:3.9.5-eclipse-temurin-17

WORKDIR /app

COPY . /app

# command to run tests
CMD ["mvn", "clean", "test"]
