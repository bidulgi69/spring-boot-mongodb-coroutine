# spring-boot-mongodb-coroutine
MongoDB with spring boot + spring-data-mongodb + kotlin-coroutines

## Usage

1. Create Mongodb container

        docker-compose up -d

2. Build & Run

        ./gradlew build && java -jar build/libs/*.jar &

## Tests
-- All test codes were implemented using test containers.

### 1. MapperTest

This test verifies that the conversion function from entity class to api class is valid.

### 2. PersistenceTest

This test verifies that CRUD operations of persistence classes are valid using ReactiveMongoTemplate.

### 3. ServiceTest

This test verifies that services work properly when user requested through the controller.
